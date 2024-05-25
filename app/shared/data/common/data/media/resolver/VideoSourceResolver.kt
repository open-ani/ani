package me.him188.ani.app.data.media.resolver

import androidx.compose.runtime.Composable
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media

/**
 * 根据 [EpisodeMetadata] 中的集数信息和 [Media.location] 中的下载方式,
 * 解析一个 [Media] 为可以播放的 [VideoSource].
 *
 * 实际操作涉及创建种子下载任务, 寻找文件等.
 */
interface VideoSourceResolver {
    /**
     * Checks if this resolver supports the given media's [Media.download].
     */
    suspend fun supports(media: Media): Boolean

    /**
     * "挂载" 到 composable 中, 以便进行需要虚拟 UI 的操作, 例如 WebView
     */
    @Composable
    fun ComposeContent() {
    }

    /**
     * 根据 [EpisodeMetadata] 中的集数信息和 [Media.location] 中的下载方式,
     * 解析一个 [Media] 为可以播放的 [VideoSource].
     *
     * @param episode Target episode to resolve, because a media can have multiple episodes.
     *
     * @throws UnsupportedMediaException if the media cannot be resolved.
     * Use [supports] to check if the media can be resolved.
     * @throws VideoSourceResolutionException if the video source cannot be resolved.
     */
    @Throws(VideoSourceResolutionException::class)
    suspend fun resolve(media: Media, episode: EpisodeMetadata): VideoSource<*>

    companion object {
        fun from(vararg resolvers: VideoSourceResolver): VideoSourceResolver {
            return ChainedVideoSourceResolver(resolvers.toList())
        }

        fun from(resolvers: Iterable<VideoSourceResolver>): VideoSourceResolver {
            return ChainedVideoSourceResolver(resolvers.toList())
        }
    }
}

data class EpisodeMetadata(
    val title: String,
    val ep: EpisodeSort,
    val sort: EpisodeSort,
)

private class ChainedVideoSourceResolver(
    private val resolvers: List<VideoSourceResolver>
) : VideoSourceResolver {
    override suspend fun supports(media: Media): Boolean {
        return resolvers.any { it.supports(media) }
    }

    @Composable
    override fun ComposeContent() {
        this.resolvers.forEach {
            it.ComposeContent()
        }
    }

    override suspend fun resolve(media: Media, episode: EpisodeMetadata): VideoSource<*> {
        return resolvers.firstOrNull { it.supports(media) }?.resolve(media, episode)
            ?: throw UnsupportedMediaException(media)
    }
}

class UnsupportedMediaException(
    val media: Media,
) : UnsupportedOperationException("Media is not supported: $media")

enum class ResolutionFailures {
    /**
     * 下载种子文件超时或者解析失败
     */
    FETCH_TIMEOUT,

    /**
     * 引擎自身错误 (bug)
     */
    ENGINE_ERROR,
}

class VideoSourceResolutionException(
    val reason: ResolutionFailures,
    override val cause: Throwable? = null,
) : Exception("Failed to resolve video source: $reason", cause)
