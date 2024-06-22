package me.him188.ani.app.data.media.fetch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transform
import me.him188.ani.app.data.media.instance.MediaSourceInstance
import me.him188.ani.app.data.subject.EpisodeInfo
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.data.subject.nameCnOrName
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.toStringMultiline
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * [MediaFetcher], 为支持从多个 [MediaSource] 并行获取 [Media] 的综合查询工具.
 *
 * 封装了获取查询进度, 重试失败的查询, 以及合并结果等功能.
 *
 * @see MediaSourceMediaFetcher
 */
interface MediaFetcher {
    /**
     * 创建一个惰性查询会话, 从多个 [MediaSource] 并行获取 [Media].
     *
     * 查询仅在 [MediaFetchSession.cumulativeResults] 被 collect 时开始.
     *
     * @param flowContext 传递给 [MediaFetchSession] 的 flow 的 context. (用于 [Flow.flowOn].) 将会与 [MediaSourceMediaFetcher.flowContext] 叠加.
     * @see MediaFetchRequest.Companion.create
     */
    fun newSession(
        request: MediaFetchRequest,
        flowContext: CoroutineContext = EmptyCoroutineContext
    ): MediaFetchSession {
        return newSession(flowOf(request), flowContext)
    }

    /**
     * 创建一个惰性查询会话, 从多个 [MediaSource] 并行获取 [Media].
     *
     * 查询仅在 [MediaFetchSession.cumulativeResults] 被 collect 时开始.
     *
     * @param requestFlow 用于动态请求的 [MediaFetchRequest] 流. 当有新的元素 emit 时, 会重新请求.
     * 如果该 flow 一直不 emit 第一个元素, [MediaFetchSession.hasCompleted] 将会一直为 false.
     * 即使该 flow 不完结, 只要当前的 [MediaFetchRequest] 的查询完成了, [MediaFetchSession.hasCompleted] 也会变为 `true`.
     *
     * @param flowContext 传递给 [MediaFetchSession] 的 flow 的 context. (用于 [Flow.flowOn].) 将会与 [MediaSourceMediaFetcher.flowContext] 叠加.
     *
     * @see MediaFetchRequest.Companion.create
     */
    fun newSession(
        requestFlow: Flow<MediaFetchRequest>,
        flowContext: CoroutineContext = EmptyCoroutineContext,
    ): MediaFetchSession
}

/**
 * 根据 [SubjectInfo] 和 [EpisodeInfo] 创建一个 [MediaFetchRequest].
 * @see createFlow
 */
fun MediaFetchRequest.Companion.create(
    subject: SubjectInfo,
    episode: EpisodeInfo,
): MediaFetchRequest {
    return MediaFetchRequest(
        subjectId = subject.id.toString(),
        episodeId = episode.id.toString(),
        subjectNameCN = subject.nameCnOrName,
        subjectNames = subject.allNames.toSet(),
        episodeSort = episode.sort,
        episodeName = episode.nameCnOrName,
        episodeEp = episode.ep,
    )
}

/**
 * 根据 [SubjectInfo] 和 [EpisodeInfo] 创建一个 [MediaFetchRequest].
 * @see create
 */
fun MediaFetchRequest.Companion.createFlow(
    info: Flow<Pair<SubjectInfo, EpisodeInfo>>,
): Flow<MediaFetchRequest> = info.map { (subject, episode) ->
    MediaFetchRequest.create(subject = subject, episode = episode)
}

class MediaFetcherConfig { // 战未来
    companion object {
        val Default = MediaFetcherConfig()
    }
}

/**
 * 一个 [MediaFetcher] 的实现, 从多个 [MediaSource] 并行[查询][MediaSource.fetch].
 *
 * @param configProvider 配置每一个 [MediaFetchSession]. 会在创建 [MediaFetchSession] 时调用.
 * @param flowContext 传递给 [MediaFetchSession] 的 flow 的 context. (用于 [Flow.flowOn].)
 */
class MediaSourceMediaFetcher(
    private val configProvider: () -> MediaFetcherConfig,
    private val mediaSources: List<MediaSourceInstance>,
    private val flowContext: CoroutineContext = Dispatchers.Default,
) : MediaFetcher {
    private inner class MediaSourceResultImpl(
        override val mediaSourceId: String,
        override val kind: MediaSourceKind,
        private val config: MediaFetcherConfig,
        val disabled: Boolean,
        pagedSources: Flow<SizedSource<MediaMatch>>,
        flowContext: CoroutineContext,
    ) : MediaSourceFetchResult {
        /**
         * 为了确保线程安全, 对 [state] 的写入必须谨慎.
         *
         * [state] 只能在 [results] 的 flow 里, 或者在 [restart] 中修改.
         */
        override val state: MutableStateFlow<MediaSourceFetchState> =
            MutableStateFlow(if (disabled) MediaSourceFetchState.Disabled else MediaSourceFetchState.Idle)
        private val restartCount = MutableStateFlow(0) // 只能在 [restart] 内修改

        override val results by lazy {
            restartCount.flatMapLatest { restartCount ->

                state.value.let { currentState ->
                    // 注意, 读 state 可能是线程不安全的, 因此检查一次 coroutine cancellation.
                    // 因为 [results] flow 被 share, 只有可能有一个协程在执行本代码, 相当于拥有 mutual exclusion.
                    // [state] 还能在 [restart] 中修改, 但 [restart] 在修改 [state] 之后一定会操作 [restartCount], 
                    // 导致本 flow 被 cancel ([flatMapLatest]).
                    currentCoroutineContext().ensureActive()

                    // 此时的 currentState 是可信的

                    if (restartCount == 0 && currentState is MediaSourceFetchState.Disabled)
                        return@flatMapLatest flowOf(emptyList()) // 禁用的数据源, 第一次查询给空列表, 必须要 restart 才能发起查询

                    val lastRestartCount = when (currentState) {
                        is MediaSourceFetchState.PendingSuccess -> currentState.id
                        is MediaSourceFetchState.Completed -> currentState.id
                        else -> -1
                    }
                    if (lastRestartCount == restartCount) {
                        // 这个 [restartCount] 已经跑过一次了, 不要重复跑
                        return@flatMapLatest emptyFlow() // 返回空 flow, 复用 replayCache
                    }
                }

                pagedSources
                    .onStart {
                        state.value = MediaSourceFetchState.Working
                    }
                    .flatMapMerge { sources ->
                        sources.results.map { it.media }
                    }
                    .catch {
                        state.value = MediaSourceFetchState.Failed(it, restartCount)
                        logger.error(it) { "Failed to fetch media from $mediaSourceId because of upstream error" }
                    }
                    .onCompletion {
                        if (it == null) {
                            // catch might have already updated the state
                            if (state.value !is MediaSourceFetchState.Completed) {
                                state.value = MediaSourceFetchState.PendingSuccess(restartCount)
                                // 不能直接诶设置为 Succeed, 必须等待 `shareIn` 完成缓存 (replayCache)
                            }
                        } else {
                            val currentState = state.value
                            if (currentState !is MediaSourceFetchState.Failed) {
                                // downstream (collector) failure
                                state.value = MediaSourceFetchState.Abandoned(it, restartCount)
                                logger.error(it) { "Failed to fetch media from $mediaSourceId because of downstream error" }
                            }
                            // upstream failure re-caught here
                        }
                    }
                    .runningFold(emptyList<Media>()) { acc, list ->
                        acc + list
                    }
                    .map { list ->
                        list.distinctBy { it.mediaId }
                    }
            }.shareIn(
                CoroutineScope(flowContext), replay = 1, started = SharingStarted.WhileSubscribed(),
            ).transform {
                // 必须在 shareIn 更新好 replay 之后再标记为 success, 否则 awaitCompletion 不工作
                // (因为 WhileSubscribed 的 stopTimeoutMillis 为 0)
                try {
                    emit(it)
                } finally {
                    val currentState = state.value
                    if (currentState is MediaSourceFetchState.PendingSuccess) {
                        state.compareAndSet(currentState, MediaSourceFetchState.Succeed(currentState.id))
                        // Ok if we lost race - someone else must set state to Idle from [restart]
                    }
                }
            }
        }

        @Synchronized // 不允许同时调用 restart
        override fun restart() {
            when (val value = state.value) {
                is MediaSourceFetchState.Completed,
                MediaSourceFetchState.Disabled -> {
                    restartCount.value += 1
                    // 必须使用 CAS
                    // 如果 [results] 现在有人 collect, [state] 可能会被变更. 
                    // 我们只需要在它没有被其他人修改的时候, 把它修改为 [Idle].
                    state.compareAndSet(value, MediaSourceFetchState.Idle)
                    // Ok if we lost race - the [results] must be running
                }

                MediaSourceFetchState.Idle -> {}
                MediaSourceFetchState.Working -> {}
            }
        }
    }

    private inner class MediaFetchSessionImpl(
        request: Flow<MediaFetchRequest>, // must be shared
        private val config: MediaFetcherConfig,
        private val flowContext: CoroutineContext,
    ) : MediaFetchSession {
        override val request: Flow<MediaFetchRequest> =
            request.shareIn(CoroutineScope(flowContext), started = SharingStarted.WhileSubscribed(), replay = 1)

        override val mediaSourceResults: List<MediaSourceFetchResult> = mediaSources.map { instance ->
            MediaSourceResultImpl(
                mediaSourceId = instance.mediaSourceId,
                instance.source.kind,
                config,
                disabled = !instance.isEnabled,
                pagedSources = request
                    .onEach {
                        logger.info { "MediaFetchSessionImpl pagedSources creating, request: \n${it.toStringMultiline()}" }
                    }
                    .map {
                        instance.source.fetch(it)
                    },
                flowContext = flowContext,
            )
        }

        override val cumulativeResults: Flow<List<Media>> =
            combine(mediaSourceResults.map { it.results }) { lists ->
                lists.asSequence().flatten().toList()
            }.map { list ->
                list.distinctBy { it.mediaId } // distinct globally by id, just to be safe
            }.flowOn(flowContext)

        override val hasCompleted = if (mediaSourceResults.isEmpty()) {
            flowOf(true)
        } else {
            combine(mediaSourceResults.map { it.state }) { states ->
                states.all { it is MediaSourceFetchState.Completed || it is MediaSourceFetchState.Disabled }
            }.flowOn(flowContext)
        }
    }

    override fun newSession(requestFlow: Flow<MediaFetchRequest>, flowContext: CoroutineContext): MediaFetchSession {
        return MediaFetchSessionImpl(requestFlow, configProvider(), this.flowContext + flowContext)
    }

    private companion object {
        private val logger = logger<MediaSourceMediaFetcher>()
    }
}
