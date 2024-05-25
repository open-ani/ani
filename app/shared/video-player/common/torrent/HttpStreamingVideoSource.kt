package me.him188.ani.app.videoplayer.torrent

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.videoplayer.data.VideoData
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.utils.io.SeekableInput

class HttpStreamingVideoSource(
    override val uri: String,
    private val filename: String,
) : VideoSource<HttpStreamingVideoData> {
    override suspend fun open(): HttpStreamingVideoData {
        return HttpStreamingVideoData(uri, filename)
    }
}


class HttpStreamingVideoData(
    val url: String,
    override val filename: String
) : VideoData {
    override val fileLength: Long = 0
    override val downloadSpeed: Flow<FileSize> = flowOf()
    override val uploadRate: Flow<FileSize> = flowOf()

    override val supportsStreaming: Boolean get() = true

    override fun computeHash(): String? = null

    override fun createInput(): SeekableInput {
        throw UnsupportedOperationException()
    }

    override fun close() {
    }
}