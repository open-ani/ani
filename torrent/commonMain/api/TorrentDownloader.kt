package me.him188.ani.app.torrent.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.him188.ani.app.torrent.torrent4j.LockedSessionManager
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.libtorrent4j.SessionManager
import org.libtorrent4j.SettingsPack
import org.libtorrent4j.swig.settings_pack
import org.libtorrent4j.swig.settings_pack.string_types
import java.io.File
import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap
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
interface TorrentDownloader : AutoCloseable {
    /**
     * Total amount of bytes uploaded
     */
    val totalUploaded: Flow<Long>
    val totalDownloaded: Flow<Long>

    val totalUploadRate: Flow<Long>
    val totalDownloadRate: Flow<Long>

    /**
     * Total file size occupied on the disk.
     */
    val dhtNodes: Flow<Long>


    /**
     * Details about the underlying torrent library.
     */
    val vendor: TorrentLibInfo

    /**
     * Fetches a magnet link.
     *
     * @param uri supports magnet link or http link for the torrent file
     *
     * @throws MagnetTimeoutException if timeout has been reached.
     */
    suspend fun fetchTorrent(uri: String, timeoutSeconds: Int = 60): EncodedTorrentInfo

    /**
     * Starts download of a torrent using the torrent data.
     *
     * This function may involve I/O operation e.g. to compare with local caches.
     */
    suspend fun startDownload(
        data: EncodedTorrentInfo,
        parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    ): TorrentDownloadSession

    fun getSaveDir(
        data: EncodedTorrentInfo,
    ): File

    /**
     * 获取所有的种子保存目录列表
     */
    fun listSaves(): List<File>

    override fun close()
}

class MagnetTimeoutException(
    override val message: String? = "Magnet fetch timeout",
    override val cause: Throwable? = null
) : Exception()

/**
 * A factory for creating [TorrentDownloader] instances without any argument.
 */
fun interface TorrentDownloaderFactory {
    suspend fun create(): TorrentDownloader
}

typealias TorrentFileDownloader = suspend (url: String) -> ByteArray

class TorrentDownloaderConfig(
    val peerFingerprint: String = "-aniLT3000-",
    val userAgent: String = "ani_libtorrent/3.0.0", // "libtorrent/2.1.0.0", "ani_libtorrent/3.0.0"
    val clientHandshakeVersion: String? = "3.0.0",
    val isDebug: Boolean = false,
) {
    companion object {
        val Default: TorrentDownloaderConfig = TorrentDownloaderConfig()
    }
}

internal abstract class AbstractLockedTorrentDownloader<Info : TorrentInfo>(
    cacheDirectory: File,
    val sessionManager: LockedSessionManager,
    private val downloadFile: TorrentFileDownloader,
    private val isDebug: Boolean,
) : TorrentDownloader {
    private val scope = CoroutineScope(SupervisorJob())

    private val logger = logger(this::class)

    override val totalUploaded: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val totalDownloaded: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val totalUploadRate: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val totalDownloadRate: MutableStateFlow<Long> = MutableStateFlow(0L)

    override val dhtNodes: MutableStateFlow<Long> = MutableStateFlow(0L)

    init {
        scope.launch {
            while (currentCoroutineContext().isActive) {
                val stats = sessionManager.use { stats() }
                totalUploaded.value = stats.totalUpload()
                totalDownloaded.value = stats.totalDownload()
                totalUploadRate.value = stats.uploadRate()
                totalDownloadRate.value = stats.downloadRate()
                dhtNodes.value = stats.dhtNodes()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    override suspend fun fetchTorrent(uri: String, timeoutSeconds: Int): EncodedTorrentInfo {
        if (uri.startsWith("http", ignoreCase = true)) {
            logger.info { "Fetching http url: $uri" }
            val data = downloadFile(uri)
            logger.info { "Fetching http url success, file length = ${data.size}" }
            return EncodedTorrentInfo(data)
        }

        logger.info { "Fetching magnet: $uri" }
        val data: ByteArray? = try {
            sessionManager.use {
                fetchMagnet(uri, timeoutSeconds, magnetCacheDir)
            }
        } catch (e: InterruptedException) {
            throw MagnetTimeoutException(cause = e)
        }
        if (data == null) {
            throw MagnetTimeoutException()
        }
        logger.info { "Fetched magnet: size=${data.size}" }
        return EncodedTorrentInfo(data)
    }

    private val magnetCacheDir = cacheDirectory.resolve("magnet").apply {
        mkdirs()

    }
    private val downloadCacheDir = cacheDirectory.resolve("api/pieces").apply {
        mkdirs()
    }

    protected val dataToSession = ConcurrentHashMap<String, DefaultTorrentDownloadSession>()

    private val lock = Mutex()

    protected abstract fun decodeTorrentInfo(data: EncodedTorrentInfo): Info

    protected abstract fun SessionManager.startDownload(
        ti: Info,
        saveDirectory: File
    )

    protected abstract suspend fun closeSession(info: Info)

    override suspend fun startDownload(
        data: EncodedTorrentInfo,
        parentCoroutineContext: CoroutineContext,
    ): TorrentDownloadSession = lock.withLock {
        withContext(Dispatchers.IO) {
            val torrentInfo = decodeTorrentInfo(data)
            val torrentName = torrentInfo.name

            logger.info { "[$torrentName] TorrentDownloader.startDownload called" }
            val saveDirectory = getSaveDir(data).apply { mkdirs() }
            val hash = torrentInfo.infoHashHex

            val reopened = dataToSession[hash]?.let {
                logger.info { "[$torrentName] Found existing session" }
                true
            } ?: kotlin.run {
                logger.info { "[$torrentName] This is a new session" }
                false
            }

            val session =
                dataToSession[hash] ?: DefaultTorrentDownloadSession(
                    torrentName = torrentName,
                    saveDirectory = saveDirectory,
                    onClose = { session ->
                        logger.debug { "[$torrentName] Close: removeListener" }
                        sessionManager.removeListener(session.listener)
                        dataToSession.remove(hash)
                        logger.debug { "[$torrentName] Close: close handle" }
                        closeSession(torrentInfo)
                    },
                    isDebug = isDebug,
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

                if (reopened) {
                    logger.info { "[$torrentName] Torrent has already opened" }
                } else {
                    sessionManager.addListener(session.listener)

                    // 第一次打开, 设置忽略所有文件, 除了我们需要的那些
                    try {
                        logger.info { "[$torrentName] Starting torrent download" }
                        startDownload(torrentInfo, saveDirectory)
                        logger.info { "[$torrentName] Torrent download started" }
                    } catch (e: Throwable) {
                        sessionManager.removeListener(session.listener)
                        logger.error(e) { "[$torrentName] Failed to start torrent download" }
                        throw e
                    }
                }
            }
            return@withContext session
        }
    }


    override fun getSaveDir(data: EncodedTorrentInfo): File =
        downloadCacheDir.resolve(data.data.contentHashCode().toString())

    override fun listSaves(): List<File> {
        return downloadCacheDir.listFiles()?.toList() ?: emptyList()
    }

    override fun close() {
        scope.cancel()
        LockedSessionManager.launch { sessionManager.use { stop() } }
    }
}

var SettingsPack.peerFingerprintString: String
    get() = getBytes(string_types.peer_fingerprint.swigValue()).toString(Charset.forName("UTF-8"))
    set(value) {
        setBytes(string_types.peer_fingerprint.swigValue(), value.toByteArray(Charset.forName("UTF-8")))
    }

var SettingsPack.userAgentString: String
    get() = getBytes(string_types.user_agent.swigValue()).toString(Charset.forName("UTF-8"))
    set(value) {
        setBytes(string_types.user_agent.swigValue(), value.toByteArray(Charset.forName("UTF-8")))
    }

var SettingsPack.handshakeClientVersionString: String
    get() = getBytes(string_types.handshake_client_version.swigValue()).toString(Charset.forName("UTF-8"))
    set(value) {
        setBytes(string_types.handshake_client_version.swigValue(), value.toByteArray(Charset.forName("UTF-8")))
    }

