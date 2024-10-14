/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.media.cache.requester

import kotlinx.coroutines.flow.StateFlow
import me.him188.ani.app.domain.media.cache.MediaCache
import me.him188.ani.app.domain.media.cache.engine.MediaCacheEngine
import me.him188.ani.app.domain.media.cache.storage.MediaCacheStorage
import me.him188.ani.app.domain.media.fetch.MediaFetchSession
import me.him188.ani.app.domain.media.fetch.MediaSourceFetchResult
import me.him188.ani.app.domain.media.selector.MediaSelector
import me.him188.ani.datasources.api.CachedMedia
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
         *
         * 注意, 若调用 [MediaSelector.select], [MediaSelector.trySelectCached], [MediaSelector.trySelectDefault] 等修改 [MediaSelector.selected] 的方法,
         * 不会更新 stage. 请使用 [SelectMedia.select], [SelectMedia.tryAutoSelectByCachedSeason], [SelectMedia.tryAutoSelectByPreference] 等方法替代.
         */
        val mediaSelector: MediaSelector

        /**
         * 是否尝试过任何 `trySelect` 函数, 无论是否成功.
         * 但注意, 若操作时状态不正确, 抛出了 [StaleStageException], 则不会更新该值.
         *
         * 一旦此值为 `true`, 它不可能再变为 `false`.
         */
        val attemptedTrySelect: StateFlow<Boolean>

        /**
         * 将 [attemptedTrySelect] 设置为 `true`.
         */
        fun markAttemptedTrySelect()
    }

    sealed interface SelectMedia : Working {
        /**
         * 选择目标 [Media] 并进入下一阶段.
         *
         * 与 [MediaSelector.select] 不同, 该方法会更新 stage. 详见 [Working.mediaSelector].
         *
         * @throws StaleStageException
         */
        suspend fun select(media: Media): SelectStorage

        /**
         * 尝试从已有的剧集缓存中, 选择一个 [Media.episodeRange] 包含当前请求的剧集序号的 [Media].
         *
         * 如果失败, 返回 `null`.
         *
         * 成功时会尝试根据上次缓存使用的 storage 选择. 如果选择成功, 将返回 [Done]. 否则返回 [SelectStorage].
         *
         * 与 [MediaSelector.select] 不同, 该方法会更新 stage. 详见 [Working.mediaSelector].
         * 将会更新 [attemptedTrySelect].
         *
         * @param existingCaches 该条目下的剧集的缓存状态
         * @throws StaleStageException
         */
        suspend fun tryAutoSelectByCachedSeason(
            existingCaches: List<_root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache>,
        ): MediaSelected?

        /**
         * 尝试自动选择一个 [Media] 并进入下一阶段.
         *
         * 若没有可选的 [Media] 或者用户已经手动选择过一个 [Media] 则返回 `null`.
         *
         * 与 [MediaSelector.select] 不同, 该方法会更新 stage. 详见 [Working.mediaSelector].
         * 将会更新 [attemptedTrySelect].
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

    /**
     * 已经完成了 [SelectMedia].
     */
    sealed interface MediaSelected : CacheRequestStage

    sealed interface SelectStorage : Working, MediaSelected {
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
         * 将会更新 [attemptedTrySelect].
         *
         * @throws StaleStageException
         */
        suspend fun trySelectByCache(mediaCache: _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache): Done?

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
        /**
         * 用于提供给 [MediaCacheEngine.createCache] 和 [CachedMedia] 的缓存元数据. 数据主要来自 [request] 的条目和剧集信息.
         */
        val metadata: MediaCacheMetadata,
    ) : CacheRequestStage, MediaSelected
}

/**
 * 从各个数据源获取的结果
 */
val CacheRequestStage.mediaSourceResults: List<MediaSourceFetchResult>
    get() = when (this) {
        is CacheRequestStage.Done -> emptyList()
        CacheRequestStage.Idle -> emptyList()
        is CacheRequestStage.Working -> fetchSession.mediaSourceResults
    }

/**
 * 如果 [CacheRequestStage.SelectStorage.storages] 只有一个选项, 则选择, 否则返回 `null`.
 */
suspend inline fun CacheRequestStage.SelectStorage.trySelectSingle(): CacheRequestStage.Done? {
    try {
        storages.singleOrNull()?.let { return select(it) }
        return null
    } finally {
        markAttemptedTrySelect()
    }
}

/**
 * 当尝试操作一个已经过期了的 [CacheRequestStage] 时抛出. 过期指的是 [CacheRequestStage] 已经不再是当前的 [EpisodeCacheRequester.stage].
 */
class StaleStageException(
    requestingStage: CacheRequestStage,
    currentStage: CacheRequestStage,
) : IllegalStateException("Stage $requestingStage is stale, current stage is $currentStage")
