package me.him188.ani.app.torrent.api

import me.him188.ani.app.torrent.AniTorrentHandle

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

class BlockDownloadingEvent(
    override val torrentName: String,
    val pieceIndex: Int,
) : TorrentEvent

class PieceFinishedEvent(
    override val torrentName: String,
    val pieceIndex: Int,
) : TorrentEvent

class TorrentFinishedEvent(
    override val torrentName: String,
) : TorrentEvent

class StatsUpdateEvent(
    override val torrentName: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val uploadRate: Long,
    val downloadRate: Long,
) : TorrentEvent
