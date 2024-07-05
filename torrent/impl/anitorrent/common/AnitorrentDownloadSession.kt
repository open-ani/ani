package me.him188.ani.app.torrent.anitorrent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.torrent.anitorrent.binding.event_t
import me.him188.ani.app.torrent.anitorrent.binding.metadata_received_event_t
import me.him188.ani.app.torrent.anitorrent.binding.session_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_handle_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_info_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_save_resume_data_event_t
import me.him188.ani.app.torrent.api.TorrentDownloadSession
import me.him188.ani.app.torrent.api.TorrentDownloadState
import me.him188.ani.app.torrent.api.files.AbstractTorrentFileEntry
import me.him188.ani.app.torrent.api.files.DownloadStats
import me.him188.ani.app.torrent.api.files.FilePriority
import me.him188.ani.app.torrent.api.files.PieceState
import me.him188.ani.app.torrent.api.files.TorrentFileEntry
import me.him188.ani.app.torrent.api.files.TorrentFileHandle
import me.him188.ani.app.torrent.api.files.TorrentFilePieceMatcher
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
import kotlin.coroutines.CoroutineContext

class AnitorrentDownloadSession(
    private val session: session_t,
    private val handle: torrent_handle_t,
    override val saveDirectory: File,
    parentCoroutineContext: CoroutineContext,
) : TorrentDownloadSession {
    val logger = logger(this::class)
    val id = handle.id

    private val scope =
        CoroutineScope(
            parentCoroutineContext + Dispatchers.IO + SupervisorJob(
                parentCoroutineContext[Job],
            ),
        )

    override val state: MutableStateFlow<TorrentDownloadState> =
        MutableStateFlow(TorrentDownloadState.Starting)
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
            override val downloadedBytes: MutableStateFlow<Long> =
                MutableStateFlow(initialDownloadedBytes)
            override val uploadRate: MutableStateFlow<Long?> = MutableStateFlow(0)
            override val progress: Flow<Float> =
                combine(totalBytes, downloadedBytes) { total, downloaded ->
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

        override fun toString(): String {
            return "TorrentInfo(numPieces=${allPiecesInTorrent.size}, entries.size=${entries.size})"
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
        check(this.actualTorrentInfo.isActive) {
            "actualTorrentInfo has already been completed or closed"
        }
        val allPiecesInTorrent =
            Piece.buildPieces(info.num_pieces) {
                if (it == info.num_pieces - 1) {
                    info.last_piece_size.toUInt().toLong()
                } else info.piece_length.toUInt().toLong()
            }

        val entries: List<AnitorrentFileEntry> = kotlin.run {
            val numFiles = info.fileSequence.toList()

            var currentOffset = 0L
            val list = numFiles.mapIndexed { index, file ->
                val size = file.size
                val path = file.path.takeIf { it.isNotBlank() } ?: file.name
                val list = TorrentFilePieceMatcher.matchPiecesForFile(
                    allPiecesInTorrent,
                    currentOffset,
                    size,
                ).also { pieces ->
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
                    torrentName = file.path,
                    isDebug = false,
                    parentCoroutineContext = Dispatchers.IO,
                    initialDownloadedBytes = calculateTotalFinishedSize(filePieces),
                ).also {
                    currentOffset += size
                }
            }
            list
        }
        val value = TorrentInfo(allPiecesInTorrent, entries)
        logger.info { "Got torrent info: $value" }
        this.actualTorrentInfo.complete(value)
    }

    fun handleEvent(event: event_t) {
        logger.info { "Handle event: $event" }
        when (event) {
            is torrent_save_resume_data_event_t -> {}
            else -> {}
        }
    }

    fun onTorrentChecked() {
        val res = handle.reload_file()
        if (res != torrent_handle_t.reload_file_result_t.kReloadFileSuccess) {
            logger.error { "[$id] Reload file result: $res" }
            throw IllegalStateException("Failed to reload file, native returned $res")
        }
        handle.get_info_view()?.let { info ->
            initializeTorrentInfo(info)
        }
    }

    fun onPieceDownloading(pieceIndex: Int) {
        useTorrentInfoOrLaunch { info ->
            info.allPiecesInTorrent.getOrNull(pieceIndex)?.state?.compareAndSet(
                PieceState.READY,
                PieceState.DOWNLOADING,
            )
        }
    }

    fun onPieceFinished(pieceIndex: Int) {
        useTorrentInfoOrLaunch { info ->
            info.allPiecesInTorrent.getOrNull(pieceIndex)?.state?.value = PieceState.FINISHED
        }
    }

    fun onTorrentFinished() {
        useTorrentInfoOrLaunch { info ->
            info.allPiecesInTorrent.forEach { piece ->
                piece.state.value = PieceState.FINISHED
            }
        }
    }

    override suspend fun getFiles(): List<TorrentFileEntry> = this.actualTorrentInfo.await().entries

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
