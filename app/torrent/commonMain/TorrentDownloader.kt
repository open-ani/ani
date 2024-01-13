package me.him188.ani.app.torrent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import me.him188.ani.app.torrent.TorrentConfig.DEFAULT_DOWNLOAD_HEADER_CHUNKS
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.libtorrent4j.AlertListener
import org.libtorrent4j.Priority
import org.libtorrent4j.SessionManager
import org.libtorrent4j.TorrentFlags
import org.libtorrent4j.TorrentInfo
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import java.io.File
import kotlin.time.Duration.Companion.seconds


@JvmInline
public value class EncodedTorrentData(
    public val data: ByteArray,
)

public interface TorrentDownloader {
    public val vendor: TorrentLibInfo

    public suspend fun fetchMagnet(magnet: String, timeoutSeconds: Int = 60): EncodedTorrentData

    public fun startDownload(data: EncodedTorrentData): TorrentDownloadSession
}

public object TorrentConfig {
    public const val DEFAULT_DOWNLOAD_HEADER_CHUNKS: Int = 8
}

public fun TorrentDownloader(
    cacheDirectory: File,
    downloadHeaderChunks: Int = DEFAULT_DOWNLOAD_HEADER_CHUNKS
): TorrentDownloader =
    TorrentDownloaderImpl(cacheDirectory, downloadHeaderChunks)

public class TorrentLibInfo(
    public val vendor: String,//  "libtorrent"
    public val version: String, // LibTorrent.version()
)

internal class TorrentDownloaderImpl(
    cacheDirectory: File,
    private val downloadHeaderChunks: Int,
) : TorrentDownloader {
    private val logger = logger(this::class)
    override val vendor: TorrentLibInfo = TorrentLibInfo(
        vendor = "libtorrent",
        version = org.libtorrent4j.LibTorrent.version(),
    )

    private val magnetCacheDir = cacheDirectory.resolve("magnet").apply {
        mkdirs()

    }
    private val downloadCacheDir = cacheDirectory.resolve("download").apply {
        mkdirs()
    }

    override suspend fun fetchMagnet(magnet: String, timeoutSeconds: Int): EncodedTorrentData {
        val sessionManager = SessionManager()

        val dht = CompletableDeferred<Unit>()
        val listener = object : AlertListener {
            override fun types(): IntArray {
                return intArrayOf(AlertType.SESSION_STATS.swig(), AlertType.DHT_STATS.swig())
            }

            override fun alert(alert: Alert<*>) {
                if (alert.type() == AlertType.SESSION_STATS) {
                    sessionManager.postDhtStats()
                }

                if (alert.type() == AlertType.DHT_STATS) {
                    val nodes: Long = sessionManager.stats().dhtNodes()
                    // wait for at least 10 nodes in the DHT.
                    if (nodes >= 10) {
                        dht.complete(Unit)
                    }
                }
            }
        }
        sessionManager.addListener(listener)
        logger.info { "Starting session" }
        sessionManager.start()
        logger.info { "postDhtStats" }
        sessionManager.postDhtStats()
        logger.info { "Waiting for nodes in DHT" }
        val dhtResult = withTimeoutOrNull(30.seconds) {
            dht.await()
        }
        if (dhtResult == null) {
            logger.info { "DHT bootstrap failed" }
            error("DHT bootstrap failed")
        }
        sessionManager.removeListener(listener)

        logger.info { "Fetching magnet, magnetCacheDir=${magnetCacheDir.absolutePath}" }
        logger.info { "magnet=$magnet" }

        try {
            val data = withContext(Dispatchers.IO) {
                sessionManager.fetchMagnet(magnet, timeoutSeconds, magnetCacheDir)
            }
            logger.info { "Successfully fetched torrent data, length=${data.size}" }
            return EncodedTorrentData(data)
        } finally {
            sessionManager.stop()
        }
    }

    override fun startDownload(data: EncodedTorrentData): TorrentDownloadSession {
        val sessionManager = SessionManager()
        logger.info { "Starting torrent download session" }

        logger.info { "Decoding torrent info, input length=${data.data.size}" }
        val ti = TorrentInfo(data.data)
        logger.info { "Decoded TorrentInfo: $ti" }

        val priorities = Priority.array(Priority.IGNORE, ti.numFiles())
        logger.info { "File name: ${ti.files().fileName(0)}" }
        priorities[0] = Priority.TOP_PRIORITY

        val session =
            TorrentDownloadSessionImpl(
                sessionManager, ti,
                downloadCacheDir.resolve(ti.files().fileName(0)),
                downloadHeaderChunks,
            )
        sessionManager.addListener(session.listener)

        logger.info { "Starting session" }
        sessionManager.start()

        try {
            logger.info { "Starting torrent download" }
            sessionManager.download(
                ti,
                downloadCacheDir,
                null,
                priorities,
                null,
                TorrentFlags.SEQUENTIAL_DOWNLOAD,//.or_(TorrentFlags.NEED_SAVE_RESUME)
            )
            logger.info { "Torrent download started." }
        } catch (e: Throwable) {
            sessionManager.stop()
            throw e
        }

        return session
    }
}

