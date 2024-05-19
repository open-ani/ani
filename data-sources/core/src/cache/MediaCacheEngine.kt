package me.him188.ani.datasources.core.cache

import kotlinx.coroutines.flow.Flow
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import kotlin.coroutines.CoroutineContext

/**
 * 媒体缓存引擎, 处理 [MediaCache] 的创建与恢复, 以及实际的下载操作.
 *
 * [MediaCacheEngine] 的作用可以简单理解为是使用 libtorrent4j (内嵌) 还是 qBittorrent (本机局域网) 来下载种子.
 *
 * ### 元数据管理
 *
 * [Media] 和 [MediaCacheMetadata] 是一个 [MediaCache] 必要的数据.
 * [Media] 表示该 [MediaCache] 缓存的是哪个视频和这个视频自身的信息, [MediaCacheMetadata] 则包含该视频属于哪个番剧的哪一集等来源信息.
 *
 * [MediaCacheEngine] 不考虑缓存元数据的存储方式, 即不考虑目前有多少缓存.
 * 它只根据 [Media] 与 [MediaCacheMetadata] 来启动或恢复下载任务并返回一个 [MediaCache] 示例.
 * [MediaCacheStorage] 负责持久化 [Media] 与 [MediaCacheMetadata], 然后调用 [MediaCacheEngine.restore] 恢复下载任务.
 *
 * ### 下载数据存储位置
 *
 * [MediaCacheEngine] 决定种子数据的实际存储位置, 但该目录
 */
interface MediaCacheEngine {
//    /**
//     * 引擎的唯一 ID. 例如用于区分是 libtorrent4j (内嵌) 还是 qBittorrent (本机局域网).
//     *
//     * 此 ID 与 [MediaSource.mediaSourceId] 不同.
//     */
//    val mediaCacheEngineId: String

    val isEnabled: Flow<Boolean>

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
     * 创建一个新的返回
     */
    suspend fun createCache(
        origin: Media,
        request: MediaCacheMetadata,
        parentContext: CoroutineContext
    ): MediaCache

    /**
     * 删除所有未在 [all] 中找到对应 [MediaCache] 的文件.
     */
    suspend fun deleteUnusedCaches(all: List<MediaCache>)
}