package me.him188.ani.app.data.media

import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.torrent.FileVideoSource
import me.him188.ani.app.videoplayer.torrent.TorrentVideoSource
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.ResourceLocation
import java.io.File

/**
 * Resolves the video description [Media] into a [VideoSource] which can be opened to play.
 */
interface VideoSourceResolver {
    /**
     * Check if this resolver supports the given media's [Media.download].
     */
    fun supports(media: Media): Boolean

    /**
     * Resolve the given media into a [VideoSource] which can be opened to play.
     *
     * @throws UnsupportedOperationException if the media cannot be resolved.
     * Use [supports] to check if the media can be resolved.
     */
    suspend fun resolve(media: Media, episode: EpisodeSort): VideoSource<*>

    companion object {
        fun from(vararg resolvers: VideoSourceResolver): VideoSourceResolver {
            return ChainedVideoSourceResolver(resolvers.toList())
        }

        fun from(resolvers: Iterable<VideoSourceResolver>): VideoSourceResolver {
            return ChainedVideoSourceResolver(resolvers.toList())
        }
    }
}

class UnsupportedMediaException(
    val media: Media,
) : UnsupportedOperationException("Media is not supported: $media")

private class ChainedVideoSourceResolver(
    private val resolvers: List<VideoSourceResolver>
) : VideoSourceResolver {
    override fun supports(media: Media): Boolean {
        return resolvers.any { it.supports(media) }
    }

    override suspend fun resolve(media: Media, episode: EpisodeSort): VideoSource<*> {
        return resolvers.firstOrNull { it.supports(media) }?.resolve(media, episode)
            ?: throw UnsupportedMediaException(media)
    }
}

class TorrentVideoSourceResolver(
    private val torrentManager: TorrentManager,
) : VideoSourceResolver {
    override fun supports(media: Media): Boolean {
        return media.download is ResourceLocation.HttpTorrentFile || media.download is ResourceLocation.MagnetLink
    }

    override suspend fun resolve(media: Media, episode: EpisodeSort): VideoSource<*> {
        return when (val location = media.download) {
            is ResourceLocation.HttpTorrentFile,
            is ResourceLocation.MagnetLink -> {
                TorrentVideoSource(
                    torrentManager.downloader.await().fetchTorrent(location.uri)
                )
            }

            else -> throw UnsupportedMediaException(media)
        }
    }
}


class LocalFileVideoSourceResolver : VideoSourceResolver {
    override fun supports(media: Media): Boolean {
        return media.download is ResourceLocation.LocalFile
    }

    override suspend fun resolve(media: Media, episode: EpisodeSort): VideoSource<*> {
        when (media.download) {
            is ResourceLocation.LocalFile -> {
                return FileVideoSource(
                    File((media.download as ResourceLocation.LocalFile).filePath)
                )
            }

            else -> throw UnsupportedMediaException(media)
        }
    }
}
