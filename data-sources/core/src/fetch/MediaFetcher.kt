package me.him188.ani.datasources.core.fetch

import androidx.compose.runtime.Stable
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.toStringMultiline
import me.him188.ani.datasources.core.instance.MediaSourceInstance
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
interface MediaFetcher : AutoCloseable {
    /**
     * 创建一个会话, 从多个 [MediaSource] 并行获取 [Media].
     *
     * 该会话的生命与 [MediaFetcher] 绑定. 一旦 [MediaFetcher] 被释放, 会话也会被释放.
     */
    fun fetch(request: MediaFetchRequest): MediaFetchSession
}

/**
 * 从多个 [MediaSource] 并行获取 [Media] 的活跃的会话.
 */
@Stable
interface MediaFetchSession {
    /**
     * The request used to initiate this session.
     */
    val request: MediaFetchRequest

    /**
     * Results from each source. The key is the source ID.
     */
    val resultsPerSource: Map<String, MediaSourceResult> // dev notes: see implementation of [MediaSource]s for the IDs.

    /**
     * Cumulative results from all sources.
     *
     * ### Sanitization
     *
     * The results are post-processed to eliminate duplicated entries from different sources.
     * Hence [cumulativeResults] might emit less values than a merge of all values from [MediaSourceResult.results].
     */
    val cumulativeResults: Flow<List<Media>>

    /**
     * Whether all sources have completed fetching.
     *
     * Note that this is lazily updated by [resultsPerSource].
     * If [resultsPerSource] is not collected, this will always be false.
     */
    val hasCompleted: Flow<Boolean>
}

/**
 * 表示一个数据源 [MediaSource] 的查询结果
 */
@Stable
interface MediaSourceResult {
    val mediaSourceId: String
    val kind: MediaSourceKind

    val state: StateFlow<MediaSourceState>

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
            .map { it !is MediaSourceState.Disabled }
            .distinctUntilChanged()
            .flatMapLatest {
                if (it) results else flowOf(emptyList())
            }

    /**
     * 重新请求获取结果.
     *
     * 即使状态是 [MediaSourceState.Disabled], 也会重新请求.
     */
    fun restart()
}


@Stable
sealed class MediaSourceState {
    data object Idle : MediaSourceState()

    /**
     * 被禁用, 因此不会主动发起请求. 仍然可以通过 [MediaSourceResult.restart] 发起请求.
     */
    data object Disabled : MediaSourceState()

    data object Working : MediaSourceState()


    sealed class Completed : MediaSourceState()
    data object Succeed : Completed()

    /**
     * The data source upstream has failed. E.g. a network request failed.
     */
    data class Failed(
        val cause: Throwable,
    ) : Completed()

    /**
     * Failed because the flow collector has thrown an exception (and stopped collection)
     */
    data class Abandoned(
        val cause: Throwable,
    ) : Completed()
}

class MediaFetcherConfig {
    companion object {
        val Default = MediaFetcherConfig()
    }
}

/**
 * A [MediaFetcher] implementation that fetches media from various [MediaSource]s (from the data-sources module).
 *
 * @param configProvider configures each [MediaFetchSession] from [MediaFetcher.fetch].
 * The provider is evaluated for each fetch so that it can be dynamic.
 */
class MediaSourceMediaFetcher(
    private val configProvider: () -> MediaFetcherConfig,
    private val mediaSources: List<MediaSourceInstance>,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : MediaFetcher {
    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))

    private inner class MediaSourceResultImpl(
        override val mediaSourceId: String,
        override val kind: MediaSourceKind,
        private val config: MediaFetcherConfig,
        val disabled: Boolean,
        pagedSources: Flow<SizedSource<MediaMatch>>,
    ) : MediaSourceResult {
        override val state: MutableStateFlow<MediaSourceState> =
            MutableStateFlow(if (disabled) MediaSourceState.Disabled else MediaSourceState.Idle)
        private val restartCount = MutableStateFlow(0)

        override val results: Flow<List<Media>> by lazy {
            restartCount.flatMapLatest {
                pagedSources
                    .flatMapMerge { sources ->
                        sources.results.map { it.media }
                    }
                    .onStart {
                        state.value = MediaSourceState.Working
                    }
                    .catch {
                        state.value = MediaSourceState.Failed(it)
                        logger.error(it) { "Failed to fetch media from $mediaSourceId because of upstream error" }
//                        throw it
                    }
                    .onCompletion {
                        if (it == null) {
                            // catch might have already updated the state
                            if (state.value !is MediaSourceState.Completed) {
                                state.value = MediaSourceState.Succeed
                            }
                        } else {
                            val currentState = state.value
                            if (currentState !is MediaSourceState.Failed) {
                                // downstream (collector) failure
                                state.value = MediaSourceState.Abandoned(it)
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
                if (value is MediaSourceState.Completed || value is MediaSourceState.Disabled) {
                    if (state.compareAndSet(value, MediaSourceState.Idle)) {
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
        override val request: MediaFetchRequest,
        private val config: MediaFetcherConfig,
    ) : MediaFetchSession {
        init {
            logger.info { "MediaFetchSessionImpl created, request: \n${request.toStringMultiline()}" }
        }

        override val resultsPerSource: Map<String, MediaSourceResult> = mediaSources.associateBy {
            it.mediaSourceId
        }.mapValues { (id, instance) ->
            MediaSourceResultImpl(
                mediaSourceId = id,
                instance.source.kind,
                config,
                disabled = !instance.isEnabled,
                pagedSources = flowOf(request)
                    .map {
                        instance.source.fetch(it)
                    }
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
            states.all { it is MediaSourceState.Completed || it is MediaSourceState.Disabled }
        }
    }

    override fun fetch(request: MediaFetchRequest): MediaFetchSession {
        return MediaFetchSessionImpl(request, configProvider())
    }

    override fun close() {
        scope.cancel()
    }

    private companion object {
        val logger = logger<MediaSourceMediaFetcher>()
    }
}
