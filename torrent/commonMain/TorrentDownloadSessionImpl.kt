package me.him188.ani.app.torrent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import me.him188.ani.app.torrent.download.PiecePriorities
import me.him188.ani.app.torrent.download.TorrentDownloadController
import me.him188.ani.app.torrent.file.TorrentInput
import me.him188.ani.app.torrent.model.Piece
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.io.asSeekableInput
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.libtorrent4j.AlertListener
import org.libtorrent4j.AnnounceEntry
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.TorrentInfo
import org.libtorrent4j.alerts.AddTorrentAlert
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import org.libtorrent4j.alerts.BlockDownloadingAlert
import org.libtorrent4j.alerts.BlockFinishedAlert
import org.libtorrent4j.alerts.PieceFinishedAlert
import org.libtorrent4j.alerts.TorrentAlert
import org.libtorrent4j.alerts.TorrentResumedAlert
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

internal class TorrentDownloadSessionImpl(
    private val removeListener: suspend (listener: AlertListener) -> Unit,
    private val closeHandle: suspend (handle: TorrentHandle) -> Unit,
    private val torrentName: String,
    private val torrentInfo: TorrentInfo,
    /**
     * The directory where the torrent is saved.
     *
     * The directory may contain multiple files, or a single file.
     * The files are not guaranteed to be present at the moment when this function returns.
     */
    private val saveDirectory: File,
    private val onClose: () -> Unit,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : TorrentDownloadSession {
    private val coroutineCloseHandle =
        parentCoroutineContext[Job]?.invokeOnCompletion {
            close()
        }

    private val logger = logger(this::class.simpleName + "@${this.hashCode()}")
//    override val torrentDownloadController: Flow<TorrentDownloadController>
//        get() = flow {
//            emit(handle.await().controller)
//        }

    override val state: MutableStateFlow<TorrentDownloadState> = MutableStateFlow(TorrentDownloadState.Starting)

    override val totalBytes: MutableStateFlow<Long> = MutableStateFlow(torrentInfo.sizeOnDisk())

    private val _downloadedBytes = MutableSharedFlow<Long>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val downloadedBytes: Flow<Long> = _downloadedBytes.distinctUntilChanged()

    override val downloadRate: MutableSharedFlow<Long> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val progress: MutableSharedFlow<Float> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override val peerCount: MutableStateFlow<Int> = MutableStateFlow(0)

    private inner class Handle(
        val pieces: List<Piece>,
        var torrentHandle: TorrentHandle,
    ) {
        val controller: TorrentDownloadController = TorrentDownloadController(
            pieces,
            createPiecePriorities()
        )

        @Synchronized
        fun onPieceDownloaded(index: Int) {
            pieces[index].state.value = PieceState.FINISHED
            logger.info { "[TorrentDownloadControl] Piece downloaded: $index. " } // Was downloading ${controller.getDebugInfo().downloadingPieces}
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

    private val handle: CompletableDeferred<Handle> = CompletableDeferred()
    private val jobsToDoInHandle = ConcurrentLinkedQueue<(TorrentHandle) -> Unit>()

    private fun handleOrNull(): Handle? = handle.takeIf { it.isCompleted }?.getCompleted()

    override suspend fun createInput(): SeekableInput {
        logger.info { "createInput: finding cache file" }
        val file = withContext(Dispatchers.IO) {
            RandomAccessFile(getFile(), "r").asSeekableInput()
        }
        logger.info { "createInput: got cache file, awaiting handle" }
        return TorrentInput(
            file,
            handle.await().pieces,
            onSeek = { piece ->
                logger.info { "[TorrentDownloadControl] Set piece ${piece.pieceIndex} priority to TOP because it was requested " }
                val pieces = handle.await().pieces
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

    private suspend fun getFile(): File {
        while (true) {
            val file = runInterruptible(Dispatchers.IO) {
                saveDirectory.walk().singleOrNull { it.isFile }
            }
            if (file != null) {
                logger.info { "Get file: ${file.absolutePath}" }
                return file
            }
            logger.info { "Still waiting to get file... saveDirectory: $saveDirectory" }
            delay(1.seconds)
        }
        @Suppress("UNREACHABLE_CODE") // compiler bug
        error("")
    }

    override val fileLength: Long get() = totalBytes.value

    override val fileHash: String?
        get() {
            if (state.value != TorrentDownloadState.Finished) {
                return null
            }
            val file = saveDirectory.walk().singleOrNull { it.isFile } ?: return null
            return hashFileMd5(file)
        }

    override val isFinished: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val onFinish = CompletableDeferred(Unit)
    private var torrentHandle: TorrentHandle? = null

    internal val listener = object : AlertListener {
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
        override fun types(): IntArray? = null
//        override fun types(): IntArray = intArrayOf(
//            AlertType.ADD_TORRENT.swig(),
//            AlertType.PEER_CONNECT.swig(),
//            AlertType.PEER_DISCONNECTED.swig(),
//            AlertType.BLOCK_DOWNLOADING.swig(),
//            AlertType.PIECE_FINISHED.swig(),
//            AlertType.BLOCK_FINISHED.swig(),
//            AlertType.TORRENT_FINISHED.swig(),
//        )

        override fun alert(alert: Alert<*>) {
            if (alert !is TorrentAlert) return
            if (alert.torrentName() != this@TorrentDownloadSessionImpl.torrentName) return // listener will receive alerts from other torrents

            try {
                val type = alert.type()

                when (type) {
                    AlertType.ADD_TORRENT -> {
                        logger.info { "Torrent added" }
                        val torrentHandle = (alert as AddTorrentAlert).handle()
                        this@TorrentDownloadSessionImpl.torrentHandle = torrentHandle

                        // Add trackers
                        trackers.lines().map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                            torrentHandle.addTracker(AnnounceEntry(it))
                        }

                        // Initialize [pieces]
                        val torrentInfo = torrentHandle.torrentFile()
                        val numPieces = torrentInfo.numPieces()
                        val pieces =
                            Piece.buildPieces(numPieces) { torrentInfo.pieceSize(it).toUInt().toLong() }

                        if (pieces.isNotEmpty()) {
//                            torrentHandle.piecePriority(0, Priority.TOP_PRIORITY)
                            torrentHandle.setPieceDeadline(0, 0)
                            torrentHandle.setPieceDeadline(1, 0)
                            torrentHandle.setPieceDeadline(2, 1)
//                            torrentHandle.piecePriority(pieces.lastIndex, Priority.TOP_PRIORITY)
                            torrentHandle.setPieceDeadline(pieces.lastIndex, 0)
                        }

                        this@TorrentDownloadSessionImpl.handle.complete(Handle(pieces, torrentHandle))

                        torrentHandle.resume()
                    }

                    // This alert is posted when a torrent completes checking. i.e. when it transitions out of the checking files state into a state where it is ready to start downloading
                    AlertType.TORRENT_RESUMED -> {
                        val torrentHandle = (alert as TorrentResumedAlert).handle()
                        if (handle.isCompleted) {
                            handleOrNull()?.torrentHandle = torrentHandle
                            return
                        }
                        logger.info { "TORRENT_RESUMED" }
                        val pieceAvailability = torrentHandle.pieceAvailability()
                        logger.info { "Total ${pieceAvailability.size} pieces" }


//                        torrentHandle.prioritizePieces(pieces.indices.map { Priority.LOW }.toTypedArray())
//                        handleOrNull()?.controller?.onTorrentResumed()

                        // Prioritize pieces
//                        val piecePriorities = Array(numPieces) { Priority.LOW }
//                        for (i in (0..2.coerceAtMost(piecePriorities.lastIndex))
//                                + (numPieces - 1 - 2..<numPieces)) {
//                            piecePriorities[i] = Priority.SIX
//                        }
//                        torrentHandle.prioritizePieces(piecePriorities)
                        logger.info { "Priorities set" }

                        state.value = TorrentDownloadState.FetchingMetadata
                        _downloadedBytes.tryEmit(0)
                        progress.tryEmit(0f)
                    }

                    AlertType.PEER_CONNECT -> {
                        peerCount.getAndUpdate { it + 1 }
                    }

                    AlertType.PEER_DISCONNECTED -> {
                        peerCount.getAndUpdate { it - 1 }
                    }
                    //
                    //                AlertType.BLOCK_TIMEOUT -> {
                    //                    val a = alert as BlockTimeoutAlert
                    //                }

                    AlertType.BLOCK_DOWNLOADING -> {
                        val a = alert as BlockDownloadingAlert
                        val pieceIndex = a.pieceIndex()
                        handleOrNull()?.onBlockDownloading(pieceIndex)
                    }

                    AlertType.PIECE_FINISHED -> {
                        val a = alert as PieceFinishedAlert
                        val pieceIndex = a.pieceIndex()
                        handleOrNull()?.onPieceDownloaded(pieceIndex)
                    }

                    AlertType.BLOCK_FINISHED -> {
                        val a = alert as BlockFinishedAlert

                        val handle = alert.handle()
                        torrentHandle = handle
                        handleOrNull()?.torrentHandle = handle

                        while (jobsToDoInHandle.isNotEmpty()) {
                            val job = jobsToDoInHandle.poll()
                            job(handle)
                        }

                        val totalWanted = a.handle().status().totalWanted()
                        val totalDone = a.handle().status().totalDone()
                        _downloadedBytes.tryEmit(totalDone)
                        downloadRate.tryEmit(a.handle().status().downloadRate().toUInt().toLong())
                        progress.tryEmit(totalDone.toFloat() / totalWanted.toFloat())
                    }

                    AlertType.TORRENT_FINISHED -> {
                        logger.info { "Torrent finished" }
                        handleOrNull()?.onFinished()
                            ?: error("Torrent handle should not be null")
                        downloadRate.tryEmit(0)
                        progress.tryEmit(1f)
                        state.value = TorrentDownloadState.Finished
                        isFinished.value = true
                    }

                    else -> {
                    }
                }
            } catch (e: Throwable) {
                logger.info(e) { "Error in alert listener" }
            }
        }
    }

    override suspend fun pause() {
        if (state.value == TorrentDownloadState.Finished) {
            return
        }
        withHandle { it.pause() }
    }

    override suspend fun resume() {
        if (state.value == TorrentDownloadState.Finished) {
            return
        }
        withHandle { it.resume() }
    }

    override suspend fun filePath(): Path {
        return getFile().toPath()
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

    override suspend fun awaitFinished() {
        onFinish.join()
    }

    private suspend fun closeImpl() {
        removeListener(listener)
        torrentHandle?.let {
            closeHandle(it)
        }
        onClose()
        coroutineCloseHandle?.dispose()
    }

    private var closed = false
    override fun close() {
        if (closed) {
            return
        }
        closed = true
        logger.info { "Closing torrent" }
        jobsToDoInHandle.add {
            if (torrentHandle?.isValid == true) {
                runBlocking {
                    closeImpl()
                }
            }
        }
    }

    override suspend fun closeAndDelete() {
        closeImpl()
        withContext(Dispatchers.IO) { saveDirectory.deleteRecursively() }
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
                logger.info { "[TorrentDownloadControl] Prioritizing pieces: $pieceIndexes" }
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
                            udp://tracker.opentrackr.org:1337/announce
                            udp://opentracker.i2p.rocks:6969/announce
                            udp://open.demonii.com:1337/announce
                            http://tracker.openbittorrent.com:80/announce
                            udp://tracker.openbittorrent.com:6969/announce
                            udp://open.stealth.si:80/announce
                            udp://tracker.torrent.eu.org:451/announce
                            udp://exodus.desync.com:6969/announce
                            udp://tracker.auctor.tv:6969/announce
                            udp://explodie.org:6969/announce
                            udp://tracker1.bt.moack.co.kr:80/announce
                            udp://uploads.gamecoast.net:6969/announce
                            udp://tracker.tiny-vps.com:6969/announce
                            udp://tracker.therarbg.com:6969/announce
                            udp://tracker.theoks.net:6969/announce
                            udp://tracker.skyts.net:6969/announce
                            udp://tracker.moeking.me:6969/announce
                            udp://thinking.duckdns.org:6969/announce
                            udp://tamas3.ynh.fr:6969/announce
                            udp://retracker01-msk-virt.corbina.net:80/announce
                        """.trimIndent()