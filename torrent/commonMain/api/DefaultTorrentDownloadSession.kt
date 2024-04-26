package me.him188.ani.app.torrent.api

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.him188.ani.app.torrent.api.TorrentFilePieceMatcher.matchPiecesForFile
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.api.pieces.PiecePriorities
import me.him188.ani.app.torrent.api.pieces.TorrentDownloadController
import me.him188.ani.app.torrent.api.pieces.lastIndex
import me.him188.ani.app.torrent.api.pieces.startIndex
import me.him188.ani.app.torrent.file.TorrentInput
import me.him188.ani.app.torrent.io.TorrentFileIO
import me.him188.ani.app.torrent.torrent4j.LockedSessionManager
import me.him188.ani.utils.coroutines.SuspendLazy
import me.him188.ani.utils.coroutines.flows.resetStale
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.io.asSeekableInput
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

internal open class DefaultTorrentDownloadSession(
    private val torrentName: String,
    /**
     * The directory where the torrent is saved.
     *
     * The directory may contain multiple files, or a single file.
     * The files are not guaranteed to be present at the moment when this function returns.
     */
    final override val saveDirectory: File,
    private val onClose: suspend (DefaultTorrentDownloadSession) -> Unit,
    private val isDebug: Boolean,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : TorrentDownloadSession {
    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))
    private val coroutineCloseHandle =
        parentCoroutineContext[Job]?.invokeOnCompletion {
            close()
        }

    private val logger = logger(this::class.simpleName + "@${this.hashCode()}")

    /**
     * 在 BT 线程执行
     */
    private val torrentThreadTasks = TaskQueue<AniTorrentHandle>(
        enableTimeoutWatchdog = isDebug,
    )

    final override val state: MutableStateFlow<TorrentDownloadState> = MutableStateFlow(TorrentDownloadState.Starting)

    inner class OverallStatsImpl : DownloadStats {
        override val totalBytes: MutableStateFlow<Long> = MutableStateFlow(0L)
        override val downloadedBytes = MutableStateFlow(0L)

        val downloadRate0 = MutableStateFlow<Long?>(null)
        override val downloadRate: Flow<Long?>
            get() = downloadRate0
                .resetStale(1000) {
                    emit(0L)
                }
                .distinctUntilChanged()

        val uploadRate0 = MutableStateFlow<Long?>(null)
        override val uploadRate
            get() = uploadRate0
                .resetStale(1000) {
                    emit(0L)
                }
                .distinctUntilChanged()

        override val progress = combine(downloadedBytes, totalBytes) { downloaded, total ->
            if (total == 0L) {
                0f
            } else {
                downloaded.toFloat() / total.toFloat()
            }
        }.distinctUntilChanged()
        override val peerCount: MutableStateFlow<Int> = MutableStateFlow(0)
        override val isFinished: Flow<Boolean> = flow {
            emitAll(combine(getFiles().map { it.stats.isFinished }) { list ->
                list.all { it }
            })
        }
        private val onFinish = CompletableDeferred(Unit)

        override suspend fun awaitFinished() = onFinish.await()
    }

    override val overallStats = OverallStatsImpl()
    override suspend fun getFiles(): List<TorrentFileEntry> = entries.get()

    private inner class ActualTorrentInfo(
        val pieces: List<Piece>,
        val files: List<TorrentFile>,
    ) {
        val controller: TorrentDownloadController = TorrentDownloadController(
            pieces,
            createPiecePriorities()
        )

        @Synchronized
        fun onPieceDownloaded(index: Int) {
            pieces[index].state.value = PieceState.FINISHED
            logger.debug { "[TorrentDownloadControl] Piece downloaded: $index. " } // Was downloading ${controller.getDebugInfo().downloadingPieces}
            controller.onPieceDownloaded(index)
        }

        @Synchronized
        fun onBlockDownloading(pieceIndex: Int) {
            pieces[pieceIndex].state.value = PieceState.DOWNLOADING
        }

        @Synchronized
        fun onFinished() {
            pieces.forEach {
                it.state.value = PieceState.FINISHED
            }
        }
    }

    private val entries = SuspendLazy {
        val files = actualInfo.await().files

        val numFiles = files.size

        var currentOffset = 0L
        val list = List(numFiles) { index ->
            val file = files[index]
            val size = file.size
            val path = file.path
            TorrentFileEntryImpl(
                index = index,
                offset = currentOffset,
                length = size,
                relativePath = path,
            ).also {
                currentOffset += size
            }
        }
        list
    }

    internal val openHandles = ConcurrentLinkedQueue<TorrentFileEntryImpl.TorrentFileHandleImpl>()

    internal inner class TorrentFileEntryImpl(
        val index: Int,
        val offset: Long,
        override val length: Long,
        val relativePath: String,
    ) : TorrentFileEntry {
        inner class TorrentFileHandleImpl : TorrentFileHandle {
            @Volatile
            private var closed = false
            private var closeException: Throwable? = null

            override fun close(): Unit = synchronized(this) {
                if (closed) return
                closed = true

                logger.info { "[$torrentName] Close file $pathInTorrent, set file priority to ignore" }
                removePriority()

                if (isDebug) {
                    closeException = Exception("Stacktrace for close()")
                }

                openHandles.remove(this)
                this@DefaultTorrentDownloadSession.closeIfNotInUse()
            }

            override suspend fun pause() {
                checkClosed()
                requestPriority(null)
            }

            private fun checkClosed() {
                if (closed) throw IllegalStateException(
                    "Attempting to pause but TorrentFile has already been closed: $pathInTorrent",
                    closeException
                )
            }

            override val entry get() = this@TorrentFileEntryImpl

            override suspend fun resume(priority: FilePriority) {
                checkClosed()

                val pieces = pieces.get()
                torrentThreadTasks.withHandle { handle ->
                    if (pieces.isNotEmpty()) {
                        val firstIndex = pieces.first().pieceIndex
                        val lastIndex = pieces.last().pieceIndex
                        handle.setPieceDeadline(firstIndex, 0)
                        handle.setPieceDeadline(lastIndex, 1)

                        if (firstIndex + 1 <= lastIndex) {
                            handle.setPieceDeadline(firstIndex + 1, 2)
                        }
                        if (firstIndex + 2 <= lastIndex) {
                            handle.setPieceDeadline(firstIndex + 2, 3)
                        }
                        println("setPieceDeadline ok")
                    }
                    handle.resume()
                }

                requestPriority(priority)
            }

            override fun toString(): String = "TorrentFileHandleImpl(index=$index, filePath='$pathInTorrent')"
        }

        /**
         * 与这个文件有关的 pieces, sorted naturally by offset
         */
        private val pieces: SuspendLazy<List<Piece>> = SuspendLazy {
            val allPieces = this@DefaultTorrentDownloadSession.actualInfo.await().pieces
            matchPiecesForFile(allPieces, offset, length).also { pieces ->
                logger.info {
                    val start = pieces.minByOrNull { it.startIndex }
                    val end = pieces.maxByOrNull { it.lastIndex }
                    "[$torrentName] File '$pathInTorrent' piece initialized, ${pieces.size} pieces, offset range: $start..$end"
                }
            }
        }


        val finishedOverride = MutableStateFlow(false)

        override val stats: DownloadStats = object : DownloadStats {
            override val totalBytes: Flow<Long> = flowOf(length)
            override val downloadedBytes: Flow<Long> = flow {
                emit(pieces.get())
            }.flatMapLatest { list ->
                combine(list.map { it.downloadedBytes }) {
                    it.sum()
                }
            }
            override val downloadRate: Flow<Long?> get() = overallStats.downloadRate
            override val uploadRate: Flow<Long?> get() = overallStats.uploadRate
            override val progress: Flow<Float> =
                combine(finishedOverride, downloadedBytes) { finished, downloadBytes ->
                    when {
                        finished -> 1f
                        length == 0L -> 0f
                        else -> (downloadBytes.toFloat() / length.toFloat()).coerceAtMost(1f)
                    }
                }
            override val isFinished: Flow<Boolean> = flow {
                emit(pieces.get())
            }.flatMapLatest { list ->
                combine(list.map { it.state }) {
                    it.all { state -> state == PieceState.FINISHED }
                }
            }
            override val peerCount: Flow<Int> get() = overallStats.peerCount

            override suspend fun awaitFinished() {
                isFinished.filter { it }.first()
            }
        }

        override val pathInTorrent: String get() = relativePath.substringAfter("/")

        private val priorityRequests: MutableMap<TorrentFileHandle, FilePriority?> = mutableMapOf()

        /**
         * `null` to ignore
         */
        private fun TorrentFileHandle.requestPriority(priority: FilePriority?) {
            priorityRequests[this] = priority
            updatePriority()
        }

        private fun TorrentFileHandle.removePriority() {
            priorityRequests.remove(this)
            updatePriority()
        }

        private fun updatePriority() {
            @OptIn(TorrentThread::class)
            torrentThreadTasks.submit { handle ->
                val highestPriority = priorityRequests.values.maxWithOrNull(nullsFirst(naturalOrder()))
                    ?: FilePriority.IGNORE
                handle.contents.files[index].priority = highestPriority
                logger.info { "[$torrentName] Set file $pathInTorrent priority to $highestPriority" }
                handle.resume()
            }
        }

        override suspend fun getPieces(): List<Piece> = pieces.get()

        override suspend fun createHandle(): TorrentFileHandle = TorrentFileHandleImpl().also {
            priorityRequests[it] = null
            openHandles.add(it)
        }

        override suspend fun resolveFile(): Path = resolveDownloadingFile().toPath()

        private val hashMd5 by lazy {
            scope.async {
                stats.awaitFinished()
                withContext(Dispatchers.IO) {
                    TorrentFileIO.hashFileMd5(resolveDownloadingFile())
                }
            }
        }

        override suspend fun getFileHash(): String = hashMd5.await()

        override fun getFileHashOrNull(): String? = if (hashMd5.isCompleted) {
            hashMd5.getCompleted()
        } else null

        private suspend fun resolveDownloadingFile(): File {
            while (true) {
                val file = withContext(Dispatchers.IO) { resolveFileOrNull() }
                if (file != null) {
                    logger.info { "$torrentName: Get file: ${file.absolutePath}" }
                    return file
                }
                logger.info { "$torrentName: Still waiting to get file... saveDirectory: $saveDirectory" }
                delay(1.seconds)
            }
            @Suppress("UNREACHABLE_CODE") // compiler bug
            error("")
        }

        @Throws(IOException::class)
        private fun resolveFileOrNull(): File? =
            saveDirectory.resolve(relativePath).takeIf { it.isFile }

        override suspend fun createInput(): SeekableInput {
            logger.info { "createInput: finding cache file" }
            val file = withContext(Dispatchers.IO) {
                RandomAccessFile(resolveDownloadingFile(), "r").asSeekableInput()
            }
            logger.info { "createInput: got cache file, awaiting pieces" }
            val pieces = pieces.get()
            logger.info { "createInput: ${pieces.size} pieces" }
            return TorrentInput(
                file,
                pieces,
                onSeek = { piece ->
                    logger.info { "[TorrentDownloadControl] $torrentName: Set piece ${piece.pieceIndex} deadline to 0 because it was requested " }
                    torrentThreadTasks.submit { handle ->
                        handle.setPieceDeadline(piece.pieceIndex, 0)
                        for (i in (piece.pieceIndex + 1..piece.pieceIndex + 3)) {
                            if (i < pieces.size - 1) {
                                handle.setPieceDeadline(
                                    i,
                                    System.currentTimeMillis().and(0x0FFF_FFFFL).toInt() + i
                                )
                            }
                        }
                    }
                }
            )
        }

        override fun toString(): String {
            return "TorrentFileEntryImpl(index=$index, offset=$offset, length=$length, relativePath='$relativePath')"
        }
    }

    /**
     * 通过磁力链解析的初始的信息可能是不准确的
     */
    private val actualInfo: CompletableDeferred<ActualTorrentInfo> = CompletableDeferred()

    private fun actualInfo(): ActualTorrentInfo = actualInfo.getCompleted()

    internal val listener = object : EventListener {
        @TorrentThread
        override fun onUpdate(handle: AniTorrentHandle) {
            torrentThreadTasks.invokeAll(handle)
        }

        override val torrentName: String
            get() = this@DefaultTorrentDownloadSession.torrentName

        @TorrentThread
        override fun onEvent(event: TorrentEvent) {
            when (event) {
                is TorrentAddEvent -> {
                    logger.info { "[$torrentName] Received alert: Torrent added" }
                    val torrentHandle = event.handle

                    // Add trackers
                    trackers.lines().map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                        torrentHandle.addTracker(it)
                    }

                    // Initialize [pieces]
                    // 注意, 必须在这里初始化获取 pieces, 通过磁力链解析的可能是不准确的
                    val contents = torrentHandle.contents
                    actualInfo.complete(ActualTorrentInfo(contents.createPieces(), contents.files))
                }

                is TorrentResumeEvent -> {
                    if (actualInfo.isCompleted) return
                    state.value = TorrentDownloadState.FetchingMetadata
                }

                // TODO: torrent peer stats 
//                is PeerConnectAlert -> {
//                    overallStats.peerCount.getAndUpdate { it + 1 }
//                }
//
//                is PeerDisconnectedAlert -> {
//                    overallStats.peerCount.getAndUpdate { it - 1 }
//                }

                is BlockDownloadingEvent -> {
                    val pieceIndex = event.pieceIndex
                    actualInfo().onBlockDownloading(pieceIndex)
                }

                is PieceFinishedEvent -> {
                    val pieceIndex = event.pieceIndex
                    actualInfo().onPieceDownloaded(pieceIndex)
                }

                is TorrentFinishedEvent -> {
                    // https://libtorrent.org/reference-Alerts.html#:~:text=report%20issue%5D-,torrent_finished_alert,-Declared%20in%20%22
                    logger.info { "[$torrentName] Torrent finished" }
                    for (openHandle in openHandles) {
                        logger.info { "[$torrentName] Set entry's finishedOverride to true because torrent finished: ${openHandle.entry.pathInTorrent}" }
                        openHandle.entry.finishedOverride.value = true
                    }
                }

//                is FileErrorAlert -> {
//                    logger.warn { "[libtorrent] $torrentName: File error: ${alert.operation()} ${alert.error()}" }
//                }
//
//                is MetadataFailedAlert -> {
//                    logger.warn { "[libtorrent] $torrentName: Metadata failed: ${alert.error.message}" }
//                }
                is StatsUpdateEvent -> {
                    overallStats.totalBytes.value = event.totalBytes
                    overallStats.downloadedBytes.value = event.downloadedBytes
                    overallStats.downloadRate0.value = event.downloadRate
                    overallStats.uploadRate0.value = event.uploadRate
                }
            }
        }
    }


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

        for (openHandle in openHandles) {
            logger.info { "Closing torrent: close handle $openHandle" }
            openHandle.close()
        }

        logger.info { "Closing torrent" }

        torrentThreadTasks.submit {
            runBlocking(LockedSessionManager.dispatcher) {
                onClose(this@DefaultTorrentDownloadSession)
                logger.info { "Close torrent $torrentName: dispose handle" }
                scope.cancel()
                coroutineCloseHandle?.dispose()
            }
        }
    }

    /*
     * 注意, 目前其实种子的 saveDirectory 不会被删除. close TorrentHandle 时只会删除它们自己对应的文件.
     */
    override fun closeIfNotInUse() {
        if (openHandles.isNotEmpty()) {
            close()
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
                logger.debug { "[TorrentDownloadControl] Prioritizing pieces: $pieceIndexes" }
                pieceIndexes.forEach { index ->
                    torrentThreadTasks.submit { handle ->
                        handle.setPieceDeadline(index, System.currentTimeMillis().and(0x0FFF_FFFFL).toInt())
                    }
                }
                lastPrioritizedIndexes = pieceIndexes.toList()
            }
        }
    }
}

private val trackers = """
udp://tracker1.itzmx.com:8080/announce
udp://moonburrow.club:6969/announce
udp://new-line.net:6969/announce
udp://opentracker.io:6969/announce
udp://tamas3.ynh.fr:6969/announce
udp://tracker.bittor.pw:1337/announce
udp://tracker.dump.cl:6969/announce
udp://tracker1.myporn.club:9337/announce
udp://tracker2.dler.org:80/announce
https://tracker.tamersunion.org:443/announce
udp://open.demonii.com:1337/announce
udp://open.stealth.si:80/announce
udp://tracker.torrent.eu.org:451/announce
udp://exodus.desync.com:6969/announce
udp://tracker.moeking.me:6969/announce
udp://explodie.org:6969/announce
udp://tracker1.bt.moack.co.kr:80/announce
udp://tracker.tiny-vps.com:6969/announce
udp://retracker01-msk-virt.corbina.net:80/announce
udp://bt1.archive.org:6969/announce

udp://tracker2.itzmx.com:6961/announce

udp://tracker3.itzmx.com:6961/announce

udp://tracker4.itzmx.com:2710/announce

http://tracker1.itzmx.com:8080/announce

http://tracker2.itzmx.com:6961/announce

http://tracker3.itzmx.com:6961/announce

http://tracker4.itzmx.com:2710/announce

udp://tracker.opentrackr.org:1337/announce

http://tracker.opentrackr.org:1337/announce
                        """.trimIndent().lineSequence().filter { it.isNotBlank() }.joinToString()