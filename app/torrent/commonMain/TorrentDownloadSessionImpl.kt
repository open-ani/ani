package me.him188.ani.app.torrent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import me.him188.ani.app.torrent.download.PiecePriorities
import me.him188.ani.app.torrent.file.SeekableInput
import me.him188.ani.app.torrent.file.TorrentInput
import me.him188.ani.app.torrent.file.asSeekableInput
import me.him188.ani.app.torrent.model.Piece
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.libtorrent4j.AlertListener
import org.libtorrent4j.AnnounceEntry
import org.libtorrent4j.Priority
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.TorrentInfo
import org.libtorrent4j.alerts.AddTorrentAlert
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import org.libtorrent4j.alerts.BlockDownloadingAlert
import org.libtorrent4j.alerts.BlockFinishedAlert
import org.libtorrent4j.alerts.PieceFinishedAlert
import org.libtorrent4j.alerts.TorrentResumedAlert
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration.Companion.seconds

internal class TorrentDownloadSessionImpl(
    private val removeListener: (listener: AlertListener) -> Unit,
    private val closeHandle: (handle: TorrentHandle) -> Unit,
    private val torrentInfo: TorrentInfo,
    /**
     * The directory where the torrent is saved.
     *
     * The directory may contain multiple files, or a single file.
     * The files are not guaranteed to be present at the moment when this function returns.
     */
    private val saveDirectory: File,
) : TorrentDownloadSession {
    private val logger = logger(this::class)
//    override val torrentDownloadController: Flow<TorrentDownloadController>
//        get() = flow {
//            emit(handle.await().controller)
//        }

    override val state: MutableStateFlow<TorrentDownloadState> = MutableStateFlow(TorrentDownloadState.Starting)

    override val totalBytes: Flow<Long> = MutableStateFlow(torrentInfo.sizeOnDisk())

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
//        val controller: TorrentDownloadController = TorrentDownloadController(
//            pieces,
//            { torrentHandle }.asPiecePriorities()
//        )

        @Synchronized
        fun onPieceDownloaded(index: Int) {
            pieces[index].state.value = PieceState.FINISHED
            logger.info { "[TorrentDownloadControl] Piece downloaded: $index. " } // Was downloading ${controller.getDebugInfo().downloadingPieces}
//            controller.onPieceDownloaded(index)
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

//        @Synchronized
//        fun onAllRequestedPiecesDownloaded() {
////            controller.downloadingPieces.forEach {
////                pieces[it].state.value = PieceState.FINISHED
////            }
////            controller.onAllRequestedPiecesDownloaded()
//        }
    }

    private val handle: CompletableDeferred<Handle> = CompletableDeferred()
    private val jobsToDoInHandle = ConcurrentLinkedQueue<(TorrentHandle) -> Unit>()

    private fun handleOrNull(): Handle? = handle.takeIf { it.isCompleted }?.getCompleted()

    override suspend fun createInput(): SeekableInput = TorrentInput(
        run {
            while (true) {
                val file = saveDirectory.walk().singleOrNull { it.isFile }
                if (file != null) {
                    logger.info { "Get file: ${file.absolutePath}" }
                    return@run RandomAccessFile(file, "r").asSeekableInput()
                }

                delay(1.seconds)
            }
            @Suppress("UNREACHABLE_CODE") // compiler bug
            error("")
        },
        handle.await().pieces,
        onSeek = { piece ->
            logger.info { "[TorrentDownloadControl] Set piece ${piece.pieceIndex} priority to TOP because it was requested " }
            jobsToDoInHandle.add { handle ->
                handle.piecePriority(piece.pieceIndex, Priority.TOP_PRIORITY)
            }
        }
    )

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
            try {
                val type = alert.type()

                when (type) {
                    AlertType.ADD_TORRENT -> {
                        logger.info { "Torrent added" }
                        val handle = (alert as AddTorrentAlert).handle()
                        torrentHandle = handle

                        // Add trackers
                        trackers.lines().map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                            handle.addTracker(AnnounceEntry(it))
                        }
                        handle.resume()
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


                        // Initialize [pieces]
                        val torrentInfo = torrentHandle.torrentFile()
                        val numPieces = torrentInfo.numPieces()
                        val pieces =
                            Piece.buildPieces(numPieces) { torrentInfo.pieceSize(it).toUInt().toLong() }

                        this@TorrentDownloadSessionImpl.handle.complete(Handle(pieces, torrentHandle))
//                        torrentHandle.prioritizePieces(pieces.indices.map { Priority.LOW }.toTypedArray())
//                        handleOrNull()?.controller?.onTorrentResumed()

                        // Prioritize pieces
                        // 根据实际测试, 只给部分 piece 设置优先级为 TOP_PRIORITY 并不一定会让这部分优先下载. 必须得忽略其他 pieces.
                        val piecePriorities = Array(numPieces) { Priority.LOW }
                        for (i in (0..4.coerceAtMost(piecePriorities.lastIndex))
                                + (numPieces - 1 - 4..<numPieces)) {
                            piecePriorities[i] = Priority.TOP_PRIORITY
                        }
                        torrentHandle.prioritizePieces(piecePriorities)
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

    override suspend fun awaitFinished() {
        onFinish.join()
    }

    override fun close() {
        removeListener(listener)
        torrentHandle?.let(closeHandle)
        logger.info { "Closing torrent" }
    }

    private fun (() -> TorrentHandle).asPiecePriorities(): PiecePriorities {
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
                    invoke().piecePriority(index, Priority.TOP_PRIORITY)
                }
                logger.info { "[TorrentDownloadControl] Resuming" }
//                invoke().resume()
                lastPrioritizedIndexes = pieceIndexes.toList()
            }
        }
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