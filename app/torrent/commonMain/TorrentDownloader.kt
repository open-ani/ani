package me.him188.ani.app.torrent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.torrent.model.EncodedTorrentData
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import org.libtorrent4j.AlertListener
import org.libtorrent4j.Priority
import org.libtorrent4j.SessionManager
import org.libtorrent4j.Sha1Hash
import org.libtorrent4j.TorrentFlags
import org.libtorrent4j.TorrentInfo
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import java.io.File


/**
 * A torrent downloader which holds connection to the DHT network and peers.
 *
 * It must be closed when it is no longer needed.
 */
public interface TorrentDownloader : AutoCloseable {
    public val vendor: TorrentLibInfo

    public suspend fun fetchMagnet(magnet: String, timeoutSeconds: Int = 60): EncodedTorrentData

    public fun startDownload(data: EncodedTorrentData): TorrentDownloadSession
}

/**
 * A factory for creating [TorrentDownloader] instances without any argument.
 */
public fun interface TorrentDownloaderFactory {
    public suspend fun create(): TorrentDownloader
}

private val logger = logger(TorrentDownloader::class)

/**
 * Creates a new [TorrentDownloader] instance.
 *
 * The returned instance must be closed when it is no longer needed.
 *
 * @see TorrentDownloader
 */
public fun TorrentDownloader(
    cacheDirectory: File
): TorrentDownloader {
    val sessionManager = SessionManager()

    val listener = object : AlertListener {
        override fun types(): IntArray {
            return intArrayOf(AlertType.SESSION_STATS.swig(), AlertType.DHT_STATS.swig())
        }

        override fun alert(alert: Alert<*>) {
            if (alert.type() == AlertType.SESSION_STATS) {
                sessionManager.postDhtStats()
            }

//            if (alert.type() == AlertType.DHT_STATS) {
//                val nodes: Long = sessionManager.stats().dhtNodes()
//                // wait for at least 10 nodes in the DHT.
//                if (nodes >= 10) {
//                    dht.complete(Unit)
//                }
//            }
        }
    }
    sessionManager.addListener(listener)
    logger.info { "Starting SessionManager" }
    sessionManager.start()
    sessionManager.applySettings(sessionManager.settings().apply {
        isEnableDht = true
        activeDhtLimit(300)
        downloadRateLimit(0)
        connectionsLimit(200)
        maxPeerlistSize(1000)
        dhtBootstrapNodes = setOf(
            dhtBootstrapNodes.split(",") + listOf(
                "router.utorrent.com:6881",
                "router.bittorrent.com:6881",
                "dht.transmissionbt.com:6881",
                "router.bitcomet.com:6881",
            )
        ).joinToString(",")
    })
    logger.info { "postDhtStats" }
    sessionManager.postDhtStats()
    // No need to wait for DHT, some devices may not have access to the DHT network.

//    logger.info { "Waiting for nodes in DHT" }
//    val dhtResult = withTimeoutOrNull(30.seconds) {
//        dht.await()
//    }
//    if (dhtResult == null) {
//        logger.info { "DHT bootstrap failed" }
//        error("DHT bootstrap failed")
//    }
    sessionManager.removeListener(listener)

    return TorrentDownloaderImpl(
        cacheDirectory,
        sessionManager,
    )
}

internal class TorrentDownloaderImpl(
    cacheDirectory: File,
    private val sessionManager: SessionManager,
) : TorrentDownloader {
    private val logger = logger(this::class)
    override val vendor: TorrentLibInfo = TorrentLibInfo(
        vendor = "libtorrent",
        version = org.libtorrent4j.LibTorrent.version(),
    )

    override suspend fun fetchMagnet(magnet: String, timeoutSeconds: Int): EncodedTorrentData {
        logger.info { "Fetching magnet: $magnet" }
        val data = withContext(Dispatchers.IO) { sessionManager.fetchMagnet(magnet, timeoutSeconds, magnetCacheDir) }
        logger.info { "Fetched magnet: size=${data.size}" }
        return EncodedTorrentData(data)
    }

    private val magnetCacheDir = cacheDirectory.resolve("magnet").apply {
        mkdirs()

    }
    private val downloadCacheDir = cacheDirectory.resolve("download").apply {
        mkdirs()
    }

    private val dataToSession = mutableMapOf<Sha1Hash, TorrentDownloadSession>()

    @Synchronized
    override fun startDownload(data: EncodedTorrentData): TorrentDownloadSession {
        logger.info { "Starting torrent download session" }

        logger.info { "Decoding torrent info, input length=${data.data.size}" }
        val ti = TorrentInfo(data.data)
        logger.info { "Decoded TorrentInfo: ${ti.infoHash()}" }

        val saveDirectory = downloadCacheDir.resolve(data.data.contentHashCode().toString()).apply { mkdirs() }
        val hash = ti.infoHash()
        dataToSession[hash]?.let {
            logger.warn { "Reopening a torrent session, returning existing" }
            return it
        }
        dataToSession[hash] = TorrentDownloadSessionImpl(
            removeListener = { sessionManager.removeListener(it) },
            closeHandle = { sessionManager.remove(it) },
            torrentInfo = ti,
            saveDirectory = saveDirectory,
            onClose = {
                dataToSession.remove(hash)
            }
        )
        val session =
            TorrentDownloadSessionImpl(
                removeListener = { sessionManager.removeListener(it) },
                closeHandle = { sessionManager.remove(it) },
                torrentInfo = ti,
                saveDirectory = saveDirectory,
                onClose = {
                    dataToSession.remove(hash)
                }
            )
        sessionManager.addListener(session.listener)

        val priorities = Priority.array(Priority.IGNORE, ti.numFiles())
        logger.info { "File name: ${ti.files().fileName(0)}" }
        priorities[0] = Priority.TOP_PRIORITY

        try {
            logger.info { "Starting torrent download" }
            sessionManager.download(
                ti,
                saveDirectory,
                null,
                priorities,
                null,
                TorrentFlags.SEQUENTIAL_DOWNLOAD,//.or_(TorrentFlags.NEED_SAVE_RESUME)
            )
            logger.info { "Torrent download started." }
        } catch (e: Throwable) {
            sessionManager.removeListener(session.listener)
            throw e
        }

        return session
    }

    override fun close() {
        sessionManager.stop()
    }
}

