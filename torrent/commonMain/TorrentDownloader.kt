package me.him188.ani.app.torrent

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.him188.ani.app.torrent.model.EncodedTorrentData
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import org.libtorrent4j.AlertListener
import org.libtorrent4j.Priority
import org.libtorrent4j.SessionManager
import org.libtorrent4j.SettingsPack
import org.libtorrent4j.Sha1Hash
import org.libtorrent4j.TorrentFlags
import org.libtorrent4j.TorrentInfo
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import org.libtorrent4j.alerts.TorrentAlert
import org.libtorrent4j.swig.settings_pack
import org.libtorrent4j.swig.settings_pack.string_types
import java.io.File
import java.nio.charset.Charset
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
     * Total amount of bytes uploaded
     */
    public val totalUploaded: Flow<Long>
    public val totalDownloaded: Flow<Long>

    public val totalUploadRate: Flow<Long>
    public val totalDownloadRate: Flow<Long>

    /**
     * Total file size occupied on the disk.
     */
    public val dhtNodes: Flow<Long>


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

public enum class FilePriority {
    HIGH,
    NORMAL,
    LOW,
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

public class TorrentDownloaderConfig(
    public val peerFingerprint: String = "-aniLT3000-",
    public val userAgent: String = "ani_libtorrent/3.0.0", // "libtorrent/2.1.0.0", "ani_libtorrent/3.0.0"
    public val clientHandshakeVersion: String? = "3.0.0",
    public val isDebug: Boolean = false,
) {
    public companion object {
        public val Default: TorrentDownloaderConfig = TorrentDownloaderConfig()
    }
}

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
    config: TorrentDownloaderConfig = TorrentDownloaderConfig.Default,
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
        isEnableLsd = true
        activeDhtLimit(300)
        activeTrackerLimit(50)
        activeSeeds(8)
        activeDownloads(8)
        connectionsLimit(500) // default was 200
        seedingOutgoingConnections(true) // default was true, just to make sure
        uploadRateLimit(0)
        downloadRateLimit(0)
        maxPeerlistSize(1000)
        dhtBootstrapNodes = setOf(
            dhtBootstrapNodes.split(",") + listOf(
                "router.utorrent.com:6881",
                "router.bittorrent.com:6881",
                "dht.transmissionbt.com:6881",
                "router.bitcomet.com:6881",
            )
        ).joinToString(",")

        logger.info { "peerFingerprint was: $peerFingerprintString" }
        logger.info { "user_agent was: $userAgentString" }
        logger.info { "handshake_client_version was: $handshakeClientVersionString" }

        peerFingerprintString = config.peerFingerprint
        userAgentString = config.userAgent
        config.clientHandshakeVersion?.let {
            handshakeClientVersionString = it
        }
        logger.info { "peerFingerprint set: $peerFingerprintString" }
        logger.info { "user_agent set: $userAgentString" }
        logger.info { "handshake_client_version set: $handshakeClientVersionString" }
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
        downloadFile,
        isDebug = config.isDebug,
    )
}

internal class TorrentDownloaderImpl(
    cacheDirectory: File,
    private val sessionManager: LockedSessionManager,
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

    override val vendor: TorrentLibInfo = TorrentLibInfo(
        vendor = "libtorrent",
        version = org.libtorrent4j.LibTorrent.version(),
    )

    override suspend fun fetchTorrent(uri: String, timeoutSeconds: Int): EncodedTorrentData {
        if (uri.startsWith("http", ignoreCase = true)) {
            logger.info { "Fetching http url: $uri" }
            val data = downloadFile(uri)
            logger.info { "Fetching http url success, file length = ${data.size}" }
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

    private val dataToSession = ConcurrentHashMap<Sha1Hash, TorrentDownloadSessionImpl>()

    private val lock = Mutex()

    override suspend fun startDownload(
        data: EncodedTorrentData,
        parentCoroutineContext: CoroutineContext,
    ): TorrentDownloadSession = lock.withLock {
        val ti = TorrentInfo(data.data)
        val torrentName = ti.name()

        logger.info { "[$torrentName] TorrentDownloader.startDownload called" }

        val saveDirectory = downloadCacheDir.resolve(data.data.contentHashCode().toString()).apply { mkdirs() }
        val hash = ti.infoHash()

        val reopened = dataToSession[hash]?.let {
            logger.info { "[$torrentName] Found existing session" }
            true
        } ?: kotlin.run {
            logger.info { "[$torrentName] This is a new session" }
            false
        }

        val session =
            dataToSession[hash] ?: TorrentDownloadSessionImpl(
                torrentName = torrentName,
                saveDirectory = saveDirectory,
                onClose = { session ->
                    logger.debug { "[$torrentName] Close: removeListener" }
                    sessionManager.removeListener(session.listener)
                    dataToSession.remove(hash)
                    logger.debug { "[$torrentName] Close: close handle" }
                    sessionManager.use {
                        find(hash)?.let { handle ->
                            logger.debug { "[$torrentName] Close: remove from libtorrent SessionManager" }
                            remove(handle)
                            logger.debug { "[$torrentName] Close: removed" }
                        } ?: run {
                            logger.debug { "[$torrentName] Close: handle not found, ignoring" }
                        }
                    }
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
                val priorities = Priority.array(Priority.IGNORE, ti.numFiles())
                try {
                    logger.info { "[$torrentName] Starting torrent download" }
                    download(
                        ti,
                        saveDirectory,
                        null,
                        priorities,
                        null,
                        TorrentFlags.AUTO_MANAGED,
//                TorrentFlags.SEQUENTIAL_DOWNLOAD,//.or_(TorrentFlags.NEED_SAVE_RESUME)
                    )
                    logger.info { "[$torrentName] Torrent download started" }
                } catch (e: Throwable) {
                    sessionManager.removeListener(session.listener)
                    logger.error(e) { "[$torrentName] Failed to start torrent download" }
                    throw e
                }
            }
        }

        return session
    }

    override fun close() {
        scope.cancel()
        LockedSessionManager.launch { sessionManager.use { stop() } }
    }
}

internal fun FilePriority.toLibtorrentPriority(): Priority = when (this) {
    FilePriority.HIGH -> Priority.TOP_PRIORITY
    FilePriority.NORMAL -> Priority.DEFAULT
    FilePriority.LOW -> Priority.IGNORE
}

public var SettingsPack.peerFingerprintString: String
    get() = getBytes(string_types.peer_fingerprint.swigValue()).toString(Charset.forName("UTF-8"))
    set(value) {
        setBytes(string_types.peer_fingerprint.swigValue(), value.toByteArray(Charset.forName("UTF-8")))
    }

public var SettingsPack.userAgentString: String
    get() = getBytes(string_types.user_agent.swigValue()).toString(Charset.forName("UTF-8"))
    set(value) {
        setBytes(string_types.user_agent.swigValue(), value.toByteArray(Charset.forName("UTF-8")))
    }

public var SettingsPack.handshakeClientVersionString: String
    get() = getBytes(string_types.handshake_client_version.swigValue()).toString(Charset.forName("UTF-8"))
    set(value) {
        setBytes(string_types.handshake_client_version.swigValue(), value.toByteArray(Charset.forName("UTF-8")))
    }

