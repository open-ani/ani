package me.him188.ani.app.data.source.media.cache

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
