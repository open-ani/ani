package me.him188.ani.app.data.media.resolver

import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.torrent.FileVideoSource
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.ResourceLocation
import java.io.File

class LocalFileVideoSourceResolver : VideoSourceResolver {
    override fun supports(media: Media): Boolean {
        return media.download is ResourceLocation.LocalFile
    }

    override suspend fun resolve(media: Media, episode: EpisodeSort): VideoSource<*> {
        when (val download = media.download) {
            is ResourceLocation.LocalFile -> {
                return FileVideoSource(
                    File(download.filePath)
                )
            }

            else -> throw UnsupportedMediaException(media)
        }
    }
}