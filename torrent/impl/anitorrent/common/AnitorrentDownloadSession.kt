package me.him188.ani.app.torrent.anitorrent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.him188.ani.app.platform.Platform
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.app.torrent.anitorrent.binding.anitorrent
import me.him188.ani.app.torrent.anitorrent.binding.event_listener_t
import me.him188.ani.app.torrent.anitorrent.binding.event_t
import me.him188.ani.app.torrent.anitorrent.binding.metadata_received_event_t
import me.him188.ani.app.torrent.anitorrent.binding.session_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_add_event_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_finished_event_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_handle_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_info_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_save_resume_data_event_t
import me.him188.ani.app.torrent.api.FetchTorrentTimeoutException
import me.him188.ani.app.torrent.api.HttpFileDownloader
import me.him188.ani.app.torrent.api.TorrentDownloadSession
import me.him188.ani.app.torrent.api.TorrentDownloadState
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.app.torrent.api.TorrentLibInfo
import me.him188.ani.app.torrent.api.files.AbstractTorrentFileEntry
import me.him188.ani.app.torrent.api.files.DownloadStats
import me.him188.ani.app.torrent.api.files.EncodedTorrentInfo
import me.him188.ani.app.torrent.api.files.FilePriority
import me.him188.ani.app.torrent.api.files.PieceState
import me.him188.ani.app.torrent.api.files.TorrentFileEntry
import me.him188.ani.app.torrent.api.files.TorrentFileHandle
import me.him188.ani.app.torrent.api.files.TorrentFilePieceMatcher.matchPiecesForFile
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.api.pieces.lastIndex
import me.him188.ani.app.torrent.api.pieces.startIndex
import me.him188.ani.app.torrent.io.TorrentInput
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

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
    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))

    private val logger = logger(this::class)

    override val totalUploaded: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val totalDownloaded: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val totalUploadRate: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val totalDownloadRate: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val vendor: TorrentLibInfo = TorrentLibInfo(
        vendor = "Anitorrent",
        version = "1.0.0",
        supportsStreaming = true,
    )

    private val openSessions = ConcurrentHashMap<String, AnitorrentDownloadSession>()

    private inline fun forEachSession(id: HandleId, block: (AnitorrentDownloadSession) -> Unit) {
        openSessions.values.forEach {
            if (it.id == id) {
                block(it)
            }
        }
    }

    // must keep referenced
    private val eventListener = object : event_listener_t() {
        override fun on_event(event: event_t?) {
            if (event == null) return
            try {
                forEachSession(event.handle_id) {
                    it.handleEvent(event)
                }
            } catch (e: Throwable) {
                logger.error(e) { "Error while handling event: $event" }
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
    }

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

//        session.set_listener(eventListener)
        scope.launch(Dispatchers.IO) {
            delay(50)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun fetchTorrent(uri: String, timeoutSeconds: Int): EncodedTorrentInfo {
        if (uri.startsWith("http", ignoreCase = true)) {
            val cacheFile = getHttpTorrentFileCacheFile(uri)
            if (cacheFile.exists()) {
                val data = cacheFile.readText().hexToByteArray()
                logger.info { "HTTP torrent file '${uri}' found in cache: $cacheFile, length=${data.size}" }
                return AnitorrentTorrentInfo.encode(uri, data)
            }
            logger.info { "Fetching http url: $uri" }
            val data = httpFileDownloader.download(uri)
            logger.info { "Fetching http url success, file length = ${data.size}" }
            cacheFile.writeText(data.toHexString())
            logger.info { "Saved cache file: $cacheFile" }
            return AnitorrentTorrentInfo.encode(uri, data)
        }

        logger.info { "Fetching magnet: $uri" }
        val str: String = try {
            session.fetch_magnet(uri, timeoutSeconds, magnetCacheDir.absolutePath)
        } catch (e: InterruptedException) {
            throw FetchTorrentTimeoutException(cause = e)
        }
        val data = str.toByteArray()
        if (data.isEmpty()) {
            throw FetchTorrentTimeoutException()
        }
        logger.info { "Fetched magnet: size=${data.size}" }
        return AnitorrentTorrentInfo.encode(uri, data)
    }

    private val magnetCacheDir = cacheDirectory.resolve("magnet").apply {
        mkdirs()
    }

    private val httpTorrentFileCacheDir = cacheDirectory.resolve("torrentFiles").apply {
        mkdirs()
    }

    private fun getHttpTorrentFileCacheFile(uri: String): File {
        return httpTorrentFileCacheDir.resolve(uri.hashCode().toString() + ".txt")
    }

    private val downloadCacheDir = cacheDirectory.resolve("api/pieces").apply {
        mkdirs()
    }

    private val lock = Mutex()

    override suspend fun startDownload(
        data: EncodedTorrentInfo,
        parentCoroutineContext: CoroutineContext,
        overrideSaveDir: File?
    ): TorrentDownloadSession = lock.withLock {
        val info = AnitorrentTorrentInfo.decodeFrom(data)
            ?: throw IllegalArgumentException("Invalid torrent data, native failed to decode")
        val saveDir = overrideSaveDir ?: getSaveDirForTorrent(data)

        openSessions[data.data.contentHashCode().toString()]?.let {
            return it
        }

        val handle = torrent_handle_t()
        if (!session.start_download(handle, info.native, saveDir.absolutePath)) {
            throw IllegalStateException("Failed to start download, native failed")
        }
        return AnitorrentDownloadSession(
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
            openSessions[data.data.contentHashCode().toString()] = it
        }
    }

    override fun getSaveDirForTorrent(data: EncodedTorrentInfo): File =
        downloadCacheDir.resolve(data.data.contentHashCode().toString())

    override fun listSaves(): List<File> {
        return downloadCacheDir.listFiles()?.toList() ?: emptyList()
    }

    override fun close() {
        scope.cancel()
        httpFileDownloader.close()
    }
}

typealias HandleId = Long

class AnitorrentDownloadSession(
    private val session: session_t,
    private val handle: torrent_handle_t,
    override val saveDirectory: File,
    parentCoroutineContext: CoroutineContext,
) : TorrentDownloadSession {
    val logger = logger(this::class)
    val id = handle.id

    private val scope =
        CoroutineScope(parentCoroutineContext + Dispatchers.IO + SupervisorJob(parentCoroutineContext[Job]))

    override val state: MutableStateFlow<TorrentDownloadState> = MutableStateFlow(TorrentDownloadState.Starting)
    override val overallStats: DownloadStats = object : DownloadStats() {
        override val totalBytes: MutableStateFlow<Long> = MutableStateFlow(0)
        override val uploadRate: MutableStateFlow<Long?> = MutableStateFlow(0)
        override val progress: MutableStateFlow<Float> = MutableStateFlow(0f)
    }

    inner class AnitorrentFileEntry(
        override val pieces: List<Piece>,
        index: Int,
        val offset: Long,
        length: Long, saveDirectory: File, relativePath: String,
        torrentName: String, isDebug: Boolean, parentCoroutineContext: CoroutineContext,
        initialDownloadedBytes: Long,
    ) : AbstractTorrentFileEntry(
        index, length, saveDirectory, relativePath, torrentName, isDebug,
        parentCoroutineContext,
    ) {
        override val supportsStreaming: Boolean get() = true

        inner class TorrentFileHandleImpl : TorrentFileHandle {
            override val entry: TorrentFileEntry get() = this@AnitorrentFileEntry

            override fun resume(priority: FilePriority) {
            }

            override fun pause() {
            }

            override fun close() {
            }

            override fun closeAndDelete() {
            }
        }

        override fun updatePriority() {
        }

        override val stats: DownloadStats = object : DownloadStats() {
            override val totalBytes: MutableStateFlow<Long> = MutableStateFlow(length)
            override val downloadedBytes: MutableStateFlow<Long> = MutableStateFlow(initialDownloadedBytes)
            override val uploadRate: MutableStateFlow<Long?> = MutableStateFlow(0)
            override val progress: Flow<Float> = combine(totalBytes, downloadedBytes) { total, downloaded ->
                if (total == 0L) return@combine 0f
                downloaded.toFloat() / total.toFloat()
            }
        }

        private val openHandles = mutableListOf<TorrentFileHandleImpl>()

        override fun createHandle(): TorrentFileHandle {
            return TorrentFileHandleImpl().also {
                openHandles.add(it)
            }
        }

        override fun createInput(): SeekableInput {
            val input = (resolveFileOrNull() ?: runBlocking { resolveDownloadingFile() })
            val pieces = pieces
            return TorrentInput(
                RandomAccessFile(input, "r"),
                pieces,
                logicalStartOffset = offset,
                onWait = { piece ->
                    logger.info { "[TorrentDownloadControl] $torrentName: Set piece ${piece.pieceIndex} deadline to 0 because it was requested " }
//                    handle.setPieceDeadline(piece.pieceIndex, 0) // 最高优先级
//                    for (i in (piece.pieceIndex + 1..piece.pieceIndex + 3)) {
//                        if (i < pieces.size - 1) {
//                            handle.setPieceDeadline(
//                                // 按请求时间的优先
//                                i,
//                                calculatePieceDeadlineByTime(i),
//                            )
//                        }
//                    }
                },
                size = length,
            )
        }
    }

    class TorrentInfo(
        val allPiecesInTorrent: List<Piece>,
        val entries: List<AnitorrentFileEntry>,
    ) {
        init {
            check(allPiecesInTorrent is RandomAccess)
        }
    }

    private val actualTorrentInfo = CompletableDeferred<TorrentInfo>()
    private inline fun useTorrentInfoOrLaunch(crossinline block: (TorrentInfo) -> Unit) {
        if (actualTorrentInfo.isCompleted) {
            actualTorrentInfo.getCompleted().let(block)
        } else {
            scope.launch {
                actualTorrentInfo.await().let(block)
            }
        }
    }

    //TODO 必须在这个锁里计算, 因为 [onPieceDownloaded] 会 +updatedDownloadedBytes
    private fun calculateTotalFinishedSize(pieces: List<Piece>): Long =
        pieces.sumOf { if (it.state.value == PieceState.FINISHED) it.size else 0 }

    private fun initializeTorrentInfo(info: torrent_info_t) {
        val allPiecesInTorrent = Piece.buildPieces(info.num_pieces) { info.piece_length.toUInt().toLong() }

        val entries: List<AnitorrentFileEntry> = kotlin.run {
            val numFiles = info.fileSequence.toList()

            var currentOffset = 0L
            val list = numFiles.mapIndexed { index, file ->
                val size = file.size
                val path = file.root + "/" + file.name

                val list = matchPiecesForFile(allPiecesInTorrent, currentOffset, size).also { pieces ->
                    logPieces(pieces, path)
                }
                val filePieces = if (list is RandomAccess) {
                    list
                } else {
                    ArrayList(list)
                }
                AnitorrentFileEntry(
                    index = index,
                    offset = currentOffset,
                    length = size,
                    relativePath = path,
                    saveDirectory = saveDirectory,
                    pieces = filePieces,
                    torrentName = file.name,
                    isDebug = false,
                    parentCoroutineContext = Dispatchers.IO,
                    initialDownloadedBytes = calculateTotalFinishedSize(filePieces),
                ).also {
                    currentOffset += size
                }
            }
            list
        }
        this.actualTorrentInfo.complete(TorrentInfo(allPiecesInTorrent, entries))
    }

    fun handleEvent(event: event_t) {
        when (event) {
            is torrent_add_event_t -> {}
            is metadata_received_event_t -> {
                val res = handle.reload_file()
                handle.get_info_view()?.let { info ->

                }
                logger.info { "[$id] Reload file result: $res" }
            }

            is torrent_save_resume_data_event_t -> {}
            is torrent_finished_event_t -> {}
            else -> {}
        }
    }

    fun onPieceFinished(pieceIndex: Int) {
        useTorrentInfoOrLaunch { info ->
            info.allPiecesInTorrent.getOrNull(pieceIndex)?.state?.value = PieceState.FINISHED
        }
    }

    override suspend fun getFiles(): List<TorrentFileEntry> {
//        return anitorrent.get_files(handle).map {
//            TorrentFileEntry(
//                name = it.name,
//                size = it.size,
//                priority = it.priority,
//                progress = it.progress,
//            )
//        }
        return emptyList()
    }

    override fun close() {
        scope.cancel()
    }

    override fun closeIfNotInUse() {
    }

}

private fun AnitorrentDownloadSession.logPieces(pieces: List<Piece>, pathInTorrent: String) {
    logger.info {
        val start = pieces.minByOrNull { it.startIndex }
        val end = pieces.maxByOrNull { it.lastIndex }
        "[$id] File '$pathInTorrent' piece initialized, ${pieces.size} pieces, offset range: $start..$end"
    }
}

val torrent_info_t.fileSequence
    get() = sequence {
        repeat(file_count().toInt()) {
            val file = file_at(it) ?: return@sequence
            yield(file)
        }
    }
