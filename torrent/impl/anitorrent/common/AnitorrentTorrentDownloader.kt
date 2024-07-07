package me.him188.ani.app.torrent.anitorrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.him188.ani.app.torrent.anitorrent.binding.event_listener_t
import me.him188.ani.app.torrent.anitorrent.binding.new_event_listener_t
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
import me.him188.ani.app.torrent.libtorrent4j.trackers
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import java.io.File
import kotlin.coroutines.CoroutineContext

fun AnitorrentTorrentDownloader(
    rootDataDirectory: File,
    httpFileDownloader: HttpFileDownloader,
    torrentDownloaderConfig: TorrentDownloaderConfig,
    parentCoroutineContext: CoroutineContext,
): AnitorrentTorrentDownloader {
    AnitorrentLibraryLoader.loadLibraries()
    AnitorrentTorrentDownloader.logger.info { "Creating a new AnitorrentTorrentDownloader" }

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
    AnitorrentTorrentDownloader.logger.info { "AnitorrentTorrentDownloader created" }
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
        internal val logger = logger<AnitorrentTorrentDownloader>()
    }

    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))

    // key is uri hash
    // must be thread-safe
    val openSessions = MutableStateFlow<Map<String, AnitorrentDownloadSession>>(emptyMap())

    override val totalUploaded = openSessions.flatMapLatest { map ->
        combine(map.values.map { it.overallStats.uploadedBytes }) { it.sum() }
    }
    override val totalDownloaded = openSessions.flatMapLatest { map ->
        combine(map.values.map { it.overallStats.downloadedBytes }) { it.sum() }
    }
    override val totalUploadRate = openSessions.flatMapLatest { map ->
        combine(map.values.map { it.overallStats.uploadRate }) { it.sum() }
    }
    override val totalDownloadRate = openSessions.flatMapLatest { map ->
        combine(map.values.map { it.overallStats.downloadRate }) { it.sum() }
    }

    override val vendor: TorrentLibInfo = TorrentLibInfo(
        vendor = "Anitorrent",
        version = "1.0.0",
        supportsStreaming = true,
    )

    /**
     * 在 [startDownload] 时初始化, 用于缓存在调用 native startDownload 后, 到 [openSessions] 更新之前的事件.
     * 否则将会丢失事件.
     */
    private var handleTaskBuffer: DisposableTaskQueue<AnitorrentTorrentDownloader>? = null

    private val eventListener = object : event_listener_t() {

        /**
         * Note: can be called concurrently,
         * from [withHandleTaskQueue] or [newEventListener]
         */
        private inline fun AnitorrentTorrentDownloader.dispatchToSession(
            id: HandleId,
            crossinline block: (AnitorrentDownloadSession) -> Unit // will be inlined twice, for good amortized performance
        ): Unit = synchronized(this) {
            // contention is very low in most cases, except for when we are creating a new session.

            try {
                openSessions.value.values.find { it.handleId == id }?.let {
                    block(it)
                    return
                }
                // 这个 handle 仍然在创建中, 需要缓存 block, 延迟执行

                val handleTaskBuffer = handleTaskBuffer
                if (handleTaskBuffer == null) {
                    logger.warn {
                        "Session not found for handleId $id while handleTaskBuffer is not set. We are missing event"
                    }
                    return
                }
                handleTaskBuffer.add {
                    // this block does not capture anything

                    // Now we should have the session since the startDownload is locked
                    openSessions.value.values.find { it.handleId == id }?.let {
                        block(it)
                        return@add
                    }
                    logger.warn { "A delayed task failed to find session on execute. handleId=$id" }
                }
            } catch (e: Throwable) {
                logger.error(e) { "Error while handling event" }
            }
        }

        override fun on_save_resume_data(handleId: Long, data: torrent_resume_data_t?) {
            data ?: return
            dispatchToSession(handleId) {
                it.onSaveResumeData(data)
            }
        }

        override fun on_checked(handleId: Long) {
            dispatchToSession(handleId) {
                it.onTorrentChecked()
            }
        }

        override fun on_block_downloading(handleId: Long, pieceIndex: Int, blockIndex: Int) {
            dispatchToSession(handleId) {
                it.onPieceDownloading(pieceIndex)
            }
        }

        override fun on_piece_finished(handleId: Long, pieceIndex: Int) {
            dispatchToSession(handleId) {
                it.onPieceFinished(pieceIndex)
            }
        }

        override fun on_torrent_state_changed(handleId: Long, state: torrent_state_t?) {
            state ?: return
            dispatchToSession(handleId) {
                if (state == torrent_state_t.finished) {
                    it.onTorrentFinished()
                }
            }
        }

        override fun on_status_update(handleId: Long, stats: torrent_stats_t?) {
            stats ?: return
            dispatchToSession(handleId) {
                it.onStatsUpdate(stats)
            }
        }

        override fun on_file_completed(handleId: Long, fileIndex: Int) {
            dispatchToSession(handleId) {
                it.onFileCompleted(fileIndex)
            }
        }
    }

    private val eventSignal = Channel<Unit>(1)

    // must keep referenced
    private val newEventListener = object : new_event_listener_t() {
        override fun on_new_events() {
            // 根据 libtorrent 文档, 这里面不能处理事件
            eventSignal.trySend(Unit)
        }
    }

    init {
        nativeSession.set_new_event_listener(newEventListener)
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                eventSignal.receive() // await new events
                nativeSession.process_events(eventListener) // can block thread
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

    private suspend inline fun <R> withHandleTaskQueue(crossinline block: suspend () -> R): R =
        sessionsLock.withLock { // 必须只能同时有一个任务在添加. see eventListener

            val queue = DisposableTaskQueue(this)
            check(handleTaskBuffer == null) { "handleTaskBuffer is not null" }
            handleTaskBuffer = queue
            return kotlin.runCatching { block() }
                .also {
                    check(handleTaskBuffer == queue) {
                        "handleTaskBuffer changed while executing block"
                    }
                }
                .onSuccess {
                    val size = queue.runAndDispose()
                    logger.info { "withHandleTaskQueue: executed $size delayed tasks" }
                    this.handleTaskBuffer = null
                }
                .onFailure {
                    // drop all queued tasks
                    this.handleTaskBuffer = null
                }
                .getOrThrow() // rethrow exception
        }

    override suspend fun startDownload(
        data: EncodedTorrentInfo,
        parentCoroutineContext: CoroutineContext,
        overrideSaveDir: File?
    ): TorrentDownloadSession = withHandleTaskQueue {
        // 这个函数的 native 部分跑得也都很快, 整个函数十几毫秒就可以跑完, 所以 lock 也不会影响性能 (刚启动时需要尽快恢复 resume)

        val info = AnitorrentTorrentInfo.decodeFrom(data)
        val saveDir = overrideSaveDir ?: getSaveDirForTorrent(data)
        val fastResumeFile = saveDir.resolve(FAST_RESUME_FILENAME)

        openSessions.value[data.data.contentHashCode().toString()]?.let {
            logger.info { "Found existing session" }
            return@withHandleTaskQueue it
        }

        val handle = torrent_handle_t()
        val addInfo = torrent_add_info_t()
        when (info.data) {
            is AnitorrentTorrentData.MagnetUri -> {
                addInfo.kind = torrent_add_info_t.kKindMagnetUri
                addInfo.magnet_uri = info.data.uri
                logger.info { "Creating a session using magnetUri. length=${info.data.uri.length}" }
            }

            is AnitorrentTorrentData.TorrentFile -> {
                addInfo.kind = torrent_add_info_t.kKindTorrentFile
                withContext(Dispatchers.IO) {
                    val tempFile = kotlin.io.path.createTempFile("anitorrent", ".torrent").toFile()
                    tempFile.writeBytes(info.data.data)
                    addInfo.torrent_file_path = tempFile.absolutePath
                }
                logger.info { "Creating a session using torrent file. data length=${info.data.data.size}" }
            }
        }
        check(addInfo.kind != torrent_add_info_t.kKindUnset)

        if (fastResumeFile.exists()) {
            logger.info { "start_download: including fastResumeFile: ${fastResumeFile.absolutePath}" }
            addInfo.resume_data_path = fastResumeFile.absolutePath
        }

        // start_download 之后它就会开始发射 event
        if (!nativeSession.start_download(handle, addInfo, saveDir.absolutePath)) {
            throw IllegalStateException("Failed to start download, native failed")
        }

        return@withHandleTaskQueue AnitorrentDownloadSession(
            this.nativeSession, handle,
            saveDir,
            fastResumeFile = fastResumeFile,
            onClose = {
                openSessions.value -= data.data.contentHashCode().toString()
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
            openSessions.value += data.data.contentHashCode().toString() to it // 放进去之后才能处理 alert
            val trackers = trackers.split(", ")
            logger.info { "[${it.handleId}] AnitorrentDownloadSession created, adding ${trackers.size} trackers" }
            for (tracker in trackers) {
                handle.add_tracker(tracker, 0, 0)
            }
            nativeSession.resume()
        }
    }

    override fun getSaveDirForTorrent(data: EncodedTorrentInfo): File =
        downloadCacheDir.resolve(data.data.contentHashCode().toString())

    override fun listSaves(): List<File> {
        return downloadCacheDir.listFiles()?.toList() ?: emptyList()
    }

    override fun close() {
        nativeSession.remove_listener() // must remove, before gc-ing this object
        logger.info { "AnitorrentDownloadSession closing" }
        scope.cancel()
        httpFileDownloader.close()
    }
}

typealias HandleId = Long

