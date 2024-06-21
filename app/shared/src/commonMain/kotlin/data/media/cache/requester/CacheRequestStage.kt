package me.him188.ani.app.data.media.cache.requester

import me.him188.ani.app.data.media.cache.MediaCache
import me.him188.ani.app.data.media.cache.MediaCacheStorage
import me.him188.ani.app.data.media.fetch.MediaFetchSession
import me.him188.ani.app.data.media.fetch.MediaSourceFetchResult
import me.him188.ani.app.data.media.selector.MediaSelector
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata

/**
 * @see EpisodeCacheRequester.stage
 */
sealed interface CacheRequestStage {
    data object Idle : CacheRequestStage

    sealed interface Working : CacheRequestStage {
        /**
         * 当前请求的信息.
         */
        val request: EpisodeCacheRequest

        /**
         * 当前的惰性查询会话. 可用于获取各数据源的查询状态和可选的 [Media] 列表
         */
        val fetchSession: MediaFetchSession

        /**
         * 基于 [fetchSession] 的结果的 [MediaSelector], 可用于获取 [MediaSelector.alliance] 等信息
         */
        val mediaSelector: MediaSelector
    }

    sealed interface SelectMedia : Working {
        /**
         * 选择目标 [Media] 并进入下一阶段.
         *
         * @throws StaleStageException
         */
        suspend fun select(media: Media): SelectStorage

        /**
         * 尝试从已有的剧集缓存中, 选择一个 [Media.episodeRange] 包含当前请求的剧集序号的 [Media].
         *
         * 成功时进入下一阶段, 若未找到则返回 `null`.
         *
         * @param episodeCacheStats 该条目下的剧集的缓存状态
         * @throws StaleStageException
         */
        // todo: use tryAutoSelectByCachedSeason
        suspend fun tryAutoSelectByCachedSeason(
            episodeCacheStats: List<MediaCache>,
        ): SelectStorage?

        /**
         * 尝试自动选择一个 [Media] 并进入下一阶段.
         *
         * 若没有可选的 [Media] 或者用户已经手动选择过一个 [Media] 则返回 `null`.
         *
         * @throws StaleStageException
         */
        suspend fun tryAutoSelectByPreference(): SelectStorage?

        /**
         * 拒绝选择, 返回上一状态
         * @throws StaleStageException
         */
        suspend fun cancel(): Idle
    }

    sealed interface SelectStorage : Working {
        val storages: List<MediaCacheStorage>

        /**
         * 选择目标 [MediaCacheStorage] 并进入下一阶段.
         * @throws StaleStageException
         */
        suspend fun select(storage: MediaCacheStorage): Done

        /**
         * 尝试选择包含这个 [MediaCache] 的 [MediaCacheStorage].
         *
         * 成功时进入下一阶段, 若未找到则返回 `null`.
         * @throws StaleStageException
         */
        suspend fun trySelectByCache(mediaCache: MediaCache): Done?

        /**
         * 拒绝选择, 返回上一状态
         * @throws StaleStageException
         */
        suspend fun cancel(): SelectMedia
    }

    data class Done(
        val request: EpisodeCacheRequest,
        val media: Media,
        val storage: MediaCacheStorage,
        val metadata: MediaCacheMetadata,
    ) : CacheRequestStage
}

val CacheRequestStage.mediaSourceResults: List<MediaSourceFetchResult>
    get() = when (this) {
        is CacheRequestStage.Done -> emptyList()
        CacheRequestStage.Idle -> emptyList()
        is CacheRequestStage.Working -> fetchSession.results
    }

/**
 * 如果 [CacheRequestStage.SelectStorage.storages] 只有一个选项, 则选择, 否则返回 `null`.
 */
suspend inline fun CacheRequestStage.SelectStorage.trySelectSingle(): CacheRequestStage.Done? {
    storages.singleOrNull()?.let { return select(it) }
    return null
}

/**
 * 当尝试操作一个已经过期了的 [CacheRequestStage] 时抛出. 过期指的是 [CacheRequestStage] 已经不再是当前的 [EpisodeCacheRequester.stage].
 */
class StaleStageException(
    requestingStage: CacheRequestStage,
    currentStage: CacheRequestStage,
) : IllegalStateException("Stage $requestingStage is stale, current stage is $currentStage")
