package me.him188.ani.app.torrent.anitorrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.app.torrent.anitorrent.binding.anitorrent
import me.him188.ani.app.torrent.anitorrent.binding.event_listener_t
import me.him188.ani.app.torrent.anitorrent.binding.metadata_received_event_t
import me.him188.ani.app.torrent.anitorrent.binding.session_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_add_info_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_handle_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_state_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_stats_t
import me.him188.ani.app.torrent.api.HttpFileDownloader
import me.him188.ani.app.torrent.api.TorrentDownloadSession
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.app.torrent.api.TorrentLibInfo
import me.him188.ani.app.torrent.api.files.EncodedTorrentInfo
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

@Suppress("ObjectPropertyName")
private val _initAnitorrent by lazy {

    // 注意, 装了这东西之后反而可能导致 SIG 10/11, 不知道为什么
//    anitorrent.install_signal_handlers()
}

fun AnitorrentTorrentDownloader(
    cacheDirectory: File,
    httpFileDownloader: HttpFileDownloader,
    isDebug: Boolean,
    parentCoroutineContext: CoroutineContext,
): AnitorrentTorrentDownloader {
    if (currentPlatform is Platform.Windows) {
    } else {
        // TODO: this doesn't work 
        System.setProperty(
            "java.library.path",
            System.getProperty("java.library.path") + ":" +
                    File(System.getProperty("user.dir")).resolve("../appResources/macos-arm64/lib").absolutePath,
        )
    }
    System.loadLibrary("anitorrent")
    _initAnitorrent

    println("Using libtorrent version: " + anitorrent.lt_version())


    val session = session_t()
    session.start(getAniUserAgent())
    return AnitorrentTorrentDownloader(
        cacheDirectory = cacheDirectory,
        session = session,
        httpFileDownloader = httpFileDownloader,
        isDebug = isDebug,
        parentCoroutineContext = parentCoroutineContext,
    )
}

class AnitorrentTorrentDownloader(
    cacheDirectory: File,
    val session: session_t,
    private val httpFileDownloader: HttpFileDownloader,
    private val isDebug: Boolean,
    parentCoroutineContext: CoroutineContext,
) : TorrentDownloader {
    companion object {
        internal val logger = logger(this::class)
    }

    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))


    override val totalUploaded: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val totalDownloaded: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val totalUploadRate: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val totalDownloadRate: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val vendor: TorrentLibInfo = TorrentLibInfo(
        vendor = "Anitorrent",
        version = "1.0.0",
        supportsStreaming = true,
    )

    val openSessions = ConcurrentHashMap<String, AnitorrentDownloadSession>()

    inline fun forEachSession(id: HandleId, block: (AnitorrentDownloadSession) -> Unit) {
        contract { callsInPlace(block, InvocationKind.UNKNOWN) }
        openSessions.values.forEach {
            if (it.id == id) {
                block(it)
            }
        }
    }


    // must keep referenced
    private val eventListener = AnitorrentEventListener(this)

    init {
        scope.launch {
            while (currentCoroutineContext().isActive) {
                // TODO: stats 
//                val stats = sessionManager.getStats()
//                totalUploaded.value = stats.totalUpload()
//                totalDownloaded.value = stats.totalDownload()
//                totalUploadRate.value = stats.uploadRate()
//                totalDownloadRate.value = stats.downloadRate()
                delay(1000)
            }
        }

        scope.launch {
            while (true) {
                session.process_events(eventListener)
                delay(1000)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun fetchTorrent(uri: String, timeoutSeconds: Int): EncodedTorrentInfo {
        if (uri.startsWith("http", ignoreCase = true)) {
            val cacheFile = getHttpTorrentFileCacheFile(uri)
            if (cacheFile.exists()) {
                val data = cacheFile.readText().hexToByteArray()
                logger.info { "HTTP torrent file '${uri}' found in cache: $cacheFile, length=${data.size}" }
                return AnitorrentTorrentInfo.encode(AnitorrentTorrentInfo(AnitorrentTorrentData.TorrentFile(data)))
            }
            logger.info { "Fetching http url: $uri" }
            val data = httpFileDownloader.download(uri)
            logger.info { "Fetching http url success, file length = ${data.size}" }
            cacheFile.writeText(data.toHexString())
            logger.info { "Saved cache file: $cacheFile" }
            return AnitorrentTorrentInfo.encode(AnitorrentTorrentInfo(AnitorrentTorrentData.TorrentFile(data)))
        }

//        logger.info { "Fetching magnet: $uri" }
//        val str: String = try {
//            session.fetch_magnet(uri, timeoutSeconds, magnetCacheDir.absolutePath)
//        } catch (e: InterruptedException) {
//            throw FetchTorrentTimeoutException(cause = e)
//        }
//        val data = str.toByteArray()
//        if (data.isEmpty()) {
//            throw FetchTorrentTimeoutException()
//        }
//        logger.info { "Fetched magnet: size=${data.size}" }
        require(uri.startsWith("magnet")) { "Expected uri to start with \"magnet\": $uri" }
        return AnitorrentTorrentInfo.encode(AnitorrentTorrentInfo(AnitorrentTorrentData.MagnetUri(uri)))
    }

    private val httpTorrentFileCacheDir = cacheDirectory.resolve("torrentFiles").apply {
        mkdirs()
    }

    private fun getHttpTorrentFileCacheFile(uri: String): File {
        return httpTorrentFileCacheDir.resolve(uri.hashCode().toString() + ".txt")
    }

    private val downloadCacheDir = cacheDirectory.resolve("pieces").apply {
        mkdirs()
    }

    private val lock = Mutex()

    override suspend fun startDownload(
        data: EncodedTorrentInfo,
        parentCoroutineContext: CoroutineContext,
        overrideSaveDir: File?
    ): TorrentDownloadSession {
        val info = AnitorrentTorrentInfo.decodeFrom(data)
        val saveDir = overrideSaveDir ?: getSaveDirForTorrent(data)

        openSessions[data.data.contentHashCode().toString()]?.let {
            logger.info { "Found existing session" }
            return it
        }

        val handle = torrent_handle_t()
        val addInfo = torrent_add_info_t()
        when (info.data) {
            is AnitorrentTorrentData.MagnetUri -> {
                addInfo.kind = torrent_add_info_t.kKindMagnetUri
                addInfo.magnetUri = info.data.uri
                logger.info { "Using magnetUri. length=${info.data.uri.length}" }
            }

            is AnitorrentTorrentData.TorrentFile -> {
                addInfo.kind = torrent_add_info_t.kKindTorrentFile
                withContext(Dispatchers.IO) {
                    val tempFile = kotlin.io.path.createTempFile("anitorrent", ".torrent").toFile()
                    tempFile.writeBytes(info.data.data)
                    addInfo.torrentFilePath = tempFile.absolutePath
                }
                logger.info { "Using torrent file. data length=${info.data.data.size}" }
            }
        }
        check(addInfo.kind != torrent_add_info_t.kKindUnset)

        logger.info { "start_download: call native start_download" }
        if (!session.start_download(handle, addInfo, saveDir.absolutePath)) {
            throw IllegalStateException("Failed to start download, native failed")
        }
        logger.info { "start_download: native returned, handleId=${handle.id}" }
        session.resume()
        logger.info { "start_download: resumed" }
        return AnitorrentDownloadSession(
            referenceHolder = {
                eventListener
            },
            this.session, handle,
            saveDir,
//            onClose = { session ->
//                session as AnitorrentDownloadSession
//                dataToSession.remove(data.data.contentHashCode().toString())
//                anitorrent.session_release_handle(session.session, session.handle)
//            },
//            onDelete = { session ->
//                session as AnitorrentDownloadSession
//                dataToSession.remove(data.data.contentHashCode().toString())
//                anitorrent.session_release_handle(session.session, session.handle)
//            },
            parentCoroutineContext = parentCoroutineContext,
        ).also {
            logger.info { "AnitorrentDownloadSession created, saving to openSessions" }
            openSessions[data.data.contentHashCode().toString()] = it
        }
    }

    override fun getSaveDirForTorrent(data: EncodedTorrentInfo): File =
        downloadCacheDir.resolve(data.data.contentHashCode().toString())

    override fun listSaves(): List<File> {
        return downloadCacheDir.listFiles()?.toList() ?: emptyList()
    }

    override fun close() {
        logger.info { "AnitorrentDownloadSession closing" }
        scope.cancel()
        httpFileDownloader.close()
    }
}

class AnitorrentEventListener(
    private val downloader: AnitorrentTorrentDownloader,
) : event_listener_t() {
    override fun on_metadata_received(event: metadata_received_event_t?) {
        event ?: return
        try {
            downloader.forEachSession(event.handle_id) {
                it.handleEvent(event)
            }
        } catch (e: Throwable) {
            AnitorrentTorrentDownloader.logger.error(e) { "Error while handling event: $event" }
        }
    }

    override fun on_checked(handleId: Long) {
        try {
            downloader.forEachSession(handleId) {
                it.onTorrentChecked()
            }
        } catch (e: Throwable) {
            AnitorrentTorrentDownloader.logger.error(e) { "Error while handling on_checked" }
        }
    }

    override fun on_block_downloading(handleId: Long, pieceIndex: Int, blockIndex: Int) {
        try {
            downloader.forEachSession(handleId) {
                it.onPieceDownloading(pieceIndex)
            }
        } catch (e: Throwable) {
            AnitorrentTorrentDownloader.logger.error(e) { "Error while handling on_block_downloading" }
        }
    }

    override fun on_piece_finished(handleId: Long, pieceIndex: Int) {
        try {
            downloader.forEachSession(handleId) {
                it.onPieceFinished(pieceIndex)
            }
        } catch (e: Throwable) {
            AnitorrentTorrentDownloader.logger.error(e) { "Error while handling on_piece_finished" }
        }
    }

    override fun on_torrent_state_changed(handleId: Long, state: torrent_state_t?) {
        state ?: return
        try {
            downloader.forEachSession(handleId) {
                if (state == torrent_state_t.finished) {
                    it.onTorrentFinished()
                }
            }
        } catch (e: Throwable) {
            AnitorrentTorrentDownloader.logger.error(e) { "Error while handling on_torrent_state_changed" }
        }
    }

    override fun on_status_update(handleId: Long, stats: torrent_stats_t?) {
        stats ?: return
    }
}


typealias HandleId = Long

