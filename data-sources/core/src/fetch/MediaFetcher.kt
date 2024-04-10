package me.him188.ani.datasources.core.fetch

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.take
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSource
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
     * Results from each source. The key is the source ID.
     */
    val resultsPerSource: Map<String, MediaSourceResult> // dev notes: see implementation of [DownloadProvider]s for the IDs.

    /**
     * Cumulative results from all sources.
     *
     * The flow is not shared. If there are multiple collectors, each collector will start a **new** fetch.
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

    /**
     * Overall progress of all the sources.
     *
     * If one of the source cannot be determined the total size, the overall progress will skip that source.
     * Hence when [progress] emits `1f`, it does not necessarily mean all sources have completed.
     */
    val progress: Flow<Float>
}

@Stable
interface MediaSourceResult {
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

    val progress: Flow<Progress>
}

class Progress(
    val current: Int,
    val total: Int?,
)

sealed class MediaSourceState {
    data object Idle : MediaSourceState()
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
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : MediaFetcher {
    private val scope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))

    private inner class MediaSourceResultImpl(
        private val dataSourceId: String,
        private val config: MediaFetcherConfig,
        pagedSources: Flow<SizedSource<MediaMatch>>,
    ) : MediaSourceResult {
        override val state: MutableStateFlow<MediaSourceState> = MutableStateFlow(MediaSourceState.Idle)

        override val results: Flow<List<Media>> by lazy {
            pagedSources
                .flatMapMerge { sources ->
                    sources.results.map { it.media }
                }
                .onStart {
                    state.value = MediaSourceState.Working
                }
                .retry(2) { delay(2000);true }
                .catch {
                    state.value = MediaSourceState.Failed(it)
                    logger.error(it) { "Failed to fetch media from $dataSourceId because of upstream error" }
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
                        logger.error(it) { "Failed to fetch media from $dataSourceId because of downstream error" }
                        throw it
                    }
                }
                .runningFold(emptyList<Media>()) { acc, list ->
                    acc + list
                }
                .map { list ->
                    list.distinctBy { it.mediaId }
                }
                .shareIn(
                    scope, replay = 1, started = SharingStarted.WhileSubscribed(5000)
                )
        }

        override val progress: Flow<Progress> by lazy {
            combine(results, pagedSources.flatMapLatest { it.totalSize }) { res, total ->
                Progress(
                    current = res.size,
                    total = total
                )
            }
        }
    }

    private inner class MediaFetchSessionImpl(
        request: MediaFetchRequest,
        private val config: MediaFetcherConfig,
    ) : MediaFetchSession {
        override val resultsPerSource: Map<String, MediaSourceResult> = mediaSources.associateBy {
            it.mediaSourceId
        }.mapValues { (id, provider) ->
            MediaSourceResultImpl(
                dataSourceId = id,
                config,
                pagedSources = flowOf(request)
                    .map {
                        provider.fetch(it)
                    }.shareIn(
                        scope, replay = 1, started = SharingStarted.Lazily,
                    ).take(1) // so that the flow can normally complete
            )
        }

        override val cumulativeResults: Flow<List<Media>> =
            combine(resultsPerSource.values.map { it.results }) { lists ->
                // Merge into one single list
                lists.fold(mutableListOf<Media>()) { acc, list ->
                    acc.addAll(list.distinctBy { it.originalTitle }) // distinct within this source's scope
                    acc
                }
            }.map { list ->
                list.distinctBy { it.mediaId } // distinct globally by id, just to be safe
            }

        override val hasCompleted = combine(resultsPerSource.values.map { it.state }) { states ->
            states.all { it is MediaSourceState.Completed }
        }

        override val progress: Flow<Float> = combine(resultsPerSource.values.map { it.progress }) { progresses ->
            var total = 0
            var current = 0
            if (progresses.isEmpty()) {
                return@combine 1f
            }
            if (progresses.all { it.total == null }) {
                return@combine 1f
            }
            for (progress in progresses) {
                if (progress.total == null) {
                    continue
                } else {
                    total += progress.total
                    current += progress.current
                }
            }
            if (total == 0) {
                0f
            } else {
                current.toFloat() / total
            }
        }.combine(hasCompleted) { progress, hasCompleted ->
            if (hasCompleted) 1f else progress
        }
    }

    override fun fetch(request: MediaFetchRequest): MediaFetchSession {
        return MediaFetchSessionImpl(request, configProvider())
    }

    private companion object {
        val logger = logger<MediaSourceMediaFetcher>()
    }
}
