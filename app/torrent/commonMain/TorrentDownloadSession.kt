package me.him188.ani.app.torrent

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.libtorrent4j.AlertListener
import org.libtorrent4j.SessionManager
import org.libtorrent4j.TorrentInfo
import org.libtorrent4j.alerts.AddTorrentAlert
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import org.libtorrent4j.alerts.BlockFinishedAlert
import java.io.File

/**
 * Needs to be closed.
 */
public interface TorrentDownloadSession : DownloadStats, AutoCloseable {
    public val savedFile: File

    public val state: Flow<TorrentDownloadState>
//    val metadata: Flow<TorrentMetadata>
}

public sealed class TorrentDownloadState {
    public data object Ready : TorrentDownloadState()
    public data object FetchingMetadata : TorrentDownloadState()
    public data object Downloading : TorrentDownloadState()
    public data object Finished : TorrentDownloadState()
}

internal class TorrentDownloadSessionImpl(
    private val sessionManager: SessionManager,
    private val torrentInfo: TorrentInfo,
    override val savedFile: File,
) : TorrentDownloadSession {
    private val logger = logger(this::class)

    override val state: MutableStateFlow<TorrentDownloadState> = MutableStateFlow(TorrentDownloadState.Ready)
    private val _totalBytes = MutableSharedFlow<Long>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val totalBytes: Flow<Long> = _totalBytes.distinctUntilChanged()

    private val _downloadedBytes = MutableSharedFlow<Long>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val downloadedBytes: Flow<Long> = _downloadedBytes.distinctUntilChanged()

    override val downloadRate: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val progress: Flow<Float> = downloadedBytes.combine(totalBytes) { downloaded, total ->
        downloaded.toFloat() / total.toFloat()
    }

    override val isFinished: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val onFinish = CompletableDeferred(Unit)

    internal val listener = object : AlertListener {
        override fun types(): IntArray? = null
        override fun alert(alert: Alert<*>) {
            val type = alert.type()

            when (type) {
                AlertType.ADD_TORRENT -> {
                    (alert as AddTorrentAlert).handle().resume()
                    state.value = TorrentDownloadState.FetchingMetadata
                    logger.info { "Torrent added" }
                }

                AlertType.METADATA_RECEIVED -> {
                    state.value = TorrentDownloadState.Downloading
                    logger.info { "Metadata received" }
                }

                AlertType.BLOCK_FINISHED -> {
                    val a = alert as BlockFinishedAlert
//                    val p = (a.handle().status().progress() * 100).toInt()

                    _totalBytes.tryEmit(a.handle().status().totalWanted())
                    _downloadedBytes.tryEmit(a.handle().status().totalDone())
                    downloadRate.tryEmit(a.handle().status().downloadRate().toUInt().toLong())

//                    sessionManager.stats().totalDownload()
                }

                AlertType.TORRENT_FINISHED -> {
                    logger.info { "Torrent finished" }
                    state.value = TorrentDownloadState.Finished
                    isFinished.value = true
                }

                else -> {
                }
            }
        }
    }

    override suspend fun awaitFinished() {
        onFinish.join()
    }

    override fun close() {
        kotlin.runCatching {
            sessionManager.stop()
        }
    }
}
