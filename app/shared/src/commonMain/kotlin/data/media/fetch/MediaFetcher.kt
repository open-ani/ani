package me.him188.ani.app.data.media.fetch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
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
import me.him188.ani.utils.coroutines.cancellableCoroutineScope
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import kotlin.coroutines.CoroutineContext

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
     * @param flowContext 传递给 [MediaFetchSession] 的 flow 的 context. (用于 [Flow.flowOn])
     */
    fun newSession(request: MediaFetchRequest, flowContext: CoroutineContext = Dispatchers.Default): MediaFetchSession {
        return newSession(flowOf(request), flowContext)
    }

    /**
     * 创建一个惰性查询会话, 从多个 [MediaSource] 并行获取 [Media].
     *
     * 查询仅在 [MediaFetchSession.cumulativeResults] 被 collect 时开始.
     *
     * @param requestFlow 用于动态请求的 [MediaFetchRequest] 流. 当有新的元素 emit 时, 会重新请求.
     * 如果该 flow 一直不 emit 第一个元素, [MediaFetchSession.hasCompleted] 将会一直为 false.
     *
     * 即使该 flow 不完结, 只要当前的 [MediaFetchRequest] 的查询完成了, [MediaFetchSession.hasCompleted] 也会变为 `true`.
     */
    fun newSession(
        requestFlow: Flow<MediaFetchRequest>,
        flowContext: CoroutineContext = Dispatchers.Default
    ): MediaFetchSession
}

/**
 * 根据 [SubjectInfo] 和 [EpisodeInfo] 创建一个 [MediaFetchRequest].
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
 */
fun MediaFetchRequest.Companion.createFlow(
    info: Flow<Pair<SubjectInfo, EpisodeInfo>>,
): Flow<MediaFetchRequest> = info.map { (subject, episode) ->
    MediaFetchRequest.create(subject = subject, episode = episode)
}

/**
 * 从多个 [MediaSource] 并行获取 [Media] 的活跃的惰性会话.
 *
 * 只有在 [MediaSourceFetchResult.results] 有 collector 时, 才会开始查询. 当一段时间没有 collector 后, 查询自动停止
 *
 * 在查询完成 [hasCompleted] 后, 该会话自动关闭.
 *
 * 可通过 [MediaFetcher] 创建.
 */
interface MediaFetchSession {
    /**
     * The request used to initiate this session.
     */
    val request: Flow<MediaFetchRequest>

    /**
     * 从各个
     */
    val resultsPerSource: Map<String, MediaSourceFetchResult> // dev notes: see implementation of [MediaSource]s for the IDs.

    /**
     * 从所有数据源聚合的结果. collect [cumulativeResults] 会导致所有数据源开始查询. 持续 collect 以保持查询不被中断.
     * 停止 collect [cumulativeResults] 几秒后, 查询将被中断.
     *
     * ## 各数据源自身结果拥有缓存
     *
     * 每个数据源自己的[结果][MediaSourceFetchResult.results]是共享且有记忆的. 当它查询成功后, 就不会被因为 collect [cumulativeResults] 而重新查询.
     * 但仍然可以通过 [MediaSourceFetchResult.restart] 来手动重新查询.
     *
     * 重新 collect [cumulativeResults], 已经完成的数据源不会重新查询.
     *
     * ### [cumulativeResults] 没有缓存
     *
     * [cumulativeResults] 不是 [SharedFlow] 每个 collector 都会独立计算.
     * 每次 collect 都会从当前瞬时的结果开始, flow 一定会 emit 一个当前的结果.
     *
     * ## 获取当前瞬时查询结果
     *
     * ```
     * cumulativeResults.first()
     * ```
     *
     * ## 获取全部结果
     *
     * 因为数据源查询可以重试, 该 flow 永远不会完结.
     *
     * 当 [hasCompleted] emit `true` 后, [cumulativeResults] 一定会 emit 所有的查询结果.
     * 因此, 如需获取所有结果, 可以先使用 [awaitCompletion] 等待查询完成, 再 collect [cumulativeResults] 的 [Flow.first].
     * 也可以便捷地使用 [awaitCompletedResults].
     *
     * ## Sanitization
     *
     * The results are post-processed to eliminate duplicated entries from different sources.
     * Hence [cumulativeResults] might emit less values than a merge of all values from [MediaSourceFetchResult.results].
     */
    val cumulativeResults: Flow<List<Media>>

    /**
     * 所有数据源是否都已经完成, 无论是成功还是失败.
     *
     * 注意, collect [hasCompleted], 不会导致 [cumulativeResults] 开始 collect.
     * 也就是说, 必须要先开始 collect [cumulativeResults], [hasCompleted] 才有可能变为 `true`.
     *
     * 注意, 即使 [hasCompleted] 现在为 `true`, 它也可能在未来因为数据源重试, 或者 [request] 变更而变为 `false`.
     * 因此该 flow 永远不会完结.
     */
    val hasCompleted: Flow<Boolean>
}

/**
 * 挂起当前协程, 直到所有 [MediaSource] 都查询完成.
 *
 * 支持 cancellation.
 */
suspend fun MediaFetchSession.awaitCompletion() {
    cancellableCoroutineScope {
        cumulativeResults.shareIn(this, started = SharingStarted.Eagerly, replay = 1)
        hasCompleted.first { it }
        cancelScope()
    }
}

/**
 * 挂起当前协程, 直到所有 [MediaSource] 都查询完成, 然后获取所有查询结果.
 *
 * 支持 cancellation.
 */
suspend inline fun MediaFetchSession.awaitCompletedResults(): List<Media> {
    awaitCompletion()
    return cumulativeResults.first()
}

/**
 * 表示一个数据源 [MediaSource] 的查询结果
 */
interface MediaSourceFetchResult {
    val mediaSourceId: String
    val kind: MediaSourceKind

    val state: StateFlow<MediaSourceFetchState>

    /**
     * 从该数据源查询到的结果.
     *
     * ## 初始值为 [emptyList]
     *
     * 该 flow 一定至少有一个元素, [emptyList]. 第一次调用 [Flow.first] 一定返回 [emptyList].
     *
     * ## 查询是惰性的
     *
     * 只有在 [results] 有 collector 时, 才会开始查询. 当一段时间没有 collector 后, 查询自动停止.
     *
     * 查询结果会 share 在指定的 context.
     */
    val results: SharedFlow<List<Media>>

    /**
     * 仅当启用时才获取结果. 返回的 flow 一定至少有一个元素, 例如 [emptyList].
     */
    val resultsIfEnabled
        get() = state
            .map { it !is MediaSourceFetchState.Disabled }
            .distinctUntilChanged()
            .flatMapLatest {
                if (it) results else flowOf(emptyList())
            }

    /**
     * 重新请求获取结果.
     *
     * 即使状态是 [MediaSourceFetchState.Disabled], 也会重新请求.
     */
    fun restart()
}

class MediaFetcherConfig {
    companion object {
        val Default = MediaFetcherConfig()
    }
}

/**
 * 一个 [MediaFetcher] 的实现, 从多个 [MediaSource] 并行[查询][MediaSource.fetch].
 *
 * @param configProvider configures each [MediaFetchSession] from [MediaFetcher.newSession].
 * The provider is evaluated for each fetch so that it can be dynamic.
 */
class MediaSourceMediaFetcher(
    private val configProvider: () -> MediaFetcherConfig,
    private val mediaSources: List<MediaSourceInstance>,
) : MediaFetcher {
    private inner class MediaSourceResultImpl(
        override val mediaSourceId: String,
        override val kind: MediaSourceKind,
        private val config: MediaFetcherConfig,
        val disabled: Boolean,
        pagedSources: Flow<SizedSource<MediaMatch>>,
        flowContext: CoroutineContext,
    ) : MediaSourceFetchResult {
        override val state: MutableStateFlow<MediaSourceFetchState> =
            MutableStateFlow(if (disabled) MediaSourceFetchState.Disabled else MediaSourceFetchState.Idle)
        private val restartCount = MutableStateFlow(0)

        override val results: SharedFlow<List<Media>> by lazy {
            restartCount.flatMapLatest {
                pagedSources
                    .onStart {
                        state.value = MediaSourceFetchState.Working
                    }
                    .flatMapMerge { sources ->
                        sources.results.map { it.media }
                    }
                    .catch {
                        state.value = MediaSourceFetchState.Failed(it)
                        logger.error(it) { "Failed to fetch media from $mediaSourceId because of upstream error" }
//                        throw it
                    }
                    .onCompletion {
                        if (it == null) {
                            // catch might have already updated the state
                            if (state.value !is MediaSourceFetchState.Completed) {
                                state.value = MediaSourceFetchState.Succeed
                            }
                        } else {
                            val currentState = state.value
                            if (currentState !is MediaSourceFetchState.Failed) {
                                // downstream (collector) failure
                                state.value = MediaSourceFetchState.Abandoned(it)
                                logger.error(it) { "Failed to fetch media from $mediaSourceId because of downstream error" }
//                                throw it
                            }
                            // upstream failure re-caught here
//                            throw it
                        }
                    }
                    .runningFold(emptyList<Media>()) { acc, list ->
                        acc + list
                    }
                    .map { list ->
                        list.distinctBy { it.mediaId }
                    }
            }.shareIn(
                CoroutineScope(flowContext), replay = 1, started = SharingStarted.WhileSubscribed(5000)
            )
        }

        override fun restart() {
            while (true) {
                val value = state.value
                if (value is MediaSourceFetchState.Completed || value is MediaSourceFetchState.Disabled) {
                    if (state.compareAndSet(value, MediaSourceFetchState.Idle)) {
                        break
                    }
                } else {
                    break
                }
            }
            restartCount.value += 1
        }
    }

    private inner class MediaFetchSessionImpl(
        request: Flow<MediaFetchRequest>, // must be shared
        private val config: MediaFetcherConfig,
        private val flowContext: CoroutineContext,
    ) : MediaFetchSession {
        override val request: Flow<MediaFetchRequest> =
            request.shareIn(CoroutineScope(flowContext), started = SharingStarted.WhileSubscribed(), replay = 1)

        override val resultsPerSource: Map<String, MediaSourceFetchResult> = mediaSources.associateBy {
            it.mediaSourceId
        }.mapValues { (id, instance) ->
            MediaSourceResultImpl(
                mediaSourceId = id,
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
            combine(resultsPerSource.values.map { it.resultsIfEnabled }) { lists ->
                lists.asSequence().flatten().toList()
            }.map { list ->
                list.distinctBy { it.mediaId } // distinct globally by id, just to be safe
            }.flowOn(flowContext)

        override val hasCompleted = if (resultsPerSource.isEmpty()) {
            flowOf(true)
        } else {
            combine(resultsPerSource.values.map { it.state }) { states ->
                states.all { it is MediaSourceFetchState.Completed || it is MediaSourceFetchState.Disabled }
            }.flowOn(flowContext)
        }
    }

    override fun newSession(requestFlow: Flow<MediaFetchRequest>, flowContext: CoroutineContext): MediaFetchSession {
        return MediaFetchSessionImpl(requestFlow, configProvider(), flowContext)
    }

    private companion object {
        private val logger = logger<MediaSourceMediaFetcher>()
    }
}
