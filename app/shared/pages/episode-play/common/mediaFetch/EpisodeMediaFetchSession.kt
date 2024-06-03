package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.util.fastAll
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import me.him188.ani.app.data.media.MediaSourceManager
import me.him188.ani.app.data.media.selector.DefaultMediaSelector
import me.him188.ani.app.data.media.selector.MediaSelector
import me.him188.ani.app.data.media.selector.MediaSelectorContext
import me.him188.ani.app.data.repositories.EpisodePreferencesRepository
import me.him188.ani.app.data.repositories.EpisodeRepository
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.data.repositories.SubjectRepository
import me.him188.ani.app.data.subject.PackedDate
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.data.subject.minus
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.bangumi.processing.isOnAir
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
import kotlin.time.Duration.Companion.days

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
     * 数据源选择器
     */
    val mediaSelector: MediaSelector

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
     * 保存本次会话用户更新的资源选择偏好设置
     */
    val savePreferenceChanges: Boolean = true,
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
            savePreferenceChanges = false
        )
    }
}

/**
 * Creates a [EpisodeMediaFetchSession] that fetches media for the given [subjectId] and [episodeId],
 * and then maintains a [MediaSelectorPresentation] for user selection.
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
 * This class fetches media from the [MediaFetcher] and maintains a [MediaSelectorPresentation] for user selection.
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
    private val subjectManager: SubjectManager by inject()
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

    override val mediaSelector = kotlin.run {
        val fetchResult = mediaFetchSession.flatMapLatest { it.cumulativeResults }
            .mapLatest { list ->
                list.sortedWith(
                    compareBy<Media> { it.costForDownload }.thenByDescending { it.properties.size.inBytes }
                )
            }

        val subjectProgress = flow {
            emit(subjectManager.getEpisodeCollections(subjectId).map { it.episode })
        }

        DefaultMediaSelector(
            mediaSelectorContextNotCached = subjectProgress.map { eps ->
                val allEpisodesFinished = eps.fastAll { it.isOnAir() == false }

                val finishedLongTimeAgo = allEpisodesFinished || run {
                    val now = PackedDate.now()
                    val maxAirDate = eps
                        .map {
                            PackedDate.parseFromDate(it.airdate)
                        }
                        .filter { it.isValid }
                        .maxOrNull()

                    maxAirDate != null && now - maxAirDate >= 14.days
                }

                MediaSelectorContext(
                    subjectFinishedForAConservativeTime = finishedLongTimeAgo,
                )
            },
            mediaListNotCached = fetchResult,
            savedUserPreference = episodePreferencesRepository.mediaPreferenceFlow(subjectId),
            savedDefaultPreference = settingsRepository.defaultMediaPreference.flow,
            mediaSelectorSettings = settingsRepository.mediaSelectorSettings.flow,
        )
    }

    init {
        if (config.savePreferenceChanges) {
            launchInBackground {
                mediaSelector.events.onChangePreference.collect {
                    episodePreferencesRepository.setMediaPreference(subjectId, it)
                }
            }
        }
        if (config.autoSelectOnFetchCompletion) {
            launchInBackground {
                // Automatically select a media when the list is ready
                val owner = Any()
                try {
                    // 等全部加载完成
                    mediaFetchSession.flatMapLatest { it.hasCompleted }.filter { it }.first()
                    if (mediaSelector.selected.first() == null) {
                        mediaSelector.trySelectDefault()
                    }
                } catch (e: OwnedCancellationException) {
                    e.checkOwner(owner)
                }
            }
        }
        if (config.autoSelectLocal) {
            launchInBackground {
                val owner = Any()
                try {
                    combine(
                        mediaFetchSession.flatMapLatest { it.cumulativeResults },
                    ) { _ ->
                        if (mediaSelector.selected.first() != null ||
                            mediaSelector.trySelectCached() != null
                        ) {
                            throw OwnedCancellationException(owner) // 设置成功一次, 退出
                        }
                    }.collect()
                } catch (e: OwnedCancellationException) {
                    e.checkOwner(owner)
                }
            }
        }
    }

    override val sourceResults: List<MediaSourceResult> by mediaFetchSession.map {
        it.resultsPerSource.values.toList()
    }.produceState(emptyList())
}
