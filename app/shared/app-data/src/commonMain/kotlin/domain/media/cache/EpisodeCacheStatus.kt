/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.media.cache

import androidx.compose.runtime.Stable
import me.him188.ani.app.tools.Progress
import me.him188.ani.datasources.api.topic.FileSize

@Stable
sealed class EpisodeCacheStatus {

    /**
     * At least one cache is fully downloaded.
     */
    @Stable
    data class Cached(
        val totalSize: FileSize,
    ) : EpisodeCacheStatus()

    /**
     * No cache is fully downloaded, but at least one cache is downloading.
     */
    @Stable
    data class Caching(
        /**
         * This will not be 1f (on which it will become [Cached]).
         */
        val progress: Progress,
        val totalSize: FileSize,
    ) : EpisodeCacheStatus()

    @Stable
    data object NotCached : EpisodeCacheStatus()
}

@Stable
fun EpisodeCacheStatus.isCachedOrCaching(): Boolean {
    return this is EpisodeCacheStatus.Cached || this is EpisodeCacheStatus.Caching
}
