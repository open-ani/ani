@file:Suppress("LeakingThis")

package me.him188.ani.app.ui.subject.cache

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.data.media.cache.MediaCacheEngine
import me.him188.ani.app.data.media.cache.MediaCacheStorage
import me.him188.ani.app.data.media.cache.requester.CacheRequestStage
import me.him188.ani.app.data.media.cache.requester.EpisodeCacheRequest
import me.him188.ani.app.data.media.cache.requester.EpisodeCacheRequester
import me.him188.ani.app.data.media.cache.requester.request
import me.him188.ani.app.data.media.fetch.MediaFetchSession
import me.him188.ani.app.data.media.selector.MediaSelector
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.topic.isDoneOrDropped
import kotlin.coroutines.CoroutineContext


/**
 * 一个条目的一个剧集的缓存状态
 */
@Stable
open class EpisodeCacheState(
    val episodeId: Int,
    cacheRequesterLazy: () -> EpisodeCacheRequester,
    info: Flow<EpisodeCacheInfo>,
    cacheStatusFlow: Flow<EpisodeCacheStatus>,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    internal val cacheRequester by lazy(cacheRequesterLazy)

    /**
     * [episodeId] 对应的剧集信息, 初始值为 [EpisodeCacheInfo.Placeholder], 表示正在加载.
     * @see isInfoLoading
     */
    val info: EpisodeCacheInfo by info.produceState(EpisodeCacheInfo.Placeholder)

    /**
     * [info] 是否仍然在加载中
     */
    val isInfoLoading by derivedStateOf {
        this.info === EpisodeCacheInfo.Placeholder
    }

    private val currentStage: CacheRequestStage by cacheRequester.stage.produceState()

    /**
     * 当前需要展示给用户选择的 [MediaSelector]
     */
    val currentSelectMediaTask by derivedStateOf {
        (currentStage as? CacheRequestStage.Working)?.let {
            SelectMediaTask(
                episode = this,
                fetchSession = it.fetchSession,
                mediaSelector = it.mediaSelector,
            )
        }
    }

    /**
     * 当前需要展示给用户选择的存储位置列表
     */
    val currentSelectStorageTask by derivedStateOf {
        (currentStage as? CacheRequestStage.SelectStorage)?.let {
            SelectStorageTask(
                episode = this,
                options = it.storages,
            )
        }
    }


    /**
     * 该剧集当前的缓存状态. `null` 表示还在加载中
     */
    val cacheStatus: EpisodeCacheStatus? by cacheStatusFlow.produceState(null)

    /**
     * 是否允许缓存该剧集
     */
    val canCache: Boolean by derivedStateOf {
        // 没看过的剧集才能缓存
        !this.info.watchStatus.isDoneOrDropped()
    }

    /**
     * 是否正在进行添加缓存或删除缓存等有进度的操作
     */
    val actionTasker = MonoTasker(backgroundScope)

    val showProgressIndicator by derivedStateOf {
        actionTasker.isRunning || currentStage is CacheRequestStage.Working
    }
}

/**
 * @see EpisodeCacheState.currentSelectMediaTask
 */
@Stable
class SelectMediaTask(
    val episode: EpisodeCacheState,
    val fetchSession: MediaFetchSession,
    val mediaSelector: MediaSelector
)

/**
 * @see EpisodeCacheState.currentSelectStorageTask
 */
@Stable
class SelectStorageTask(
    val episode: EpisodeCacheState,
    val options: List<MediaCacheStorage>
)

/**
 * 从用户侧收集到的, 期望为一个剧集创建的缓存的目标 [Media], [MediaCacheStorage], 以及其他相关信息.
 *
 * @see EpisodeCacheListStateImpl.onRequestCacheComplete
 */
data class EpisodeCacheTargetInfo(
    val episode: EpisodeCacheState,
    val request: EpisodeCacheRequest,
    val media: Media,
    val storage: MediaCacheStorage,
    /**
     * 用于提供给 [MediaCacheEngine.createCache] 和 [CachedMedia] 的缓存元数据. 数据主要来自 [request] 的条目和剧集信息.
     */
    val metadata: MediaCacheMetadata,
)


///////////////////////////////////////////////////////////////////////////
// Testing
///////////////////////////////////////////////////////////////////////////

@Stable
class TestEpisodeCacheState(
    episodeId: Int,
    cacheRequesterLazy: () -> EpisodeCacheRequester,
    val infoFlow: MutableStateFlow<EpisodeCacheInfo>,
    val cacheStatusFlow: MutableStateFlow<EpisodeCacheStatus>,
    parentCoroutineContext: CoroutineContext,
) : EpisodeCacheState(
    episodeId,
    cacheRequesterLazy,
    infoFlow,
    cacheStatusFlow,
    parentCoroutineContext,
)