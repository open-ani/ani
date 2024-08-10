package me.him188.ani.app.videoplayer.torrent

import kotlinx.coroutines.flow.map
import me.him188.ani.app.torrent.api.files.TorrentFileHandle
import me.him188.ani.app.videoplayer.data.VideoData
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.utils.io.SeekableInput

class TorrentVideoData(
    private val handle: TorrentFileHandle,
) : VideoData {
    private inline val entry get() = handle.entry
    override val filename: String get() = entry.pathInTorrent
    override val fileLength: Long get() = entry.length

    override fun computeHash(): String? = entry.computeFileHashOrNull()

    override val downloadSpeed get() = entry.stats.downloadRate.map { it?.bytes ?: FileSize.Unspecified }
    override val uploadRate get() = entry.stats.uploadRate.map { it?.bytes ?: FileSize.Unspecified }

    val pieces get() = handle.entry.pieces ?: emptyList()
    val isCacheFinished get() = handle.entry.stats.isFinished

    override suspend fun createInput(): SeekableInput = entry.createInput()

    override suspend fun close() {
        handle.close()
    }

    override fun toString(): String {
        return "TorrentVideoData(entry=$entry)"
    }
}