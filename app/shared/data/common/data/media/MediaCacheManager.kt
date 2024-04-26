package me.him188.ani.app.data.media

import androidx.compose.runtime.Stable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.transform
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.core.cache.MediaCache
import me.him188.ani.datasources.core.cache.MediaCacheStorage
import me.him188.ani.utils.coroutines.sampleWithInitial

abstract class MediaCacheManager {
    abstract val storages: List<MediaCacheStorage>

    private val listFlow: Flow<List<MediaCache>> by lazy {
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
     * Returns the cache status for the episode, updated lively and sampled for 1000ms.
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
                    if (mediaCache.finished.firstOrNull() != true) {
                        hasAnyCaching = mediaCache
                    }
                }
            }

            when {
                hasAnyCaching != null -> {
                    emitAll(
                        combine(
                            hasAnyCaching.progress
                                .sampleWithInitial(1000) // Sample might not emit the last value
                                .onCompletion { if (it == null) emit(1f) }, // Always emit 1f on finish
                            hasAnyCaching.totalSize
                        ) { progress, totalSize ->
                            if (progress == 1f) {
                                EpisodeCacheStatus.Cached(totalSize)
                            } else {
                                EpisodeCacheStatus.Caching(
                                    progress = progress,
                                    totalSize = totalSize
                                )
                            }
                        }
                    )
                }

                hasAnyCached != null -> {
                    emitAll(hasAnyCached.totalSize.map {
                        EpisodeCacheStatus.Cached(
                            totalSize = it
                        )
                    })
                }

                else -> {
                    emit(EpisodeCacheStatus.NotCached)
                }
            }
        }.flowOn(Dispatchers.Default)
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
        val progress: Float?, // null means still connecting
        val totalSize: FileSize,
    ) : EpisodeCacheStatus()

    @Stable
    data object NotCached : EpisodeCacheStatus()
}

class MediaCacheManagerImpl(override val storages: List<MediaCacheStorage>) : MediaCacheManager()