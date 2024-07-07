package me.him188.ani.app.torrent.anitorrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.him188.ani.app.torrent.anitorrent.binding.event_listener_t
import me.him188.ani.app.torrent.anitorrent.binding.session_settings_t
import me.him188.ani.app.torrent.anitorrent.binding.session_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_add_info_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_handle_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_resume_data_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_state_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_stats_t
import me.him188.ani.app.torrent.api.HttpFileDownloader
import me.him188.ani.app.torrent.api.TorrentDownloadSession
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.app.torrent.api.TorrentDownloaderConfig
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

fun AnitorrentTorrentDownloader(
    rootDataDirectory: File,
    httpFileDownloader: HttpFileDownloader,
    torrentDownloaderConfig: TorrentDownloaderConfig,
    parentCoroutineContext: CoroutineContext,
): AnitorrentTorrentDownloader {
    AnitorrentLibraryLoader.loadLibraries()

    val session = session_t()
    val settings = session_settings_t().apply {
        // TODO: support more torrent settings (e.g. download speed limit)
        user_agent = torrentDownloaderConfig.userAgent
        peer_fingerprint = torrentDownloaderConfig.peerFingerprint
        handshake_client_version = torrentDownloaderConfig.handshakeClientVersion
        listOf(
            "router.utorrent.com:6881",
            "router.bittorrent.com:6881",
            "dht.transmissionbt.com:6881",
            "router.bitcomet.com:6881",
        ).forEach {
            dht_bootstrap_nodes_extra_add(it)
        }
    }
    try {
        session.start(settings)
    } finally {
        settings.delete() // 其实也可以等 GC, 不过反正我们都不用了
    }
    return AnitorrentTorrentDownloader(
        rootDataDirectory = rootDataDirectory,
        nativeSession = session,
        httpFileDownloader = httpFileDownloader,
        parentCoroutineContext = parentCoroutineContext,
    )
}

class AnitorrentTorrentDownloader(
    /**
     * 目录结构:
     * ```
     * rootDataDirectory
     *  |- torrentFiles
     *      |- <uri hash>.txt
     *  |- pieces
     *      |- <uri hash>
     *          |- [libtorrent save files]
     *          |- fastresume
     * ```
     *
     * 其中 uri hash 可能是 magnet URI 的 hash, 也可能是 HTTP URL 的 hash, 取决于 [startDownload] 时提供的是什么.
     */
    rootDataDirectory: File,
    val nativeSession: session_t, // must hold reference. 
    private val httpFileDownloader: HttpFileDownloader,
    parentCoroutineContext: CoroutineContext,
) : TorrentDownloader {
    companion object {
        private const val FAST_RESUME_FILENAME = "fastresume"
        private val logger = logger(this::class)
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


    // must keep referenced
    private val eventListener = object : event_listener_t() {

        override fun on_save_resume_data(handleId: Long, data: torrent_resume_data_t?) {
            data ?: return
            try {
                forEachSession(handleId) {
                    it.onSaveResumeData(data)
                }
            } catch (e: Throwable) {
                logger.error(e) { "Error while handling on_save_resume_data" }
            }
        }

        override fun on_checked(handleId: Long) {
            try {
                forEachSession(handleId) {
                    it.onTorrentChecked()
                }
            } catch (e: Throwable) {
                logger.error(e) { "Error while handling on_checked" }
            }
        }

        override fun on_block_downloading(handleId: Long, pieceIndex: Int, blockIndex: Int) {
            try {
                forEachSession(handleId) {
                    it.onPieceDownloading(pieceIndex)
                }
            } catch (e: Throwable) {
                logger.error(e) { "Error while handling on_block_downloading" }
            }
        }

        override fun on_piece_finished(handleId: Long, pieceIndex: Int) {
            try {
                forEachSession(handleId) {
                    it.onPieceFinished(pieceIndex)
                }
            } catch (e: Throwable) {
                logger.error(e) { "Error while handling on_piece_finished" }
            }
        }

        override fun on_torrent_state_changed(handleId: Long, state: torrent_state_t?) {
            state ?: return
            try {
                forEachSession(handleId) {
                    if (state == torrent_state_t.finished) {
                        it.onTorrentFinished()
                    }
                }
            } catch (e: Throwable) {
                logger.error(e) { "Error while handling on_torrent_state_changed" }
            }
        }

        override fun on_status_update(handleId: Long, stats: torrent_stats_t?) {
            stats ?: return
            try {
                forEachSession(handleId) {
                    it.onStatsUpdate(stats)
                }
            } catch (e: Throwable) {
                logger.error(e) { "Error while handling on_torrent_state_changed" }
            }
        }
    }


    init {
        scope.launch {
            while (true) {
                // TODO: stats 
//                val stats = sessionManager.getStats()
//                totalUploaded.value = stats.totalUpload()
//                totalDownloaded.value = stats.totalDownload()
//                totalUploadRate.value = stats.uploadRate()
//                totalDownloadRate.value = stats.downloadRate()
                nativeSession.process_events(eventListener)
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
                return AnitorrentTorrentInfo.encode(
                    AnitorrentTorrentInfo(
                        AnitorrentTorrentData.TorrentFile(data),
                        httpTorrentFilePath = cacheFile.absolutePath,
                    ),
                )
            }
            logger.info { "Fetching http url: $uri" }
            val data = httpFileDownloader.download(uri)
            logger.info { "Fetching http url success, file length = ${data.size}" }
            cacheFile.writeText(data.toHexString())
            logger.info { "Saved cache file: $cacheFile" }
            return AnitorrentTorrentInfo.encode(
                AnitorrentTorrentInfo(
                    AnitorrentTorrentData.TorrentFile(data),
                    httpTorrentFilePath = cacheFile.absolutePath,
                ),
            )
        }

        require(uri.startsWith("magnet")) { "Expected uri to start with \"magnet\": $uri" }
        return AnitorrentTorrentInfo.encode(
            AnitorrentTorrentInfo(
                AnitorrentTorrentData.MagnetUri(uri),
                httpTorrentFilePath = null,
            ),
        )
    }

    private val httpTorrentFileCacheDir = rootDataDirectory.resolve("torrentFiles").apply {
        mkdirs()
    }

    private fun getHttpTorrentFileCacheFile(uri: String): File {
        return httpTorrentFileCacheDir.resolve(uri.hashCode().toString() + ".txt")
    }

    private val downloadCacheDir = rootDataDirectory.resolve("pieces").apply {
        mkdirs()
    }

    private val sessionsLock = Mutex()

    override suspend fun startDownload(
        data: EncodedTorrentInfo,
        parentCoroutineContext: CoroutineContext,
        overrideSaveDir: File?
    ): TorrentDownloadSession = sessionsLock.withLock {
        val info = AnitorrentTorrentInfo.decodeFrom(data)
        val saveDir = overrideSaveDir ?: getSaveDirForTorrent(data)
        val fastResumeFile = saveDir.resolve(FAST_RESUME_FILENAME)

        openSessions[data.data.contentHashCode().toString()]?.let {
            logger.info { "Found existing session" }
            return it
        }

        val handle = torrent_handle_t()
        val addInfo = torrent_add_info_t()
        when (info.data) {
            is AnitorrentTorrentData.MagnetUri -> {
                addInfo.kind = torrent_add_info_t.kKindMagnetUri
                addInfo.magnet_uri = info.data.uri
                logger.info { "Using magnetUri. length=${info.data.uri.length}" }
            }

            is AnitorrentTorrentData.TorrentFile -> {
                addInfo.kind = torrent_add_info_t.kKindTorrentFile
                withContext(Dispatchers.IO) {
                    val tempFile = kotlin.io.path.createTempFile("anitorrent", ".torrent").toFile()
                    tempFile.writeBytes(info.data.data)
                    addInfo.torrent_file_path = tempFile.absolutePath
                }
                logger.info { "Using torrent file. data length=${info.data.data.size}" }
            }
        }
        check(addInfo.kind != torrent_add_info_t.kKindUnset)

        if (fastResumeFile.exists()) {
            logger.info { "start_download: including fastResumeFile" }
            addInfo.resume_data_path = fastResumeFile.absolutePath
        }

        if (!nativeSession.start_download(handle, addInfo, saveDir.absolutePath)) {
            throw IllegalStateException("Failed to start download, native failed")
        }
        return AnitorrentDownloadSession(
            this.nativeSession, handle,
            saveDir,
            fastResumeFile = fastResumeFile,
            onClose = {
                openSessions.remove(data.data.contentHashCode().toString())
                nativeSession.release_handle(handle)
            },
            onDelete = {
                scope.launch {
                    // http 下载的 .torrent 文件保存在全局路径, 需要删除
                    info.httpTorrentFilePath?.let(::File)?.let { cacheFile ->
                        withContext(Dispatchers.IO) {
                            if (cacheFile.exists()) {
                                cacheFile.delete()
                            }
                        }
                    }
                    // fast resume 保存在 saveDir 内, 已经被删除
                }
            },
            parentCoroutineContext = parentCoroutineContext,
        ).also {
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

private inline fun AnitorrentTorrentDownloader.forEachSession(
    id: HandleId,
    block: (AnitorrentDownloadSession) -> Unit
) {
    contract { callsInPlace(block, InvocationKind.UNKNOWN) }
    openSessions.values.forEach {
        if (it.id == id) {
            block(it)
        }
    }
}

typealias HandleId = Long

