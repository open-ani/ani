package me.him188.ani.app.torrent

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.him188.ani.app.torrent.model.EncodedTorrentData
import me.him188.ani.utils.logging.error
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
import org.libtorrent4j.alerts.TorrentAlert
import org.libtorrent4j.swig.settings_pack
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 * A torrent downloader.
 *
 * It is stateful:
 * - it may hold connection to the DHT network and peers.
 *
 * Hence it must be closed when it is no longer needed.
 */
public interface TorrentDownloader : AutoCloseable {
    /**
     * Details about the underlying torrent library.
     */
    public val vendor: TorrentLibInfo

    /**
     * Fetches a magnet link.
     *
     * @param uri supports magnet link or http link for the torrent file
     *
     * @throws MagnetTimeoutException if timeout has been reached.
     */
    public suspend fun fetchTorrent(uri: String, timeoutSeconds: Int = 60): EncodedTorrentData

    /**
     * Starts download of a torrent using the torrent data.
     *
     * This function may involve I/O operation e.g. to compare with local caches.
     */
    public suspend fun startDownload(
        data: EncodedTorrentData,
        parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    ): TorrentDownloadSession

    public override fun close()
}

public class MagnetTimeoutException(
    override val message: String? = "Magnet fetch timeout",
    override val cause: Throwable? = null
) : Exception()

/**
 * A factory for creating [TorrentDownloader] instances without any argument.
 */
public fun interface TorrentDownloaderFactory {
    public suspend fun create(): TorrentDownloader
}

private val logger = logger(TorrentDownloader::class)

internal class LockedSessionManager(
    private val sessionManager: SessionManager,
) {
    private val listeners = CopyOnWriteArraySet<TorrentAlertListener>()

    init {
        sessionManager.addListener(object : AlertListener {
            override fun types(): IntArray? = null
            override fun alert(alert: Alert<*>) {
                if (alert !is TorrentAlert<*>) return
                listeners.forEach {
                    try {
                        it.onAlert(alert)
                    } catch (e: Throwable) {
                        logger.error(e) { "An exception occurred in AlertListener" }
                    }
                }
            }
        })
    }

    fun addListener(listener: TorrentAlertListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: TorrentAlertListener) {
        listeners.remove(listener)
    }

    suspend inline fun <R> use(
        crossinline block: SessionManager.() -> R
    ) = withContext(dispatcher) {
        block(sessionManager)
    }

    companion object {
        /**
         * Shared dispatcher for all [LockedSessionManager] instances.
         *
         * Libtorrent crashes (the entire VM) if it is called from multiple threads.
         */ // unfortunately, we have to keep this dispatcher live for the entire app lifecycle.
        val dispatcher = Executors.newSingleThreadExecutor()
            .asCoroutineDispatcher()

        private val logger = logger(LockedSessionManager::class)

        private val scope = CoroutineScope(dispatcher + CoroutineExceptionHandler { _, throwable ->
            logger.warn(throwable) { "An exception occurred in LockedSessionManager" }
        })

        fun launch(block: suspend () -> Unit) {
            scope.launch {
                block()
            }
        }
    }
}

public typealias TorrentFileDownloader = suspend (url: String) -> ByteArray

/**
 * Creates a new [TorrentDownloader] instance.
 *
 * The returned instance must be closed when it is no longer needed.
 *
 * @see TorrentDownloader
 */
public fun TorrentDownloader(
    cacheDirectory: File,
    downloadFile: TorrentFileDownloader,
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
        uploadRateLimit(0)
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
        LockedSessionManager(sessionManager),
        downloadFile
    )
}

internal class TorrentDownloaderImpl(
    cacheDirectory: File,
    private val sessionManager: LockedSessionManager,
    private val downloadFile: TorrentFileDownloader,
) : TorrentDownloader {
    private val logger = logger(this::class)
    override val vendor: TorrentLibInfo = TorrentLibInfo(
        vendor = "libtorrent",
        version = org.libtorrent4j.LibTorrent.version(),
    )

    override suspend fun fetchTorrent(uri: String, timeoutSeconds: Int): EncodedTorrentData {
        if (uri.startsWith("http", ignoreCase = true)) {
            logger.info { "Fetching http url: $uri" }
            val data = downloadFile(uri)
            logger.info { "Fetching http url success, file length = ${data.size}" }
            logger.info { "File downloaded" }
            return EncodedTorrentData(data)
        }

        logger.info { "Fetching magnet: $uri" }
        val data: ByteArray = try {
            sessionManager.use {
                fetchMagnet(uri, timeoutSeconds, magnetCacheDir)
            }
        } catch (e: InterruptedException) {
            throw MagnetTimeoutException(cause = e)
        }
        logger.info { "Fetched magnet: size=${data.size}" }
        return EncodedTorrentData(data)
    }

    private val magnetCacheDir = cacheDirectory.resolve("magnet").apply {
        mkdirs()

    }
    private val downloadCacheDir = cacheDirectory.resolve("download").apply {
        mkdirs()
    }

    private val dataToSession = ConcurrentHashMap<Sha1Hash, TorrentDownloadSession>()

    private val lock = Mutex()

    override suspend fun startDownload(
        data: EncodedTorrentData,
        parentCoroutineContext: CoroutineContext
    ): TorrentDownloadSession = lock.withLock {
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
        val session =
            TorrentDownloadSessionImpl(
                torrentName = ti.name(),
                closeHandle = { sessionManager.use { remove(it) } },
                torrentInfo = ti,
                saveDirectory = saveDirectory,
                onClose = {
                    sessionManager.removeListener(it.listener)
                    dataToSession.remove(hash)
                },
                parentCoroutineContext = parentCoroutineContext,
            )
        dataToSession[hash] = session
        sessionManager.use {
            settings().run {
                //  https://libtorrent.org/reference-Settings.html#settings_pack
//            setInteger(settings_pack.int_types.piece_timeout.swigValue(), 3)
                setInteger(settings_pack.int_types.request_timeout.swigValue(), 3)
                setInteger(settings_pack.int_types.peer_timeout.swigValue(), 3)
            }
            sessionManager.addListener(session.listener)

            val priorities = Priority.array(Priority.IGNORE, ti.numFiles())
            logger.info { "File name: ${ti.files().fileName(0)}" }
            priorities[0] = Priority.TOP_PRIORITY

            try {
                logger.info { "Starting torrent download" }
                download(
                    ti,
                    saveDirectory,
                    null,
                    priorities,
                    null,
                    TorrentFlags.UPDATE_SUBSCRIBE,
//                TorrentFlags.SEQUENTIAL_DOWNLOAD,//.or_(TorrentFlags.NEED_SAVE_RESUME)
                )
                logger.info { "Torrent download started." }
            } catch (e: Throwable) {
                sessionManager.removeListener(session.listener)
                throw e
            }
        }

        return session
    }

    override fun close() {
        LockedSessionManager.launch { sessionManager.use { stop() } }
    }
}

