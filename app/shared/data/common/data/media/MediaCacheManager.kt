package me.him188.ani.app.data.media

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.core.cache.MediaCache
import me.him188.ani.datasources.core.cache.MediaCacheStorage

abstract class MediaCacheManager {
    abstract val storages: List<MediaCacheStorage>

    val listFlow: Flow<List<MediaCache>> by lazy {
        combine(storages.map { it.listFlow }) {
            it.asSequence().flatten().toList()
        }
    }

    @Stable
    fun listCacheForEpisode(
        subjectId: Int,
        episodeId: Int,
    ): Flow<List<MediaCache>> {
        val subjectIdString = subjectId.toString()
        val episodeIdString = episodeId.toString()
        return listFlow.map { list ->
            list.filter { cache ->
                cache.metadata.subjectId == subjectIdString && cache.metadata.episodeId == episodeIdString
            }
        }
    }

    /**
     * Returns the cache status for the episode, updated lively.
     */
    @Stable
    fun cacheStatusForEpisode(
        subjectId: Int,
        episodeId: Int,
    ): Flow<EpisodeCacheStatus> {
        val subjectIdString = subjectId.toString()
        val episodeIdString = episodeId.toString()
        return listFlow.transform { list ->
            var hasAnyCached: MediaCache? = null
            var hasAnyCaching: MediaCache? = null

            for (mediaCache in list) {
                if (mediaCache.metadata.subjectId == subjectIdString && mediaCache.metadata.episodeId == episodeIdString) {
                    hasAnyCached = mediaCache
                    if (mediaCache.progress.firstOrNull() != 1f) {
                        hasAnyCaching = mediaCache
                    }
                }
            }

            emit(
                when {
                    hasAnyCaching != null -> EpisodeCacheStatus.Caching
                    hasAnyCached != null -> EpisodeCacheStatus.Cached(hasAnyCached)
                    else -> EpisodeCacheStatus.NotCached
                }
            )
        }
    }

    companion object {
        const val LOCAL_FS_MEDIA_SOURCE_ID = "local-file-system"
    }
}

@Stable
sealed class EpisodeCacheStatus {
    /**
     * At least one cache is fully downloaded.
     */
    @Stable
    data class Cached(
        val size: Flow<FileSize>
    ) : EpisodeCacheStatus() {
        constructor(cache: MediaCache) : this(cache.totalSize)
    }

    /**
     * No cache is fully downloaded, but at least one cache is downloading.
     */
    @Stable
    data object Caching : EpisodeCacheStatus()

    @Stable
    data object NotCached : EpisodeCacheStatus()
}

class MediaCacheManagerImpl(override val storages: List<MediaCacheStorage>) : MediaCacheManager()