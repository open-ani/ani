package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import me.him188.ani.app.data.media.MediaSourceManager
import me.him188.ani.app.data.repositories.EpisodePreferencesRepository
import me.him188.ani.app.data.repositories.EpisodeRepository
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.data.repositories.SubjectRepository
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInMain
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
import me.him188.ani.datasources.core.fetch.MediaFetchSession
import me.him188.ani.datasources.core.fetch.MediaFetcher
import me.him188.ani.datasources.core.fetch.MediaFetcherConfig
import me.him188.ani.datasources.core.fetch.MediaSourceMediaFetcher
import me.him188.ani.datasources.core.fetch.MediaSourceResult
import me.him188.ani.utils.coroutines.OwnedCancellationException
import me.him188.ani.utils.coroutines.checkOwner
import me.him188.ani.utils.coroutines.runUntilSuccess
import me.him188.ani.utils.logging.logger
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
     * Whether the [mediaFetchSession] has succeed. It is always `false` when initialized.
     */
    val mediaFetcherCompleted: Boolean

    /**
     * A [MediaSelectorState] associated with the fetched medias.
     */
    val mediaSelectorState: MediaSelectorState

    /**
     * 每个数据源的结果
     */
    val sourceResults: List<MediaSourceResult>
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
    private companion object {
        private val logger = logger(DefaultEpisodeMediaFetchSession::class)
    }

    override fun getKoin(): Koin = koin()

    private val subjectRepository: SubjectRepository by inject()
    private val episodeRepository: EpisodeRepository by inject()
    private val mediaSourceManager: MediaSourceManager by inject()
    private val episodePreferencesRepository: EpisodePreferencesRepository by inject()
    private val settingsRepository: SettingsRepository by inject()

    private val subject = flowOf(subjectId).mapLatest {
        runUntilSuccess { subjectRepository.getSubject(subjectId)!! }
    }
    private val episode = flowOf(episodeId).mapLatest {
        runUntilSuccess { episodeRepository.getEpisodeById(episodeId)!! }
    }

    private val mediaFetcher = mediaSourceManager.allSources.map { providers ->
        val defaultPreference = settingsRepository.defaultMediaPreference.flow.first()
        MediaSourceMediaFetcher(
            configProvider = { MediaFetcherConfig.Default },
            mediaSources = providers,
            sourceEnabled = { defaultPreference.isSourceEnabled(it.mediaSourceId) },
            parentCoroutineContext = backgroundScope.coroutineContext,
        )
    }.shareInBackground(started = SharingStarted.Lazily)

    private val mediaFetchRequest = combine(subject, episode) { subject, episode ->
        MediaFetchRequest(
            subjectId = subject.id.toString(),
            episodeId = episode.id.toString(),
            subjectNameCN = subject.nameCNOrName(),
            subjectNames = setOfNotNull(
                subject.nameCn.takeIf { it.isNotBlank() },
                subject.name.takeIf { it.isNotBlank() },
            ),
            episodeSort = EpisodeSort(episode.sort.toString()),
            episodeName = episode.nameCNOrName(),
            episodeEp = episode.ep?.let { EpisodeSort(it) },
        )
    }

    /**
     * A lazy [MediaFetchSession] that is created when [subject] and [episode] are available.
     */
    override val mediaFetchSession = combine(mediaFetchRequest, mediaFetcher) { req, fetcher ->
        fetcher.fetch(req)
    }.shareInBackground(started = SharingStarted.Lazily)

    override val mediaFetcherCompleted by mediaFetchSession.flatMapLatest { it.hasCompleted }
        .produceState(false)

    override val mediaSelectorState = kotlin.run {
        val fetchResult by mediaFetchSession.flatMapLatest { it.cumulativeResults }
            .mapLatest { list ->
                list.sortedWith(
                    compareBy<Media> { it.costForDownload }.thenByDescending { it.properties.size.inBytes }
                )
            }
            .produceState(emptyList())

        val placeholderDefaultPreference = MediaPreference.Empty.copy() // don't remove .copy, we need identity
        val defaultPreferencesFlow = episodePreferencesRepository.mediaPreferenceFlow(subjectId)
            .stateInBackground(placeholderDefaultPreference, started = SharingStarted.Eagerly)
        val defaultPreferencesFetched = defaultPreferencesFlow.map {
            it !== placeholderDefaultPreference
        }
        var defaultPreference by mutableStateOf(placeholderDefaultPreference)
        launchInMain {
            defaultPreference = withContext(Dispatchers.Default) { defaultPreferencesFlow.first() }
        } // 不要用 produceState, 会造成递归 (用户点击过滤 -> 保存默认 -> 默认更新 -> 筛选更新)

        MediaSelectorState(
            { fetchResult },
            { defaultPreference },
        ).apply {
            if (config.savePreferencesOnFilter) {
                launchInMain {
                    // Save users' per-subject preferences when they click the filter chips
                    preferenceUpdates.preference.collect {
                        yield()
                        episodePreferencesRepository.setMediaPreference(
                            subjectId,
                            defaultPreference.merge(it)
                        )
                    }
                }
            }
            if (config.savePreferencesOnSelect) {
                launchInMain {
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
                launchInMain {
                    // Automatically select a media when the list is ready
                    combine(
                        mediaFetchSession.flatMapLatest { it.hasCompleted }.filter { it },
                        defaultPreferencesFetched
                    ) { _, defaultPreferencesFetched ->
                        if (!defaultPreferencesFetched) return@combine // wait for config load

                        // on completion
                        if (selected == null) { // only if user has not selected
                            makeDefaultSelection()
                        }
                    }.collect()
                }
            }
            if (config.autoSelectLocal) {
                launchInMain {
                    val owner = Any()
                    try {
                        combine(
                            mediaFetchSession.flatMapLatest { it.cumulativeResults },
                            defaultPreferencesFetched
                        ) { _, defaultPreferencesFetched ->
                            if (!defaultPreferencesFetched) return@combine // wait for config load
                            if (trySelectCachedByDefault()) {
                                throw OwnedCancellationException(owner)
                            }
                        }.collect()
                    } catch (e: OwnedCancellationException) {
                        e.checkOwner(owner)
                    }
                }
            }
        }
    }

    override val sourceResults: List<MediaSourceResult> by mediaFetchSession.map {
        it.resultsPerSource.values.toList()
    }.produceState(emptyList())
}
