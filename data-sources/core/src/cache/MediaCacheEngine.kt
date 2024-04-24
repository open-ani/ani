package me.him188.ani.datasources.core.cache

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.topic.FileSize
import kotlin.coroutines.CoroutineContext

@Stable
interface MediaStats {
    /**
     * Total amount of bytes uploaded
     */
    val uploaded: Flow<FileSize>
    val downloaded: Flow<FileSize>

    val uploadRate: Flow<FileSize>
    val downloadRate: Flow<FileSize>
}

/**
 * Only for tests
 */
fun emptyMediaStats() = object : MediaStats {
    override val uploaded: Flow<FileSize> = flowOf(FileSize.Zero)
    override val downloaded: Flow<FileSize> = flowOf(FileSize.Zero)
    override val uploadRate: Flow<FileSize> = flowOf(FileSize.Zero)
    override val downloadRate: Flow<FileSize> = flowOf(FileSize.Zero)
}

/**
 * Handles the actual downloading of a [Media]
 */
interface MediaCacheEngine {
    /**
     * 此引擎的总体统计
     */
    val stats: MediaStats

//    /**
//     * Total file size occupied on the disk.
//     */
//    val totalSize: Flow<FileSize>

    /**
     * Restores a cache that was created by [createCache].
     *
     * @param metadata from `MediaCache.media.cacheMetadata` from [createCache]
     *
     * Returns `null` if the cache was deleted or invalid.
     */
    suspend fun restore(
        origin: Media,
        metadata: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): MediaCache?

    /**
     * Creates a new cache.
     */
    suspend fun createCache(
        origin: Media,
        request: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): MediaCache

    /**
     * 在本地缓存中删除所有未在 [all] 中找到对应 [MediaCache] 的文件.
     */
    suspend fun deleteUnusedCaches(all: List<MediaCache>)
}