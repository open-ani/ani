package me.him188.ani.app.torrent.api.handle

interface EventListener {
    val torrentName: String

    /**
     * 当有任何支持的事件时调用. 对于不支持的事件, 将会触发 [onUpdate].
     */
    @TorrentThread
    fun onEvent(event: TorrentEvent)

    /**
     * [onEvent] 的特例, 优化性能用.
     */
    @TorrentThread
    fun onPieceFinished(pieceIndex: Int) {
    }

    @TorrentThread
    fun onBlockDownloading(pieceIndex: Int) {
    }

    /**
     * 当有任何更新时, 在 BT 引擎的线程里调用. 对于支持的事件, 还会触发 [onEvent].
     */
    @TorrentThread
    fun onUpdate(handle: AniTorrentHandle) {
    }
}

sealed interface TorrentEvent {
    val torrentName: String
}

/**
 * This alert is posted when a torrent completes checking. i.e. when it transitions out of the checking files state into a state where it is ready to start downloading
 */
class TorrentAddEvent(
    val handle: AniTorrentHandle
) : TorrentEvent {
    override val torrentName: String get() = handle.name
}

class TorrentResumeEvent(
    override val torrentName: String,
) : TorrentEvent

//@Deprecated("Replaced with onBlockDownloading")
//class BlockDownloadingEvent(
//    override val torrentName: String,
//    val pieceIndex: Int,
//) : TorrentEvent

//class PieceFinishedEvent(
//    override val torrentName: String,
//    val pieceIndex: Int,
//) : TorrentEvent

class TorrentFinishedEvent(
    override val torrentName: String,
    handle: Lazy<AniTorrentHandle>,
) : TorrentEvent {
    @property:TorrentThread
    val handle: AniTorrentHandle by handle
}

class StatsUpdateEvent(
    override val torrentName: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val uploadRate: Long,
    val downloadRate: Long,
) : TorrentEvent
