package me.him188.ani.app.data.source.media.cache

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import me.him188.ani.app.data.source.media.cache.storage.MediaCacheStorage
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.utils.coroutines.sampleWithInitial

abstract class MediaCacheManager(
    val storagesIncludingDisabled: List<MediaCacheStorage>,
    final override val backgroundScope: CoroutineScope,
) : HasBackgroundScope { // available via inject
    val enabledStorages: Flow<List<MediaCacheStorage>> = flowOf(storagesIncludingDisabled)
    val storages: List<Flow<MediaCacheStorage?>> by lazy {
        storagesIncludingDisabled.map { storage ->
            flowOf(storage)
        }
    }

    private val cacheListFlow: Flow<List<MediaCache>> by lazy {
        combine(storagesIncludingDisabled.map { it.listFlow }) {
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
        return cacheListFlow.map { list ->
            list.filter { cache ->
                cache.metadata.subjectId == subjectIdString && cache.metadata.episodeId == episodeIdString
            }
        }
    }

    @Stable
    fun listCacheForSubject(
        subjectId: Int,
    ): Flow<List<MediaCache>> {
        val subjectIdString = subjectId.toString()
        return cacheListFlow.map { list ->
            list.filter { cache ->
                cache.metadata.subjectId == subjectIdString
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
        return cacheListFlow.transformLatest { list ->
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
                            hasAnyCaching.totalSize,
                        ) { progress, totalSize ->
                            if (progress == 1f) {
                                EpisodeCacheStatus.Cached(totalSize)
                            } else {
                                EpisodeCacheStatus.Caching(
                                    progress = progress,
                                    totalSize = totalSize,
                                )
                            }
                        },
                    )
                }

                hasAnyCached != null -> {
                    emitAll(
                        hasAnyCached.totalSize.map {
                            EpisodeCacheStatus.Cached(
                                totalSize = it,
                            )
                        },
                    )
                }

                else -> {
                    emit(EpisodeCacheStatus.NotCached)
                }
            }
        }.flowOn(Dispatchers.Default)
    }

    suspend fun deleteCache(cache: MediaCache): Boolean {
        for (storage in enabledStorages.first()) {
            if (storage.delete(cache)) {
                return true
            }
        }
        return false
    }

    suspend fun deleteFirstCache(filter: (MediaCache) -> Boolean): Boolean {
        for (storage in enabledStorages.first()) {
            if (storage.deleteFirst(filter)) {
                return true
            }
        }
        return false
    }

    suspend fun closeAllCaches() = supervisorScope {
        for (storage in enabledStorages.first()) {
            for (mediaCache in storage.listFlow.first()) {
                launch { mediaCache.close() }
            }
        }
    }

    companion object {
        const val LOCAL_FS_MEDIA_SOURCE_ID = "local-file-system"
    }
}

class MediaCacheManagerImpl(
    storagesIncludingDisabled: List<MediaCacheStorage>,
    backgroundScope: CoroutineScope,
) : MediaCacheManager(storagesIncludingDisabled, backgroundScope)
