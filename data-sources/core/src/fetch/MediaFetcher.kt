package me.him188.ani.datasources.core.fetch

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
import me.him188.ani.datasources.api.paging.filter
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.topic.contains
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A fetcher that supports concurrent fetching of [Media]s from multiple [MediaSource]s.
 *
 * @see MediaSourceMediaFetcher
 */
interface MediaFetcher {
    /**
     * Starts a concurrent fetch from all [MediaSource]s this [MediaFetcher] has.
     */
    fun fetch(request: MediaFetchRequest): MediaFetchSession
}

/**
 * A session describing the ongoing process of a fetch initiated from [MediaFetcher.fetch].
 *
 * The session is cold:
 * Only when the flows are being collected, its belonging [MediaSourceResult]s will make network requests and emitting results
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

@Stable
interface MediaSourceResult {
    val mediaSourceId: String
    val kind: MediaSourceKind

    val state: StateFlow<MediaSourceState>

    /**
     * Result from this data source.
     *
     * The flow is not shared. If there are multiple collectors, each collector will start a **new** fetch.
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
     * 仅当启用时才获取结果.
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

@Immutable
class Progress(
    val current: Int,
    val total: Int?,
)

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
    private val mediaSources: List<MediaSource>,
    private val sourceEnabled: (MediaSource) -> Boolean,
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
                        throw it
                    }
                    .onCompletion {
                        if (it == null) {
                            state.value = MediaSourceState.Succeed
                        } else {
                            val currentState = state.value
                            if (currentState is MediaSourceState.Failed) {
                                // upstream failure re-caught here
                                throw it
                            }
                            // downstream (collector) failure
                            state.value = MediaSourceState.Abandoned(it)
                            logger.error(it) { "Failed to fetch media from $mediaSourceId because of downstream error" }
                            throw it
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
            restartCount.value++
        }
    }

    private inner class MediaFetchSessionImpl(
        override val request: MediaFetchRequest,
        private val config: MediaFetcherConfig,
    ) : MediaFetchSession {
        override val resultsPerSource: Map<String, MediaSourceResult> = mediaSources.associateBy {
            it.mediaSourceId
        }.mapValues { (id, source) ->
            MediaSourceResultImpl(
                mediaSourceId = id,
                source.kind,
                config,
                disabled = !sourceEnabled(source),
                pagedSources = flowOf(request)
                    .map {
                        source.fetch(it).filter { media ->
                            media.matches(request)
                        }
                    }// so that the flow can normally complete
            )
        }

        override val cumulativeResults: Flow<List<Media>> =
            combine(resultsPerSource.values.map { it.resultsIfEnabled }) { lists ->
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
            states.all { it is MediaSourceState.Completed }
        }
    }

    override fun fetch(request: MediaFetchRequest): MediaFetchSession {
        return MediaFetchSessionImpl(request, configProvider())
    }

    private companion object {
        val logger = logger<MediaSourceMediaFetcher>()
    }
}

private fun MediaMatch.matches(request: MediaFetchRequest): Boolean {
    if (this.kind == MatchKind.NONE) return false
    val actualEpRange = this.media.episodeRange ?: return false
    val expectedEp = request.episodeEp
    return !(request.episodeSort !in actualEpRange && (expectedEp == null || expectedEp !in actualEpRange))
}