package me.him188.ani.app.videoplayer.torrent

import me.him188.ani.app.torrent.TorrentDownloadSession
import me.him188.ani.app.videoplayer.data.VideoData
import me.him188.ani.utils.io.SeekableInput

class TorrentVideoData(
    val session: TorrentDownloadSession,
) : VideoData {
    override val fileLength: Long get() = session.fileLength

    override val hash: String? get() = session.fileHash

    override suspend fun createInput(): SeekableInput = session.createInput()

    override fun close() {
        session.close()
    }
}