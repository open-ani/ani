package me.him188.ani.app.data.source.media.resolver

import kotlinx.io.files.Path
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.data.FileVideoSource
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.utils.io.inSystem

class LocalFileVideoSourceResolver : VideoSourceResolver {
    override suspend fun supports(media: Media): Boolean {
        return media.download is ResourceLocation.LocalFile
    }

    override suspend fun resolve(media: Media, episode: EpisodeMetadata): VideoSource<*> {
        when (val download = media.download) {
            is ResourceLocation.LocalFile -> {
                return FileVideoSource(
                    Path(download.filePath).inSystem,
                    media.extraFiles,
                )
            }

            else -> throw UnsupportedMediaException(media)
        }
    }
}