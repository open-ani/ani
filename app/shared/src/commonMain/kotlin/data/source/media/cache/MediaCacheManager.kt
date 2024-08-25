package me.him188.ani.app.data.source.media.cache

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import me.him188.ani.app.data.source.media.cache.storage.MediaCacheStorage
import me.him188.ani.app.ui.foundation.HasBackgroundScope

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
                    hasAnyCaching = mediaCache
                    if (mediaCache.isFinished()) {
                        hasAnyCached = mediaCache
                    }
                }
            }

            when {
                hasAnyCached != null -> {
                    emitAll(
                        hasAnyCached.fileStats.map {
                            EpisodeCacheStatus.Cached(totalSize = it.totalSize)
                        },
                    )
                }

                hasAnyCaching != null -> {
                    emitAll(
                        hasAnyCaching.fileStats.map {
                            val progress = it.downloadProgress
                            if (progress.isFinished) {
                                EpisodeCacheStatus.Cached(it.totalSize)
                            } else {
                                EpisodeCacheStatus.Caching(
                                    progress = progress,
                                    totalSize = it.totalSize,
                                )
                            }
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
