package me.him188.ani.app.videoplayer.torrent

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.him188.ani.app.torrent.api.files.TorrentFileHandle
import me.him188.ani.app.torrent.api.files.averageRate
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

    override fun computeHash(): String? = null

    override val networkStats: Flow<VideoData.Stats> =
        handle.entry.fileStats.map { it.downloadedBytes }.averageRate().map { downloadSpeed ->
            VideoData.Stats(
                downloadSpeed = downloadSpeed.bytes,
                uploadRate = FileSize.Unspecified,
            )
        }

    val pieces get() = handle.entry.pieces
    val isCacheFinished get() = handle.entry.fileStats.map { it.isDownloadFinished }

    override suspend fun createInput(): SeekableInput = entry.createInput()

    override suspend fun close() {
        handle.close()
    }

    override fun toString(): String {
        return "TorrentVideoData(entry=$entry)"
    }
}