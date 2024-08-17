package me.him188.ani.app.videoplayer.torrent

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.videoplayer.data.VideoData
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.datasources.api.MediaExtraFiles
import me.him188.ani.datasources.api.matcher.WebVideo
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.utils.io.SeekableInput

class HttpStreamingVideoSource(
    override val uri: String,
    private val filename: String,
    val webVideo: WebVideo,
    override val extraFiles: MediaExtraFiles,
) : VideoSource<HttpStreamingVideoData> {
    override suspend fun open(): HttpStreamingVideoData {
        return HttpStreamingVideoData(uri, filename)
    }

    override fun toString(): String {
        return "HttpStreamingVideoSource(webVideo=$webVideo, filename='$filename')"
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

    override suspend fun createInput(): SeekableInput {
        throw UnsupportedOperationException()
    }

    override suspend fun close() {
    }
}