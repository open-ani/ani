package me.him188.ani.app.data.source.media.resolver

import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.torrent.HttpStreamingVideoSource
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.matcher.WebVideo
import me.him188.ani.datasources.api.topic.ResourceLocation

class HttpStreamingVideoSourceResolver : VideoSourceResolver {
    override suspend fun supports(media: Media): Boolean {
        return media.download is ResourceLocation.HttpStreamingFile
    }

    override suspend fun resolve(media: Media, episode: EpisodeMetadata): VideoSource<*> {
        if (!supports(media)) throw UnsupportedMediaException(media)
        return HttpStreamingVideoSource(
            media.download.uri,
            media.originalTitle,
            WebVideo(media.download.uri, emptyMap()),
            media.extraFiles,
        )
    }
}