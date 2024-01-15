package me.him188.ani.app.torrent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import me.him188.ani.app.torrent.file.DeferredFile
import me.him188.ani.app.torrent.file.TorrentDeferredFileImpl
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
import java.io.File
import java.io.RandomAccessFile
import kotlin.time.Duration.Companion.seconds

/**
 * Needs to be closed.
 */
public interface TorrentDownloadSession : DownloadStats, AutoCloseable {
    public val saveDirectory: File
    public val state: Flow<TorrentDownloadState>

    public suspend fun createDeferredFile(): DeferredFile
}

public sealed class TorrentDownloadState {
    public data object Ready : TorrentDownloadState()
    public data object FetchingMetadata : TorrentDownloadState()
    public data object Downloading : TorrentDownloadState()
    public data object Finished : TorrentDownloadState()
}

internal class TorrentDownloadSessionImpl(
    private val removeListener: (listener: AlertListener) -> Unit,
    private val closeHandle: (handle: TorrentHandle) -> Unit,
    private val torrentInfo: TorrentInfo,
    override val saveDirectory: File,
) : TorrentDownloadSession {
    private val logger = logger(this::class)

    override val state: MutableStateFlow<TorrentDownloadState> = MutableStateFlow(TorrentDownloadState.Ready)

    //    private val _totalBytes = MutableStateFlow(torrentInfo.totalSize())
    override val totalBytes: Flow<Long> = MutableStateFlow(torrentInfo.sizeOnDisk())

    private val _downloadedBytes = MutableSharedFlow<Long>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val downloadedBytes: Flow<Long> = _downloadedBytes.distinctUntilChanged()

    override val downloadRate: MutableSharedFlow<Long> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val progress: MutableSharedFlow<Float> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override val peerCount: MutableStateFlow<Int> = MutableStateFlow(0)
    override val pieces: List<Piece> =
        Piece.buildPieces(torrentInfo.numPieces()) { torrentInfo.pieceSize(it).toUInt().toLong() }

    override suspend fun createDeferredFile(): DeferredFile = TorrentDeferredFileImpl(
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
        pieces
    )

    override val isFinished: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val onFinish = CompletableDeferred(Unit)
    private var torrentHandle: TorrentHandle? = null

    private lateinit var piecePriorities: Array<Priority>

    @Synchronized
    private fun onPieceDownloaded(index: Int) {
        pieces[index].state.value = PieceState.FINISHED
    }

    internal val listener = object : AlertListener {
        override fun types(): IntArray? = null
        override fun alert(alert: Alert<*>) {
            try {
                val type = alert.type()

                when (type) {
                    AlertType.ADD_TORRENT -> {
                        logger.info { "Connecting peers" }
                        val handle = (alert as AddTorrentAlert).handle()
                        val pieceAvailability = handle.pieceAvailability()
                        logger.info { "Total ${pieceAvailability.size} pieces" }
                        logger.info { "Download first and last 10 first." }
                        torrentHandle = handle

                        // 根据实际测试, 只给部分 piece 设置优先级为 TOP_PRIORITY 并不一定会让这部分优先下载. 必须得忽略其他 pieces.
                        piecePriorities = Array(pieceAvailability.size) { Priority.LOW }
                        for (i in (0..16) + (pieceAvailability.lastIndex - 16..pieceAvailability.lastIndex)) {
                            piecePriorities[i] = Priority.TOP_PRIORITY
                        }

                        // TODO: 做一个状态, 最初只下载头尾区块, 然后根据当前下载进度, 只请求最近的区块. 优先确保即将要播放的区块下载完成.
                        handle.prioritizePieces(piecePriorities)

                        // Add trackers
                        trackers.lines().map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                            handle.addTracker(AnnounceEntry(it))
                        }

                        handle.resume()
                        state.value = TorrentDownloadState.FetchingMetadata
                        logger.info { "Torrent added" }
                        _downloadedBytes.tryEmit(0)
                        //                    _totalBytes.tryEmit(torrentInfo.totalSize())
                        progress.tryEmit(0f)
                    }

                    //                AlertType.METADATA_RECEIVED -> {
                    //                    logger.info { "Metadata received" }
                    //                }

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
                        pieces[pieceIndex].state.value = PieceState.DOWNLOADING
                    }

                    AlertType.PIECE_FINISHED -> {
                        val a = alert as PieceFinishedAlert

                        val pieceIndex = a.pieceIndex()
                        onPieceDownloaded(pieceIndex)
                    }

                    AlertType.BLOCK_FINISHED -> {
                        val a = alert as BlockFinishedAlert

                        state.value = TorrentDownloadState.Downloading

                        //                    val p = (a.handle().status().progress() * 100).toInt()

                        val totalWanted = a.handle().status().totalWanted()
                        //                    _totalBytes.tryEmit(totalWanted)
                        val totalDone = a.handle().status().totalDone()
                        _downloadedBytes.tryEmit(totalDone)
                        downloadRate.tryEmit(a.handle().status().downloadRate().toUInt().toLong())
                        progress.tryEmit(totalDone.toFloat() / totalWanted.toFloat())

                        //                    sessionManager.stats().totalDownload()
                    }

                    AlertType.TORRENT_FINISHED -> {
                        logger.info { "Torrent finished" }
                        for (piece in pieces) {
                            piece.state.value = PieceState.FINISHED
                        }
                        //                    _totalBytes.replayCache.lastOrNull()?.let {
                        //                        _downloadedBytes.tryEmit(it)
                        //                    }
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