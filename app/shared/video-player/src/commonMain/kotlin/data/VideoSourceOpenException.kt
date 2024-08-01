package me.him188.ani.app.videoplayer.data

import me.him188.ani.app.data.source.media.resolver.VideoSourceResolver


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
