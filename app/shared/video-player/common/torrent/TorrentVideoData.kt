package me.him188.ani.app.videoplayer.torrent

import kotlinx.coroutines.flow.map
import me.him188.ani.app.torrent.TorrentDownloadSession
import me.him188.ani.app.videoplayer.data.VideoData
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.utils.io.SeekableInput

class TorrentVideoData(
    val session: TorrentDownloadSession,
) : VideoData {
    override val fileLength: Long get() = session.fileLength

    override val hash: String? get() = session.fileHash

    override val downloadSpeed get() = session.downloadRate.map { it?.bytes ?: FileSize.Unspecified }
    override val uploadRate get() = session.uploadRate.map { it?.bytes ?: FileSize.Unspecified }

    override suspend fun createInput(): SeekableInput = session.createInput()

    override fun close() {
        session.close()
    }
}