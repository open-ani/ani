/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.media.resolver

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