package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.media.MediaSourceManager
import me.him188.ani.app.data.repositories.EpisodePreferencesRepository
import me.him188.ani.app.data.repositories.EpisodeRepository
import me.him188.ani.app.data.repositories.SubjectRepository
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
import me.him188.ani.datasources.core.fetch.MediaFetchSession
import me.him188.ani.datasources.core.fetch.MediaFetcher
import me.him188.ani.datasources.core.fetch.MediaFetcherConfig
import me.him188.ani.datasources.core.fetch.MediaSourceMediaFetcher
import me.him188.ani.utils.coroutines.runUntilSuccess
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext
import kotlin.coroutines.CoroutineContext

/**
 * 一个正在进行中的对剧集资源的获取操作.
 */
@Stable
interface EpisodeMediaFetchSession {
    /**
     * A lazy [MediaFetchSession] that is created on demand and then shared.
     */
    val mediaFetchSession: SharedFlow<MediaFetchSession>

    /**
     * Range is `0f..1f`. `null` means progress is not yet known.
     *
     * It is guaranteed to emit `1f` when the fetcher has succeed.
     */
    val mediaFetcherProgress: Float?

    /**
     * Whether the [mediaFetchSession] has succeed. It is always `false` when initialized.
     */
    val mediaFetcherCompleted: Boolean

    /**
     * A [MediaSelectorState] associated with the fetched medias.
     */
    val mediaSelectorState: MediaSelectorState
}

suspend fun EpisodeMediaFetchSession.awaitCompletion() {
    mediaFetchSession.flatMapLatest { it.hasCompleted }.filter { it }.first()
}

@Immutable
class FetcherMediaSelectorConfig(
    /**
     * When user clicks the filter chip.
     */
    val savePreferencesOnFilter: Boolean = true,
    /**
     * When user clicks a media.
     */
    val savePreferencesOnSelect: Boolean = true,
    /**
     * Automatically select a media when the [MediaFetchSession] has completed.
     */
    val autoSelectOnFetchCompletion: Boolean = true,
    /**
     * Automatically select the local (cached) media when there is at least one such media.
     */
    val autoSelectLocal: Boolean = true,
) {
    companion object {
        val Default = FetcherMediaSelectorConfig()
        val NoAutoSelect = FetcherMediaSelectorConfig(
            autoSelectOnFetchCompletion = false,
            autoSelectLocal = false,
        )
        val NoSave = FetcherMediaSelectorConfig(
            savePreferencesOnFilter = false,
            savePreferencesOnSelect = false
        )
    }
}

/**
 * Creates a [EpisodeMediaFetchSession] that fetches media for the given [subjectId] and [episodeId],
 * and then maintains a [MediaSelectorState] for user selection.
 *
 * @param parentCoroutineContext must have a [Job] to manage the lifecycle of this fetcher.
 */
fun EpisodeMediaFetchSession(
    subjectId: Int,
    episodeId: Int,
    parentCoroutineContext: CoroutineContext,
    config: FetcherMediaSelectorConfig = FetcherMediaSelectorConfig.Default,
    koin: () -> Koin = { GlobalContext.get() },
): EpisodeMediaFetchSession = DefaultEpisodeMediaFetchSession(
    subjectId,
    episodeId,
    parentCoroutineContext,
    config,
    koin,
)


/**
 * [MediaFetcher]-based media selector.
 *
 * This class fetches media from the [MediaFetcher] and maintains a [MediaSelectorState] for user selection.
 * It also considers the user's per-subject preferences with [EpisodePreferencesRepository].
 */
internal class DefaultEpisodeMediaFetchSession(
    subjectId: Int,
    episodeId: Int,
    parentCoroutineContext: CoroutineContext,
    private val config: FetcherMediaSelectorConfig = FetcherMediaSelectorConfig.Default,
    private val koin: () -> Koin = { GlobalContext.get() },
) : EpisodeMediaFetchSession, HasBackgroundScope by BackgroundScope(parentCoroutineContext), KoinComponent {
    override fun getKoin(): Koin = koin()

    private val subjectRepository: SubjectRepository by inject()
    private val episodeRepository: EpisodeRepository by inject()
    private val mediaSourceManager: MediaSourceManager by inject()
    private val episodePreferencesRepository: EpisodePreferencesRepository by inject()

    private val subject = flowOf(subjectId).mapLatest {
        runUntilSuccess { subjectRepository.getSubject(subjectId)!! }
    }
    private val episode = flowOf(episodeId).mapLatest {
        runUntilSuccess { episodeRepository.getEpisodeById(episodeId)!! }
    }

    private val mediaFetcher = mediaSourceManager.sources.map { providers ->
        MediaSourceMediaFetcher(
            configProvider = { MediaFetcherConfig.Default },
            mediaSources = providers,
            parentCoroutineContext = backgroundScope.coroutineContext,
        )
    }.shareInBackground(started = SharingStarted.Lazily)

    private val mediaFetchRequest = combine(subject, episode) { subject, episode ->
        MediaFetchRequest(
            subjectId = subject.id.toString(),
            episodeId = episode.id.toString(),
            subjectNames = setOfNotNull(
                subject.nameCn,
                subject.name,
            ),
            episodeSort = EpisodeSort(episode.sort.toString()),
            episodeName = episode.nameCNOrName(),
        )
    }

    /**
     * A lazy [MediaFetchSession] that is created when [subject] and [episode] are available.
     */
    override val mediaFetchSession = combine(mediaFetchRequest, mediaFetcher) { req, fetcher ->
        fetcher.fetch(req)
    }.shareInBackground(started = SharingStarted.Lazily)

    override val mediaFetcherProgress by mediaFetchSession
        .flatMapLatest { it.progress }
        .sample(100)
        .onCompletion { if (it == null) emit(1f) }
        .produceState(null)

    override val mediaFetcherCompleted by mediaFetchSession.flatMapLatest { it.hasCompleted }
        .produceState(false)

    override val mediaSelectorState = kotlin.run {
        MediaSelectorState(
            mediaFetchSession,
            mediaPreferenceFlow = episodePreferencesRepository.mediaPreferenceFlow(subjectId),
            backgroundScope,
        ).apply {
            if (config.savePreferencesOnFilter) {
                launchInBackground {
                    // Save users' per-subject preferences when they click the filter chips
                    preferenceUpdates.preference.collect {
                        val defaultPreference = withContext(Dispatchers.Main.immediate) {
                            default
                        }
                        episodePreferencesRepository.setMediaPreference(
                            subjectId,
                            defaultPreference.merge(it)
                        )
                    }
                }
            }
            if (config.savePreferencesOnSelect) {
                launchInBackground {
                    // Save users' per-subject preferences when they click cards
                    preferenceUpdates.select.collect { media ->
                        episodePreferencesRepository.setMediaPreference(
                            subjectId,
                            MediaPreference(
                                alliance = media.properties.alliance,
                                resolution = media.properties.resolution,
                                // Use the filter chip if any
                                // because a media has multiple languages and user may choose the media because it includes their desired one
                                subtitleLanguageId = selectedSubtitleLanguageId
                                    ?: media.properties.subtitleLanguageIds.firstOrNull(),
                                mediaSourceId = media.mediaSourceId
                            )
                        )
                    }
                }
            }
            if (config.autoSelectOnFetchCompletion) {
                launchInBackground {
                    // Automatically select a media when the list is ready
                    mediaFetchSession.flatMapLatest { it.hasCompleted }.filter { it }.collect {
                        // on completion
                        withContext(Dispatchers.Main.immediate) {
                            if (selected == null) { // only if user has not selected
                                makeDefaultSelection()
                            }
                        }
                    }
                }
            }
            if (config.autoSelectLocal) {
                launchInBackground {
                    mediaFetchSession.flatMapLatest { it.cumulativeResults }.collect { list ->
                        if (list.any { it.location == MediaSourceLocation.LOCAL }) {
                            withContext(Dispatchers.Main.immediate) {
                                if (selected == null) { // only if user has not selected
                                    makeDefaultSelection()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
