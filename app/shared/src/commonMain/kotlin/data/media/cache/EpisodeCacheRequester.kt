package me.him188.ani.app.data.media.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import me.him188.ani.app.data.media.fetch.EpisodeMediaFetchSession
import me.him188.ani.app.data.media.fetch.FetcherMediaSelectorConfig
import me.him188.ani.utils.coroutines.onReplacement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


class EpisodeCacheRequest(
    val subjectId: Int,
    val episodeId: Int,
)

/**
 * 剧集缓存请求工具.
 *
 * 支持根据 `subjectId` 和 `episodeId` [请求][request], 创建一个 [EpisodeMediaFetchSession]
 */
interface EpisodeCacheRequester {
    /**
     * 当前进行中的请求, 为 `null` 时表示没有请求.
     */
    val request: StateFlow<EpisodeCacheRequest?>

    /**
     * 当前进行中的查询.
     */
    val fetchSession: Flow<EpisodeMediaFetchSession>

    /**
     * 取消已有的请求, 并创建一个新的请求.
     */
    fun request(subjectId: Int, episodeId: Int)

    /**
     * 取消当前请求. 若没有请求则不做任何事情.
     */
    fun cancelRequest()
}

typealias EpisodeMediaFetchSessionFactory = (subjectId: Int, episodeId: Int, parentCoroutineContext: CoroutineContext, config: FetcherMediaSelectorConfig) -> EpisodeMediaFetchSession

fun EpisodeCacheRequester(
    createFetchSession: EpisodeMediaFetchSessionFactory,
    flowContext: CoroutineContext = Dispatchers.Default,
): EpisodeCacheRequester = EpisodeCacheRequesterImpl(createFetchSession, flowContext)

private class EpisodeCacheRequesterImpl(
    private val createFetchSession: EpisodeMediaFetchSessionFactory,
    private val flowContext: CoroutineContext = EmptyCoroutineContext,
    private val enableCaching: Boolean = true,
) : EpisodeCacheRequester {
    private fun <T> Flow<T>.cached() = if (enableCaching) {
        shareIn(CoroutineScope(flowContext), started = SharingStarted.WhileSubscribed(5000))
    } else {
        this
    }

    override val request: MutableStateFlow<EpisodeCacheRequest?> = MutableStateFlow(null)

    // filterNotNull, 所以最新的一个 EpisodeMediaFetchSession 不会被关闭, (会在五秒后关闭)
    override val fetchSession = request.filterNotNull().map { req ->
        createFetchSession(
            req.subjectId, req.episodeId, flowContext, FetcherMediaSelectorConfig(
                // 手动缓存的时候要保存设置, 但不要自动选择
                savePreferenceChanges = true,
                autoSelectOnFetchCompletion = false,
                autoSelectLocal = false,
            )
        )
    }.onReplacement {
        it.close()
    }.cached()

    override fun request(subjectId: Int, episodeId: Int) {
        request.value = EpisodeCacheRequest(subjectId, episodeId)
    }

    override fun cancelRequest() {
        request.value = null
    }
}
