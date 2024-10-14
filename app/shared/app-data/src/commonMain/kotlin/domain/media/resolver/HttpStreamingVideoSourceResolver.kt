/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.media.resolver

import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.HttpStreamingVideoSource
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