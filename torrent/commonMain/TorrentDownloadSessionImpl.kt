package me.him188.ani.app.torrent

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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import me.him188.ani.app.torrent.download.PiecePriorities
import me.him188.ani.app.torrent.download.TorrentDownloadController
import me.him188.ani.app.torrent.file.TorrentInput
import me.him188.ani.app.torrent.model.Piece
import me.him188.ani.app.torrent.model.lastIndex
import me.him188.ani.app.torrent.model.startIndex
import me.him188.ani.utils.coroutines.SuspendLazy
import me.him188.ani.utils.coroutines.cancellableCoroutineScope
import me.him188.ani.utils.coroutines.flows.resetStale
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.io.asSeekableInput
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import org.libtorrent4j.AnnounceEntry
import org.libtorrent4j.Priority
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.TorrentInfo
import org.libtorrent4j.alerts.AddTorrentAlert
import org.libtorrent4j.alerts.BlockDownloadingAlert
import org.libtorrent4j.alerts.BlockFinishedAlert
import org.libtorrent4j.alerts.FileErrorAlert
import org.libtorrent4j.alerts.MetadataFailedAlert
import org.libtorrent4j.alerts.PeerConnectAlert
import org.libtorrent4j.alerts.PeerDisconnectedAlert
import org.libtorrent4j.alerts.PieceFinishedAlert
import org.libtorrent4j.alerts.TorrentAlert
import org.libtorrent4j.alerts.TorrentFinishedAlert
import org.libtorrent4j.alerts.TorrentResumedAlert
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

/**
 * 封装对一个文件的下载
 */
internal class TorrentDownloadSessionImpl(
    private val torrentName: String,
    /**
     * The directory where the torrent is saved.
     *
     * The directory may contain multiple files, or a single file.
     * The files are not guaranteed to be present at the moment when this function returns.
     */
    private val saveDirectory: File,
    private val onClose: suspend (TorrentDownloadSessionImpl) -> Unit,
    private val isDebug: Boolean,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : TorrentDownloadSession {
    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))
    private val coroutineCloseHandle =
        parentCoroutineContext[Job]?.invokeOnCompletion {
            close()
        }

    private val logger = logger(this::class.simpleName + "@${this.hashCode()}")

    override val state: MutableStateFlow<TorrentDownloadState> = MutableStateFlow(TorrentDownloadState.Starting)

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
        override val isFinished: MutableStateFlow<Boolean> = MutableStateFlow(false)
        private val onFinish = CompletableDeferred(Unit)

        override suspend fun awaitFinished() = onFinish.await()
    }

    override val overallStats = OverallStatsImpl()
    override suspend fun getFiles(): List<TorrentFileEntry> = entries.get()

    private inner class ActualTorrentInfo(
        val pieces: List<Piece>,
        val torrentInfo: TorrentInfo,
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
        val files = actualInfo.await().torrentInfo.files()

        val numFiles = files.numFiles()

        var currentOffset = 0L
        val list = List(numFiles) { index ->
            val size = files.fileSize(index)
            val path = files.filePath(index)
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

    private val openHandles = ConcurrentLinkedQueue<TorrentFileEntryImpl.TorrentFileHandleImpl>()

    private inner class TorrentFileEntryImpl(
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

                logger.info { "[$torrentName] Close file $filePath, set file priority to ignore" }
                removePriority()

                if (isDebug) {
                    closeException = Exception("Stacktrace for close()")
                }

                openHandles.remove(this)
                this@TorrentDownloadSessionImpl.closeIfNotInUse()
            }

            override suspend fun pause() {
                checkClosed()
                requestPriority(null)
            }

            private fun checkClosed() {
                if (closed) throw IllegalStateException(
                    "Attempting to pause but TorrentFile has already been closed: $filePath",
                    closeException
                )
            }

            override val entry get() = this@TorrentFileEntryImpl

            override suspend fun resume(priority: FilePriority) {
                checkClosed()

                val pieces = pieces.get()
                withHandle { handle ->
                    if (pieces.isNotEmpty()) {
//                            handle.piecePriority(0, Priority.TOP_PRIORITY)
//                            handle.piecePriority(pieces.lastIndex, Priority.TOP_PRIORITY)

                        val firstIndex = pieces.first().pieceIndex
                        val lastIndex = pieces.last().pieceIndex
                        handle.setPieceDeadline(firstIndex, 0)
                        handle.setPieceDeadline(lastIndex, 1)

                        handle.setPieceDeadline(firstIndex + 1, 2)
                        handle.setPieceDeadline(firstIndex + 2, 3)
                    }
                    handle.resume()
                }

                requestPriority(priority)
            }

            override fun toString(): String = "TorrentFileHandleImpl(index=$index, filePath='$filePath')"
        }

        /**
         * 与这个文件有关的 pieces, sorted naturally by pieceIndex
         */
        private val pieces: SuspendLazy<List<Piece>> = SuspendLazy {
            val allPieces = this@TorrentDownloadSessionImpl.actualInfo.await().pieces
            allPieces.filter { piece ->
                piece.offset >= offset && piece.offset < offset + length
            }.also {
                logger.info {
                    val start = it.minBy { it.startIndex }
                    val end = it.maxBy { it.lastIndex }
                    "[$torrentName] File '$filePath' piece initialized, ${it.size} pieces, offset range: $start..$end"
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

        override val filePath: String get() = relativePath.substringAfter("/")

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
            jobsToDoInHandle.add { handle ->
                val highestPriority = priorityRequests.values.maxWithOrNull(nullsFirst(naturalOrder()))
                handle.filePriority(index, highestPriority?.toLibtorrentPriority() ?: Priority.IGNORE)
                logger.info { "[$torrentName] Set file $filePath priority to $highestPriority" }
                handle.resume()
            }
        }

        override suspend fun createHandle(): TorrentFileHandle = TorrentFileHandleImpl().also {
            priorityRequests[it] = null
            openHandles.add(it)
        }

        override suspend fun resolveFile(): Path = resolveDownloadingFile().toPath()

        private val hashMd5 by lazy {
            scope.async {
                withContext(Dispatchers.IO) {
                    hashFileMd5(resolveDownloadingFile())
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
                    jobsToDoInHandle.add { handle ->
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
    private val jobsToDoInHandle = ConcurrentLinkedQueue<(TorrentHandle) -> Unit>()

    private fun actualInfo(): ActualTorrentInfo = actualInfo.getCompleted()

    internal val listener = object : TorrentAlertListener {
        // Typical event sequence:
        /*
        Alert: LISTEN_SUCCEEDED
        Alert: LSD_ERROR
        Alert: ADD_TORRENT
        Alert: STATE_CHANGED
        Alert: TORRENT_CHECKED
        Alert: TORRENT_RESUMED
        Alert: TRACKER_ANNOUNCE
        Alert: TRACKER_ERROR
        Alert: EXTERNAL_IP
        Alert: PORTMAP
        Alert: TRACKER_REPLY
        Alert: PEER_CONNECT
        Alert: DHT_GET_PEERS
        Alert: BLOCK_DOWNLOADING
        Alert: BLOCK_FINISHED
         */
//        override fun types(): IntArray? = null
//        override fun types(): IntArray = intArrayOf(
//            AlertType.ADD_TORRENT.swig(),
//            AlertType.PEER_CONNECT.swig(),
//            AlertType.PEER_DISCONNECTED.swig(),
//            AlertType.BLOCK_DOWNLOADING.swig(),
//            AlertType.PIECE_FINISHED.swig(),
//            AlertType.BLOCK_FINISHED.swig(),
//            AlertType.TORRENT_FINISHED.swig(),
//        )

        override fun onAlert(alert: TorrentAlert<*>) {
            if (alert.torrentName() != this@TorrentDownloadSessionImpl.torrentName) return // listener will receive alerts from other torrents

            try {
                when (alert) {
                    is AddTorrentAlert -> {
                        logger.info { "[$torrentName] Received alert: Torrent added" }
                        val torrentHandle = alert.handle()

                        // Add trackers
                        trackers.lines().map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                            torrentHandle.addTracker(AnnounceEntry(it))
                        }

                        // Initialize [pieces]
                        // 注意, 必须在这里初始化获取 pieces, 通过磁力链解析的可能是不准确的
                        val torrentInfo = torrentHandle.torrentFile()
                        check(torrentInfo != null) {
                            "$torrentName: Actual torrent info is null"
                        }
                        val numPieces = torrentInfo.numPieces()
                        val pieces =
                            Piece.buildPieces(numPieces) { torrentInfo.pieceSize(it).toUInt().toLong() }

                        actualInfo.complete(ActualTorrentInfo(pieces, torrentInfo))
                    }

                    // This alert is posted when a torrent completes checking. i.e. when it transitions out of the checking files state into a state where it is ready to start downloading
                    is TorrentResumedAlert -> {
                        val torrentHandle = alert.handle()
                        if (actualInfo.isCompleted) {
                            return
                        }
                        val pieceAvailability = torrentHandle.pieceAvailability()
                        logger.info { "$torrentName: Total ${pieceAvailability.size} pieces" }


//                        torrentHandle.prioritizePieces(pieces.indices.map { Priority.LOW }.toTypedArray())
//                        handleOrNull()?.controller?.onTorrentResumed()

                        // Prioritize pieces
//                        val piecePriorities = Array(numPieces) { Priority.LOW }
//                        for (i in (0..2.coerceAtMost(piecePriorities.lastIndex))
//                                + (numPieces - 1 - 2..<numPieces)) {
//                            piecePriorities[i] = Priority.SIX
//                        }
//                        torrentHandle.prioritizePieces(piecePriorities)

                        state.value = TorrentDownloadState.FetchingMetadata
                    }

                    is PeerConnectAlert -> {
                        overallStats.peerCount.getAndUpdate { it + 1 }
                    }

                    is PeerDisconnectedAlert -> {
                        overallStats.peerCount.getAndUpdate { it - 1 }
                    }

                    is BlockDownloadingAlert -> {
                        val pieceIndex = alert.pieceIndex()
                        actualInfo().onBlockDownloading(pieceIndex)
                    }

                    is PieceFinishedAlert -> {
                        val pieceIndex = alert.pieceIndex()
                        actualInfo().onPieceDownloaded(pieceIndex)
                    }

                    is BlockFinishedAlert -> {
                    }

                    is TorrentFinishedAlert -> {
                        // https://libtorrent.org/reference-Alerts.html#:~:text=report%20issue%5D-,torrent_finished_alert,-Declared%20in%20%22
                        logger.info { "[$torrentName] Torrent finished" }
                        for (openHandle in openHandles) {
                            logger.info { "[$torrentName] Set entry's finishedOverride to true because torrent finished: ${openHandle.entry.filePath}" }
                            openHandle.entry.finishedOverride.value = true
                        }
                    }

                    is FileErrorAlert -> {
                        logger.warn { "[libtorrent] $torrentName: File error: ${alert.operation()} ${alert.error()}" }
                    }

                    is MetadataFailedAlert -> {
                        logger.warn { "[libtorrent] $torrentName: Metadata failed: ${alert.error.message}" }
                    }

                    else -> {
                    }
                }

                val max = alert.handle().status().totalDone()
                val curr = alert.handle().status().totalWanted()
                overallStats.totalBytes.value = max
                overallStats.downloadedBytes.value = curr

                overallStats.downloadRate0.value = alert.handle().status().downloadRate().toUInt().toLong()
                overallStats.uploadRate0.value = alert.handle().status().uploadRate().toUInt().toLong()

                while (jobsToDoInHandle.isNotEmpty()) {
                    val job = jobsToDoInHandle.poll()
                    if (isDebug) {
                        runBlocking {
                            cancellableCoroutineScope {
                                launch {
                                    delay(5.seconds)
                                    logger.warn { "$torrentName: Job $job in handle took too long" }
                                }
                                launch {
                                    job(alert.handle())
                                    cancel()
                                }
                            }
                        }
                    } else {
                        job(alert.handle())
                    }
                }
            } catch (e: Throwable) {
                logger.info(e) { "$torrentName: Error in alert listener" }
            }
        }
    }

    private suspend fun <R> withHandle(action: (TorrentHandle) -> R): R {
        return suspendCancellableCoroutine { cont ->
            val job: (TorrentHandle) -> Unit = {
                cont.resumeWith(kotlin.runCatching { action(it) })
            }
            jobsToDoInHandle.add(job)
            cont.invokeOnCancellation {
                jobsToDoInHandle.remove(job)
            }
        }
    }

    private suspend fun closeImpl() {
        withContext(LockedSessionManager.dispatcher) {
            openHandles
            onClose(this@TorrentDownloadSessionImpl)
            logger.info { "Close torrent $torrentName: dispose handle" }
            scope.cancel()
            coroutineCloseHandle?.dispose()
        }
    }

    private var closed = false
    override fun close() {
        if (closed) {
            return
        }
        state.value = TorrentDownloadState.Closed
        closed = true

        for (openHandle in openHandles) {
            logger.info { "Closing torrent: close handle $openHandle" }
            openHandle.close()
        }

        logger.info { "Closing torrent" }

        jobsToDoInHandle.add {
            runBlocking {
                closeImpl()
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
                    jobsToDoInHandle.add { handle ->
                        handle.setPieceDeadline(index, System.currentTimeMillis().and(0x0FFF_FFFFL).toInt())
                    }
                }
                lastPrioritizedIndexes = pieceIndexes.toList()
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@kotlin.jvm.Throws(IOException::class)
private fun hashFileMd5(input: File): String {
    val md = java.security.MessageDigest.getInstance("MD5")
    val buffer = ByteArray(8192)
    input.inputStream().use { inputStream ->
        while (true) {
            val read = inputStream.read(buffer)
            if (read == -1) {
                break
            }
            md.update(buffer, 0, read)
        }
        val bytes = md.digest()
        return bytes.toHexString()
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