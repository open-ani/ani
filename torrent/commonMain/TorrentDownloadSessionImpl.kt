package me.him188.ani.app.torrent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import me.him188.ani.app.torrent.download.PiecePriorities
import me.him188.ani.app.torrent.download.TorrentDownloadController
import me.him188.ani.app.torrent.file.TorrentInput
import me.him188.ani.app.torrent.model.Piece
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.io.asSeekableInput
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.trace
import me.him188.ani.utils.logging.warn
import org.libtorrent4j.AnnounceEntry
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
import java.io.RandomAccessFile
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

internal class TorrentDownloadSessionImpl(
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
    private val onClose: (TorrentDownloadSessionImpl) -> Unit,
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

    private val _downloadedBytes = MutableStateFlow(0L)
    override val downloadedBytes: Flow<Long> = _downloadedBytes

    override val downloadRate = MutableStateFlow<Long?>(null)

    private val _uploadRate = MutableStateFlow<Long?>(null)
    override val uploadRate
        get() = channelFlow {
            coroutineScope {
                val time = object {
                    @Volatile
                    var value: Long = 0L
                }
                launch {
                    while (isActive) {
                        delay(1000)
                        val now = System.currentTimeMillis()
                        if (now - time.value >= 1000) {
                            send(0L)
                        }
                    }
                }
                _uploadRate.collect {
                    time.value = System.currentTimeMillis()
                    send(it)
                }
            }
        }.distinctUntilChanged()

    override val progress = MutableStateFlow(0f)

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
            logger.trace { "[TorrentDownloadControl] Piece downloaded: $index. " } // Was downloading ${controller.getDebugInfo().downloadingPieces}
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
                logger.info { "[TorrentDownloadControl] $torrentName: Set piece ${piece.pieceIndex} priority to TOP because it was requested " }
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
                logger.info { "$torrentName: Get file: ${file.absolutePath}" }
                return file
            }
            logger.info { "$torrentName: Still waiting to get file... saveDirectory: $saveDirectory" }
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
            torrentHandle = alert.handle()

            try {
                when (alert) {
                    is AddTorrentAlert -> {
                        logger.info { "$torrentName: Torrent added" }
                        val torrentHandle = alert.handle()
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
                    is TorrentResumedAlert -> {
                        val torrentHandle = alert.handle()
                        if (handle.isCompleted) {
                            handleOrNull()?.torrentHandle = torrentHandle
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
                        _downloadedBytes.tryEmit(0)
                        progress.tryEmit(0f)
                    }

                    is PeerConnectAlert -> {
                        peerCount.getAndUpdate { it + 1 }
                    }

                    is PeerDisconnectedAlert -> {
                        peerCount.getAndUpdate { it - 1 }
                    }

                    is BlockDownloadingAlert -> {
                        val pieceIndex = alert.pieceIndex()
                        handleOrNull()?.onBlockDownloading(pieceIndex)
                    }

                    is PieceFinishedAlert -> {
                        val pieceIndex = alert.pieceIndex()
                        handleOrNull()?.onPieceDownloaded(pieceIndex)
                    }

                    is BlockFinishedAlert -> {
                        val handle = alert.handle()
                        torrentHandle = handle
                        handleOrNull()?.torrentHandle = handle

                        val totalWanted = alert.handle().status().totalWanted()
                        val totalDone = alert.handle().status().totalDone()
                        _downloadedBytes.value = totalDone
                        downloadRate.value = alert.handle().status().downloadRate().toUInt().toLong()
                        _uploadRate.value = alert.handle().status().uploadRate().toUInt().toLong()
                        progress.value = totalDone.toFloat() / totalWanted.toFloat()
                    }

                    is TorrentFinishedAlert -> {
                        // https://libtorrent.org/reference-Alerts.html#:~:text=report%20issue%5D-,torrent_finished_alert,-Declared%20in%20%22
                        logger.info { "$torrentName: Torrent finished" }
                        handleOrNull()?.onFinished()
                            ?: error("Torrent handle should not be null")
                        downloadRate.value = 0
                        progress.value = 1f
                        state.value = TorrentDownloadState.Finished
                        isFinished.value = true
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

                _uploadRate.value = alert.handle().status().uploadRate().toUInt().toLong()

                while (jobsToDoInHandle.isNotEmpty()) {
                    val job = jobsToDoInHandle.poll()
                    job(alert.handle())
                }
            } catch (e: Throwable) {
                logger.info(e) { "$torrentName: Error in alert listener" }
            }
        }
    }

    override suspend fun pause() {
        if (state.value == TorrentDownloadState.Finished) {
            return
        }
        withTimeoutOrNull(2000) {
            withHandle { it.pause() }
        }
    }

    override suspend fun resume() {
        if (state.value == TorrentDownloadState.Finished) {
            return
        }
        withTimeoutOrNull(2000) {
            withHandle { it.resume() }
        }
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
        withContext(LockedSessionManager.dispatcher) {
            logger.info { "Close torrent $torrentName: closeHandle $torrentHandle" }
            torrentHandle?.let {
                closeHandle(it)
            }
            logger.info { "Close torrent $torrentName: onClose" }
            onClose(this@TorrentDownloadSessionImpl)
            logger.info { "Close torrent $torrentName: dispose handle" }
            coroutineCloseHandle?.dispose()
        }
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
        close()
        delay(1000)
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
                logger.trace { "[TorrentDownloadControl] Prioritizing pieces: $pieceIndexes" }
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