package me.him188.ani.app.data.source.media.cache.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.source.media.cache.MediaCache
import me.him188.ani.app.data.source.media.cache.MediaCacheManager
import me.him188.ani.app.data.source.media.cache.MediaStats
import me.him188.ani.app.data.source.media.cache.emptyMediaStats
import me.him188.ani.app.data.source.media.fetch.MediaFetcher
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes

/**
 * 表示一个媒体缓存的存储空间, 例如一个本地目录.
 *
 * ## Identity
 *
 * [MediaCacheStorage] and [MediaSource] use the same ID system and,
 * there can be a [MediaSource] with the same ID as this [MediaCacheStorage].
 *
 * By having a [MediaSource] with the same ID,
 * a [MediaCacheStorage] can participate in the [MediaFetcher.newSession] process (and usually it should).
 */
interface MediaCacheStorage : AutoCloseable {
    /**
     * ID of this media source.
     */
    val mediaSourceId: String

    val isEnabled: Flow<Boolean>

    /**
     * 此空间的 [MediaSource]. 调用 [MediaSource.fetch] 则可从此空间中查询缓存, 作为 [Media].
     */
    val cacheMediaSource: MediaSource

    /**
     * 此存储的总体统计
     */
    val stats: MediaStats

    /**
     * A flow that subscribes on all the caches in the storage.
     *
     * Note that to retrieve [Media] (more specifically, [CachedMedia]) from the cache storage, you might want to use [cacheMediaSource].
     */
    val listFlow: Flow<List<MediaCache>>

    /**
     * Finds the existing cache for the media or adds the media to the cache (queue).
     *
     * When this function returns, A new [MediaSource] can then be listed by [listFlow].
     *
     * Caching is made asynchronously. This function might only adds a job to the queue and does not guarantee when the cache will be done.
     *
     * This function returns only if the cache configuration is persisted.
     *
     * @param metadata The request to fetch the media.
     */
    suspend fun cache(media: Media, metadata: MediaCacheMetadata, resume: Boolean = true): MediaCache

    /**
     * Delete the cache if it exists.
     * @return `true` if a cache was deleted, `false` if there wasn't such a cache.
     */
    suspend fun delete(cache: MediaCache): Boolean = deleteFirst { it == cache }

    /**
     * Delete the cache if it exists.
     * @return `true` if a cache was deleted, `false` if there wasn't such a cache.
     */
    suspend fun deleteFirst(predicate: (MediaCache) -> Boolean): Boolean
}

/**
 * 所有缓存项目的大小总和
 */
val MediaCacheStorage.totalSize: Flow<FileSize>
    get() = listFlow.flatMapLatest { caches ->
        combine(caches.map { it.totalSize }) { sizes ->
            sizes.sumOf { it.inBytes }.bytes
        }
    }

/**
 * Number of caches in this storage.
 */
val MediaCacheStorage.count: Flow<Int>
    get() = listFlow.map { it.size }

suspend inline fun MediaCacheStorage.contains(cache: MediaCache): Boolean = listFlow.first().any { it === cache }

val MediaCacheStorage.anyCaching: Flow<Boolean>
    get() = listFlow.flatMapLatest { caches ->
        combine(caches.map { it.progress }) { array -> array.any { it < 1f } }
    }

class TestMediaCacheStorage : MediaCacheStorage {
    override val mediaSourceId: String
        get() = MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID
    override val isEnabled: Flow<Boolean> = flowOf(true)
    override val cacheMediaSource: MediaSource
        get() = throw UnsupportedOperationException()
    override val listFlow: MutableStateFlow<List<MediaCache>> = MutableStateFlow(listOf())
    override val stats: MediaStats = emptyMediaStats()

    override suspend fun cache(media: Media, metadata: MediaCacheMetadata, resume: Boolean): MediaCache {
        throw UnsupportedOperationException()
    }

    override suspend fun delete(cache: MediaCache): Boolean {
        if (listFlow.first().any { it == cache }) {
            listFlow.value = listFlow.first().filter { it != cache }
            return true
        }
        return false
    }

    override suspend fun deleteFirst(predicate: (MediaCache) -> Boolean): Boolean {
        val list = listFlow.first()
        val cache = list.firstOrNull(predicate) ?: return false
        listFlow.value = list.filter { it != cache }
        return true
    }

    override fun close() {
    }
}

