package me.him188.ani.app.torrent.anitorrent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.torrent.anitorrent.binding.event_listener_t
import me.him188.ani.app.torrent.anitorrent.binding.new_event_listener_t
import me.him188.ani.app.torrent.anitorrent.binding.session_settings_t
import me.him188.ani.app.torrent.anitorrent.binding.session_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_resume_data_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_state_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_stats_t
import me.him188.ani.app.torrent.anitorrent.session.SwigTorrentAddInfo
import me.him188.ani.app.torrent.anitorrent.session.SwigTorrentHandle
import me.him188.ani.app.torrent.anitorrent.session.SwigTorrentManagerSession
import me.him188.ani.app.torrent.anitorrent.session.SwigTorrentResumeData
import me.him188.ani.app.torrent.anitorrent.session.SwigTorrentStats
import me.him188.ani.app.torrent.api.HttpFileDownloader
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.app.torrent.api.TorrentDownloaderConfig
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.logging.info
import kotlin.coroutines.CoroutineContext

internal actual fun createAnitorrentTorrentDownloader(
    rootDataDirectory: SystemPath,
    httpFileDownloader: HttpFileDownloader,
    torrentDownloaderConfig: TorrentDownloaderConfig,
    parentCoroutineContext: CoroutineContext
): TorrentDownloader {
    AnitorrentLibraryLoader.loadLibraries()
    AnitorrentTorrentDownloader.logger.info { "Creating a new AnitorrentTorrentDownloader" }

    val session = session_t()
    val settings = session_settings_t().apply {
        // TODO: support more torrent settings (e.g. download speed limit)
        user_agent = torrentDownloaderConfig.userAgent
        peer_fingerprint = torrentDownloaderConfig.peerFingerprint
        handshake_client_version = torrentDownloaderConfig.handshakeClientVersion
        listOf(
            "router.utorrent.com:6881",
            "router.bittorrent.com:6881",
            "dht.transmissionbt.com:6881",
            "router.bitcomet.com:6881",
        ).forEach {
            dht_bootstrap_nodes_extra_add(it)
        }
    }
    try {
        session.start(settings)
    } finally {
        settings.delete() // 其实也可以等 GC, 不过反正我们都不用了
    }
    AnitorrentTorrentDownloader.logger.info { "AnitorrentTorrentDownloader created" }
    return SwigAnitorrentTorrentDownloader(
        rootDataDirectory = rootDataDirectory,
        native = SwigTorrentManagerSession(session),
        httpFileDownloader = httpFileDownloader,
        parentCoroutineContext = parentCoroutineContext,
    )
}


internal class SwigAnitorrentTorrentDownloader(
    rootDataDirectory: SystemPath,
    override val native: SwigTorrentManagerSession,
    httpFileDownloader: HttpFileDownloader,
    parentCoroutineContext: CoroutineContext
) : AnitorrentTorrentDownloader<SwigTorrentHandle, SwigTorrentAddInfo>(
    rootDataDirectory,
    httpFileDownloader,
    parentCoroutineContext,
) {

    private val eventListener = object : event_listener_t() {
        override fun on_save_resume_data(handleId: Long, data: torrent_resume_data_t?) {
            data ?: return
            dispatchToSession(handleId) {
                it.onSaveResumeData(SwigTorrentResumeData(data))
            }
        }

        override fun on_checked(handleId: Long) {
            dispatchToSession(handleId) {
                it.onTorrentChecked()
            }
        }

        override fun on_block_downloading(handleId: Long, pieceIndex: Int, blockIndex: Int) {
            dispatchToSession(handleId) {
                it.onPieceDownloading(pieceIndex)
            }
        }

        override fun on_piece_finished(handleId: Long, pieceIndex: Int) {
            dispatchToSession(handleId) {
                it.onPieceFinished(pieceIndex)
            }
        }

        override fun on_torrent_state_changed(handleId: Long, state: torrent_state_t?) {
            state ?: return
            dispatchToSession(handleId) {
                if (state == torrent_state_t.finished) {
                    it.onTorrentFinished()
                }
            }
        }

        override fun on_status_update(handleId: Long, stats: torrent_stats_t?) {
            stats ?: return
            dispatchToSession(handleId) {
                it.onStatsUpdate(SwigTorrentStats(stats))
            }
        }

        override fun on_file_completed(handleId: Long, fileIndex: Int) {
            dispatchToSession(handleId) {
                it.onFileCompleted(fileIndex)
            }
        }

        override fun on_torrent_removed(handleId: Long, torrentName: String) {
            if (handleId != 0L) {
                dispatchToSession(handleId) {
                    it.onTorrentRemoved()
                }
            } else {
                // torrent_removed_alerts 事件处理时其 handle 可能已经无效
                // 但是它的 torrent_name 还是有效的
                // 在这里 runBlocking 是没问题的, 因为 on_torrent_removed 一定只会在 actualTorrentInfo 之后调用
                runBlocking(Dispatchers.IO) {
                    openSessions.value.values.firstOrNull { session ->
                        session.getName() == torrentName
                    }
                }?.onTorrentRemoved()
            }
        }
    }

    private val eventSignal = Channel<Unit>(1)

    // must keep referenced
    private val newEventListener = object : new_event_listener_t() {
        override fun on_new_events() {
            // 根据 libtorrent 文档, 这里面不能处理事件
            eventSignal.trySend(Unit)
        }
    }

    init {
        native.native.set_new_event_listener(newEventListener)
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                eventSignal.receive() // await new events
                native.native.process_events(eventListener) // can block thread
            }
        }
    }

    override fun close() {
        native.native.remove_listener() // must remove, before gc-ing this object
        super.close()
    }
}
