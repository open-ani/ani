/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.media.cache.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.him188.ani.app.domain.media.cache.MediaCache
import me.him188.ani.app.domain.media.cache.MediaCacheManager
import me.him188.ani.app.domain.media.cache.engine.MediaStats
import me.him188.ani.app.domain.media.fetch.MediaFetcher
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

    /**
     * 此空间的 [MediaSource]. 调用 [MediaSource.fetch] 则可从此空间中查询缓存, 作为 [Media].
     */
    val cacheMediaSource: MediaSource

    /**
     * 此存储的总体统计
     */
    val stats: Flow<MediaStats>

    /**
     * A flow that subscribes on all the caches in the storage.
     *
     * Note that to retrieve [Media] (more specifically, [CachedMedia]) from the cache storage, you might want to use [cacheMediaSource].
     */
    val listFlow: Flow<List<_root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache>>

    /**
     * 重新加载那些上次 APP 运行时保存在本地的缓存.
     *
     * 通常在 APP 启动时调用.
     */
    suspend fun restorePersistedCaches()

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
    suspend fun cache(media: Media, metadata: MediaCacheMetadata, resume: Boolean = true): _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache

    /**
     * Delete the cache if it exists.
     * @return `true` if a cache was deleted, `false` if there wasn't such a cache.
     */
    suspend fun delete(cache: _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache): Boolean = deleteFirst { it == cache }

    /**
     * Delete the cache if it exists.
     * @return `true` if a cache was deleted, `false` if there wasn't such a cache.
     */
    suspend fun deleteFirst(predicate: (_root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache) -> Boolean): Boolean
}

/**
 * 所有缓存项目的大小总和
 */
val MediaCacheStorage.totalSize: Flow<FileSize>
    get() = listFlow.flatMapLatest { caches ->
        combine(caches.map { cache -> cache.fileStats.map { it.totalSize } }) { sizes ->
            sizes.sumOf { it.inBytes }.bytes
        }
    }

/**
 * Number of caches in this storage.
 */
val MediaCacheStorage.count: Flow<Int>
    get() = listFlow.map { it.size }

suspend inline fun MediaCacheStorage.contains(cache: _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache): Boolean = listFlow.first().any { it === cache }

class TestMediaCacheStorage : MediaCacheStorage {
    override val mediaSourceId: String
        get() = MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID
    override val cacheMediaSource: MediaSource
        get() = throw UnsupportedOperationException()
    override val listFlow: MutableStateFlow<List<_root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache>> = MutableStateFlow(listOf())
    override suspend fun restorePersistedCaches() {
    }

    override val stats: Flow<MediaStats> = flowOf(MediaStats.Unspecified)

    override suspend fun cache(media: Media, metadata: MediaCacheMetadata, resume: Boolean): _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache {
        throw UnsupportedOperationException()
    }

    override suspend fun delete(cache: _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache): Boolean {
        if (listFlow.first().any { it == cache }) {
            listFlow.value = listFlow.first().filter { it != cache }
            return true
        }
        return false
    }

    override suspend fun deleteFirst(predicate: (_root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache) -> Boolean): Boolean {
        val list = listFlow.first()
        val cache = list.firstOrNull(predicate) ?: return false
        listFlow.value = list.filter { it != cache }
        return true
    }

    override fun close() {
    }
}

