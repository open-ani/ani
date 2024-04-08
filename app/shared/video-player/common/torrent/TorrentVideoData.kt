package me.him188.ani.app.videoplayer.torrent

import me.him188.ani.app.torrent.TorrentDownloadSession
import me.him188.ani.app.videoplayer.data.VideoData

class TorrentVideoData(
    val session: TorrentDownloadSession,
) : VideoData {
    override val fileLength: Long get() = session.fileLength

    override val hash: String? get() = session.fileHash

    override fun close() {
        session.close()
    }
}