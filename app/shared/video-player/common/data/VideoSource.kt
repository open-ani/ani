package me.him188.ani.app.videoplayer.data

import me.him188.ani.app.data.media.resolver.VideoSourceResolver
import me.him188.ani.app.torrent.api.TorrentDownloadSession
import me.him188.ani.utils.io.SeekableInput

/**
 * A source of the video data [S].
 *
 * [VideoSource]s are stateless: They only represent a location of the resource, not holding file descriptors or network connections, etc.
 *
 * ## Obtaining data stream
 *
 * To get the input stream of the video file, two steps are needed:
 * 1. Open a [VideoData] using [open].
 * 2. Use [VideoData.createInput] to get the input stream [SeekableInput].
 *
 * Note that both [VideoData] and [SeekableInput] are [AutoCloseable] and needs to be properly closed.
 *
 * In the BitTorrent scenario, [VideoSource.open] is to resolve magnet links, and to download the torrent metadata file.
 * [VideoData.createInput] is to start downloading the actual video file.
 * Though the actual implementation might start downloading very soon (e.g. when [VideoSource] is just created), so that
 * the video buffers more soon.
 *
 * @param S the type of the stream. For example, a torrent video source would be [TorrentDownloadSession].
 */
interface VideoSource<S : VideoData> {
    val uri: String

    /**
     * Opens the underlying video data.
     *
     * Note that [S] should be closed by the caller.
     *
     * Repeat calls to this function may return different instances so it may be desirable to store the result.
     *
     * @throws VideoSourceOpenException 当打开失败时抛出, 包含原因
     */
    @Throws(VideoSourceOpenException::class)
    suspend fun open(): S
}

/**
 * @see VideoSource.open
 * @see VideoSourceOpenException
 */
enum class OpenFailures {
    /**
     * 未找到符合剧集描述的文件
     */
    NO_MATCHING_FILE,

    /**
     * 视频资源没问题, 但播放器不支持该资源. 例如尝试用一个不支持边下边播的播放器 (例如桌面端的 vlcj) 来播放种子视频 `TorrentVideoSource`.
     */
    UNSUPPORTED_VIDEO_SOURCE,

    /**
     * TorrentEngine 等被关闭.
     *
     * 这个错误实际上不太会发生, 因为当引擎关闭时会跳过使用该引擎的 [VideoSourceResolver], 也就不会产生依赖该引擎的 [VideoSource].
     * 只有在得到 [VideoSource] 后引擎关闭 (用户去设置中关闭) 才会发生.
     */
    ENGINE_DISABLED,
}

class VideoSourceOpenException(
    val reason: OpenFailures,
    override val cause: Throwable? = null,
) : Exception("Failed to open video source: $reason", cause)