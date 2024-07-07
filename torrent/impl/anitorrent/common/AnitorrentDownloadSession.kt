package me.him188.ani.app.torrent.anitorrent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.torrent.anitorrent.AnitorrentDownloadSession.Entry.EntryHandle
import me.him188.ani.app.torrent.anitorrent.binding.session_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_handle_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_info_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_resume_data_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_stats_t
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
import me.him188.ani.app.torrent.api.pieces.PiecePriorities
import me.him188.ani.app.torrent.api.pieces.TorrentDownloadController
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
    val fastResumeFile: File,
    private val onClose: (AnitorrentDownloadSession) -> Unit,
    private val onDelete: (AnitorrentDownloadSession) -> Unit,
    parentCoroutineContext: CoroutineContext,
) : TorrentDownloadSession {
    val logger = logger(this::class)
    val id = handle.id

    private val scope =
        CoroutineScope(
            parentCoroutineContext + Dispatchers.IO + SupervisorJob(parentCoroutineContext[Job]),
        )

    init {
        scope.launch {
            while (isActive) {
                if (!handle.is_valid) {
                    return@launch
                }
                handle.post_status_updates()
                delay(1000)
            }
        }
    }

    override val state: MutableStateFlow<TorrentDownloadState> =
        MutableStateFlow(TorrentDownloadState.Starting)

    override val overallStats: MutableDownloadStats = MutableDownloadStats()

    private val openFiles = mutableListOf<EntryHandle>()

    inner class Entry(
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
        val pieceRange = if (pieces.isEmpty()) LongRange.EMPTY else pieces.first().startIndex..pieces.last().lastIndex

        val controller: TorrentDownloadController = TorrentDownloadController(
            pieces,
            createPiecePriorities(),
            windowSize = (2048 * 1024 / (pieces.firstOrNull()?.size ?: 1024L)).toInt().coerceIn(1, 64),
            headerSize = 1024 * 1024,
        )

        inner class EntryHandle : AbstractTorrentFileHandle() {
            override val entry get() = this@Entry

            override fun closeImpl() {
                openFiles.remove(this)
                closeIfNotInUse()
            }

            override fun resumeImpl(priority: FilePriority) {
                controller.onTorrentResumed()
                handle.resume()
            }

            override fun closeAndDelete() {
                close()
                deleteEntireTorrentIfNotInUse()
            }
        }

        override fun updatePriority() {
            handle.set_file_priority(index, FilePriority.NORMAL.toLibtorrentValue().toShort())
        }

        val downloadedBytes: MutableStateFlow<Long> = MutableStateFlow(initialDownloadedBytes)
        override val stats: DownloadStats = object : DownloadStats() {
            override val totalBytes: MutableStateFlow<Long> = MutableStateFlow(length)
            override val downloadedBytes: MutableStateFlow<Long> get() = this@Entry.downloadedBytes
            override val uploadRate: MutableStateFlow<Long?> get() = this@AnitorrentDownloadSession.overallStats.uploadRate
            override val progress: Flow<Float> =
                combine(totalBytes, downloadedBytes) { total, downloaded ->
                    if (total == 0L) return@combine 0f
                    downloaded.toFloat() / total.toFloat()
                }
        }

        override fun createHandle(): TorrentFileHandle {
            return EntryHandle().also {
                openFiles.add(it)
            }
        }

        override fun createInput(): SeekableInput {
            val input = (resolveFileOrNull() ?: runBlocking { resolveDownloadingFile() })
            return TorrentInput(
                RandomAccessFile(input, "r"),
                this.pieces,
                logicalStartOffset = offset,
                onWait = { piece ->
                    logger.info { "[TorrentDownloadControl] $torrentName: Set piece ${piece.pieceIndex} deadline to 0 because it was requested " }
                    updatePieceDeadlinesForSeek(piece, this.pieces)
                },
                size = length,
            )
        }
    }

    private fun updatePieceDeadlinesForSeek(
        requested: Piece,
        pieces: List<Piece>
    ) {
        handle.clear_piece_deadlines()
        handle.set_piece_deadline(requested.pieceIndex, 0) // 最高优先级
        for (i in (requested.pieceIndex + 1..requested.pieceIndex + 3)) {
            if (i < pieces.size - 1) {
                handle.set_piece_deadline(
                    // 按请求时间的优先
                    i,
                    calculatePieceDeadlineByTime(i),
                )
            }
        }
    }

    /**
     * 延迟获取的具体文件信息. 因为如果是添加磁力链的话, 文件信息需要经过耗时的网络查询后才能得到.
     * @see useTorrentInfoOrLaunch
     */
    inner class TorrentInfo(
        val allPiecesInTorrent: List<Piece>,
        val pieceLength: Int,
        val entries: List<Entry>,
    ) {
        init {
            check(allPiecesInTorrent is RandomAccess)
        }

        override fun toString(): String {
            return "TorrentInfo(numPieces=${allPiecesInTorrent.size}, entries.size=${entries.size})"
        }
    }

    private val actualTorrentInfo = CompletableDeferred<TorrentInfo>()

    // 回调可能会早于 [actualTorrentInfo] 计算完成之前调用, 所以需要考虑延迟的情况
    private inline fun useTorrentInfoOrLaunch(crossinline block: (TorrentInfo) -> Unit) {
        if (actualTorrentInfo.isCompleted) {
            actualTorrentInfo.getCompleted().let(block)
        } else {
            scope.launch {
                actualTorrentInfo.await().let(block)
            }
        }
    }

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

        val entries: List<Entry> = kotlin.run {
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
                Entry(
                    index = index,
                    offset = currentOffset,
                    length = size,
                    relativePath = path,
                    saveDirectory = saveDirectory,
                    pieces = filePieces,
                    torrentName = file.path,
                    isDebug = false,
                    parentCoroutineContext = Dispatchers.IO,
                    initialDownloadedBytes = calculateTotalFinishedSize(filePieces).coerceAtMost(size),
                ).also {
                    currentOffset += size
                }
            }
            list
        }
        val value = TorrentInfo(allPiecesInTorrent, pieceLength = info.piece_length, entries)
        logger.info { "[$id] Got torrent info: $value" }
        this.overallStats.totalBytes.value = entries.sumOf { it.length }
//        handle.ignore_all_files() // no need because we set libtorrent::torrent_flags::default_dont_download in native
        this.actualTorrentInfo.complete(value)
    }

    fun onTorrentChecked() {
        logger.info { "[$id] onTorrentChecked" }
        val res = handle.reload_file()
        if (res != torrent_handle_t.reload_file_result_t.kReloadFileSuccess) {
            logger.error { "[$id] Reload file result: $res" }
            throw IllegalStateException("Failed to reload file, native returned $res")
        }
        val info = handle.get_info_view()
        if (info != null) {
            initializeTorrentInfo(info)
        } else {
            logger.error { "[$id] onTorrentChecked: info is null" }
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
            for (file in openFiles) {
                if (pieceIndex in file.entry.pieceRange) {
                    file.entry.controller.onPieceDownloaded(pieceIndex)
                }
            }
            // TODO: Anitorrent 计算 file 完成度可以优化性能 
            info.entries.forEach { entry ->
                entry.downloadedBytes.value = calculateTotalFinishedSize(entry.pieces).coerceAtMost(entry.length)
            }
        }
    }

    fun onTorrentFinished() {
        logger.info { "[$id] onTorrentFinished" }
        useTorrentInfoOrLaunch { info ->
            info.allPiecesInTorrent.forEach { piece ->
                piece.state.value = PieceState.FINISHED
            }
            for (entry in info.entries) {
                entry.downloadedBytes.value = entry.length
            }
            this.overallStats.totalBytes.value = info.entries.sumOf { it.length }
            this.overallStats.progress.value = 1f
        }
        handle.post_save_resume()
    }

    fun onStatsUpdate(stats: torrent_stats_t) {
        this.overallStats.downloadRate.value = stats.download_payload_rate.toUInt().toLong()
        this.overallStats.uploadRate.value = stats.upload_payload_rate.toUInt().toLong()
        this.overallStats.progress.value = stats.progress
        this.overallStats.downloadedBytes.value = (this.overallStats.totalBytes.value * stats.progress).toLong()
        this.overallStats.isFinished.value = stats.progress >= 1f
    }

    fun onSaveResumeData(data: torrent_resume_data_t) {
        logger.info { "[$id] saving resume data to: ${fastResumeFile.absolutePath}" }
        data.save_to_file(fastResumeFile.absolutePath)
    }

    override suspend fun getFiles(): List<TorrentFileEntry> = this.actualTorrentInfo.await().entries

    private var closed = false
    override fun close() {
        if (closed) {
            return
        }
        synchronized(this) {
            if (closed) {
                return
            }
            state.value = TorrentDownloadState.Closed
            closed = true
        }
        logger.info { "AnitorrentDownloadSession closing" }
        scope.cancel()
        onClose(this)
    }

    override fun closeIfNotInUse() {
        if (openFiles.isEmpty()) {
            close()
        }
    }

    fun deleteEntireTorrentIfNotInUse() {
        if (openFiles.isEmpty() && closed) {
            saveDirectory.deleteRecursively()
            onDelete(this)
        }
    }

    private fun createPiecePriorities(): PiecePriorities {
        return object : PiecePriorities {
            //            private val priorities = Array(torrentFile().numPieces()) { Priority.IGNORE }
            private var lastPrioritizedIndexes: Collection<Int>? = null

            override fun downloadOnly(pieceIndexes: Collection<Int>) {
                if (pieceIndexes.isEmpty()) {
                    return
                }
                if (lastPrioritizedIndexes == pieceIndexes) {
                    return
                }
                logger.info { "[$id][TorrentDownloadControl] Prioritizing pieces: $pieceIndexes" }
                pieceIndexes.forEachIndexed { index, it ->
                    handle.set_piece_deadline(it, calculatePieceDeadlineByTime(index))
                }
                lastPrioritizedIndexes = pieceIndexes.toList()
            }
        }
    }

    private fun calculatePieceDeadlineByTime(
        shift: Int
    ): Int {
        return (((System.currentTimeMillis() / 1000).and(0x0FFF_FFFFL).toInt() % 1000_000) * 100 + shift).also {
            if (it < 0)
                logger.error { "[TorrentDownloadControl] $id: Calculated deadline for piece $shift: $it" }
        }
    }
}

class MutableDownloadStats : DownloadStats() {
    override val totalBytes: MutableStateFlow<Long> = MutableStateFlow(0)
    override val uploadRate: MutableStateFlow<Long?> = MutableStateFlow(0)
    override val downloadRate: MutableStateFlow<Long?> = MutableStateFlow(0)
    override val progress: MutableStateFlow<Float> = MutableStateFlow(0f)
    override val downloadedBytes: MutableStateFlow<Long> = MutableStateFlow(0)
    override val isFinished: MutableStateFlow<Boolean> = MutableStateFlow(false)
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

private fun calculateTotalFinishedSize(pieces: List<Piece>): Long =
    pieces.sumOf { if (it.state.value == PieceState.FINISHED) it.size else 0 }

private fun FilePriority.toLibtorrentValue(): Int = when (this) {
    FilePriority.IGNORE -> 0
    FilePriority.LOW -> 1
    FilePriority.NORMAL -> 4
    FilePriority.HIGH -> 7
}