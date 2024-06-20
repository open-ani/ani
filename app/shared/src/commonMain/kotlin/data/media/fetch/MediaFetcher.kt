package me.him188.ani.app.data.media.fetch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
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
     */
    fun fetch(request: MediaFetchRequest): MediaFetchSession

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
    fun fetch(requestFlow: Flow<MediaFetchRequest>): MediaFetchSession
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
 * 从多个 [MediaSource] 并行获取 [Media] 的活跃的会话.
 *
 * 在查询完成 [hasCompleted] 后, 该会话自动关闭. 在这之前, 可以通过 [close] 手动关闭.
 *
 * 可通过 [MediaFetcher] 创建.
 */
interface MediaFetchSession : AutoCloseable {
    /**
     * The request used to initiate this session.
     */
    val request: Flow<MediaFetchRequest>

    /**
     * Results from each source. The key is the source ID.
     */
    val resultsPerSource: Map<String, MediaSourceFetchResult> // dev notes: see implementation of [MediaSource]s for the IDs.

    /**
     * 从所有数据源聚合的结果.
     *
     * 该流是惰性创建并在后台共享的. 第一次 collect 时, 所有数据源才会开始查询.
     *
     * ### Sanitization
     *
     * The results are post-processed to eliminate duplicated entries from different sources.
     * Hence [cumulativeResults] might emit less values than a merge of all values from [MediaSourceFetchResult.results].
     *
     * @see close
     */
    val cumulativeResults: Flow<List<Media>>

    /**
     * 所有数据源是否都已经完成, 无论是成功还是失败.
     *
     * 注意, collect 此 flow, 会导致 [cumulativeResults] 开始 collect, 也就是会启动所有数据源查询.
     *
     * 注意, 即使 [hasCompleted] 现在为 `true`, 它也可能在未来因为数据源重试, 或者 [request] 变更而变为 `false`.
     *
     * @see close
     */
    val hasCompleted: Flow<Boolean>

    /**
     * 停止所有查询. 查询将不能重启. 届时 [hasCompleted] 将变为 `true`.
     */ // TODO: 添加先 close 再 hasCompleted/cumulativeResults 的测试
    override fun close()
}

/**
 * 挂起当前协程, 直到所有 [MediaSource] 都查询完成.
 *
 * 支持 cancellation.
 */
suspend inline fun MediaFetchSession.awaitCompletion() {
    hasCompleted.first { it }
}

/**
 * 表示一个数据源 [MediaSource] 的查询结果
 */
interface MediaSourceFetchResult {
    val mediaSourceId: String
    val kind: MediaSourceKind

    val state: StateFlow<MediaSourceFetchState>

    /**
     * Result from this data source.
     *
     * ### Results are lazy
     *
     * Requests are send only when the flow is being collected.
     *
     * The requests inherit the coroutine scope from the collector,
     * therefore, when the collector is cancelled,
     * any ongoing requests are also canceled.
     */
    val results: Flow<List<Media>>

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
 * @param configProvider configures each [MediaFetchSession] from [MediaFetcher.fetch].
 * The provider is evaluated for each fetch so that it can be dynamic.
 */
class MediaSourceMediaFetcher(
    private val configProvider: () -> MediaFetcherConfig,
    private val mediaSources: List<MediaSourceInstance>,
    private val parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : MediaFetcher {
    private inner class MediaSourceResultImpl(
        override val mediaSourceId: String,
        override val kind: MediaSourceKind,
        private val config: MediaFetcherConfig,
        val disabled: Boolean,
        pagedSources: Flow<SizedSource<MediaMatch>>,
        scope: CoroutineScope,
    ) : MediaSourceFetchResult {
        override val state: MutableStateFlow<MediaSourceFetchState> =
            MutableStateFlow(if (disabled) MediaSourceFetchState.Disabled else MediaSourceFetchState.Idle)
        private val restartCount = MutableStateFlow(0)

        override val results: Flow<List<Media>> by lazy {
            restartCount.flatMapLatest {
                pagedSources
                    .flatMapMerge { sources ->
                        sources.results.map { it.media }
                    }
                    .onStart {
                        state.value = MediaSourceFetchState.Working
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
                scope, replay = 1, started = SharingStarted.WhileSubscribed(5000)
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
    ) : MediaFetchSession {
        private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))
        override val request: Flow<MediaFetchRequest> =
            request.shareIn(scope, started = SharingStarted.WhileSubscribed(5000), replay = 1)

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
                scope = scope,
            )
        }

        override val cumulativeResults: Flow<List<Media>> =
            combine(resultsPerSource.values.map { it.resultsIfEnabled.onStart { emit(emptyList()) } }) { lists ->
                // Merge into one single list
                lists.fold(mutableListOf<Media>()) { acc, list ->
                    acc.addAll(list.distinctBy { it.originalTitle }) // distinct within this source's scope
                    acc
                }
            }.map { list ->
                list.distinctBy { it.mediaId } // distinct globally by id, just to be safe
            }.shareIn(
                scope, replay = 1, started = SharingStarted.Lazily,
            )

        override val hasCompleted = combine(resultsPerSource.values.map { it.state }) { states ->
            states.all { it is MediaSourceFetchState.Completed || it is MediaSourceFetchState.Disabled }
        }

        override fun close() {
            scope.cancel()
        }
    }

    override fun fetch(request: MediaFetchRequest): MediaFetchSession {
        return MediaFetchSessionImpl(flowOf(request), configProvider())
    }

    override fun fetch(requestFlow: Flow<MediaFetchRequest>): MediaFetchSession {
        return MediaFetchSessionImpl(requestFlow, configProvider())
    }

    private companion object {
        private val logger = logger<MediaSourceMediaFetcher>()
    }
}
