package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
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
import kotlinx.coroutines.flow.onStart
import me.him188.ani.app.data.media.MediaSourceManager
import me.him188.ani.app.data.media.selector.DefaultMediaSelector
import me.him188.ani.app.data.media.selector.MediaSelector
import me.him188.ani.app.data.media.selector.MediaSelectorContext
import me.him188.ani.app.data.models.MediaSelectorSettings
import me.him188.ani.app.data.repositories.EpisodePreferencesRepository
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.data.subject.EpisodeInfo
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.data.subject.isSubjectCompleted
import me.him188.ani.app.data.subject.nameCnOrName
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.core.fetch.MediaFetchSession
import me.him188.ani.datasources.core.fetch.MediaFetcher
import me.him188.ani.datasources.core.fetch.MediaFetcherConfig
import me.him188.ani.datasources.core.fetch.MediaSourceMediaFetcher
import me.him188.ani.datasources.core.fetch.MediaSourceResult
import me.him188.ani.datasources.core.instance.MediaSourceInstance
import me.him188.ani.utils.coroutines.OwnedCancellationException
import me.him188.ani.utils.coroutines.checkOwner
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
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
    val savePreferenceChanges: Boolean,
    /**
     * Automatically select a media when the [MediaFetchSession] has completed.
     */
    val autoSelectOnFetchCompletion: Boolean,
    /**
     * Automatically select the local (cached) media when there is at least one such media.
     */
    val autoSelectLocal: Boolean,
)

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
    config: FetcherMediaSelectorConfig,
): EpisodeMediaFetchSession {
    val koin = GlobalContext.get()

    val subjectManager: SubjectManager by koin.inject()
    val mediaSourceManager: MediaSourceManager by koin.inject()
    val episodePreferencesRepository: EpisodePreferencesRepository by koin.inject()
    val settingsRepository: SettingsRepository by koin.inject()

    return DefaultEpisodeMediaFetchSession(
        subjectId,
        config,
        subject = flowOf(subjectId).mapLatest {
            subjectManager.getSubjectInfo(subjectId) // cache-first
        },
        episode = flowOf(episodeId).mapLatest {
            subjectManager.getEpisode(episodeId) // cache-first
        },
        episodePreferencesRepository = episodePreferencesRepository,
        defaultMediaPreferenceFlow = settingsRepository.defaultMediaPreference.flow,
        mediaSelectorSettingsFlow = settingsRepository.mediaSelectorSettings.flow,
        subjectCompletedNotCached = subjectManager.isSubjectCompleted(subjectId),
        mediaSourceInstancesNotCached = mediaSourceManager.allInstances,
        parentCoroutineContext,
    )
}


/**
 * [MediaFetcher]-based media selector.
 *
 * This class fetches media from the [MediaFetcher] and maintains a [MediaSelectorPresentation] for user selection.
 * It also considers the user's per-subject preferences with [EpisodePreferencesRepository].
 */
internal class DefaultEpisodeMediaFetchSession(
    subjectId: Int,
    config: FetcherMediaSelectorConfig,
    private val subject: Flow<SubjectInfo>,
    /**
     * 需要查询的 episode
     */
    private val episode: Flow<EpisodeInfo>,
    private val episodePreferencesRepository: EpisodePreferencesRepository,
    private val defaultMediaPreferenceFlow: Flow<MediaPreference>,
    private val mediaSelectorSettingsFlow: Flow<MediaSelectorSettings>,
    /**
     * 该条目已经完结
     */
    private val subjectCompletedNotCached: Flow<Boolean>,
    mediaSourceInstancesNotCached: Flow<List<MediaSourceInstance>>,
    parentCoroutineContext: CoroutineContext,
) : EpisodeMediaFetchSession, HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    private companion object {
        val logger = logger<DefaultEpisodeMediaFetchSession>()
    }

    private val mediaFetcher = mediaSourceInstancesNotCached.map { providers ->
        MediaSourceMediaFetcher(
            configProvider = { MediaFetcherConfig.Default },
            mediaSources = providers,
            parentCoroutineContext = backgroundScope.coroutineContext,
        )
    }.shareInBackground(started = SharingStarted.Lazily)

    private val mediaFetchRequest = combine(subject, episode) { subject, episode ->
        createMediaFetchResult(subject, episode)
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

        DefaultMediaSelector(
            mediaSelectorContextNotCached = subjectCompletedNotCached.map {
                MediaSelectorContext(subjectFinishedForAConservativeTime = it)
            }.onStart {
                emit(MediaSelectorContext.Initial) // 否则如果一直没获取到剧集信息, 就无法选集, #385
            },
            mediaListNotCached = fetchResult,
            savedUserPreference = episodePreferencesRepository.mediaPreferenceFlow(subjectId),
            savedDefaultPreference = defaultMediaPreferenceFlow,
            mediaSelectorSettings = mediaSelectorSettingsFlow,
        )
    }

    override val sourceResults: List<MediaSourceResult> by mediaFetchSession.map {
        it.resultsPerSource.values.toList()
    }.produceState(emptyList())

    init {
        if (config.savePreferenceChanges) {
            launchInBackground {
                mediaSelector.events.onChangePreference.collect {
                    episodePreferencesRepository.setMediaPreference(subjectId, it)
                }
            }
        }
        if (config.autoSelectOnFetchCompletion) {
            logger.info { "autoSelectOnFetchCompletion is on" }
            launchInBackground {
                // Automatically select a media when the list is ready
                doAutoSelectOnFetchCompletion()
            }
        }
        if (config.autoSelectLocal) {
            launchInBackground {
                doAutoSelectLocal()
            }
        }
    }

    suspend fun doAutoSelectOnFetchCompletion() {
        val owner = Any()
        try {
            // 等全部加载完成
            mediaFetchSession.flatMapLatest { it.hasCompleted }.filter { it }.first()
            if (mediaSelector.selected.value == null) {
                val selected = mediaSelector.trySelectDefault()
                logger.info { "autoSelectOnFetchCompletion selected: $selected" }
            }
        } catch (e: OwnedCancellationException) {
            e.checkOwner(owner)
        }
    }

    suspend fun doAutoSelectLocal() {
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

private fun createMediaFetchResult(
    subject: SubjectInfo,
    episode: EpisodeInfo
) = MediaFetchRequest(
    subjectId = subject.id.toString(),
    episodeId = episode.id.toString(),
    subjectNameCN = subject.nameCnOrName,
    subjectNames = setOfNotNull(
        subject.nameCn.takeIf { it.isNotBlank() },
        subject.name.takeIf { it.isNotBlank() },
    ),
    episodeSort = episode.sort,
    episodeName = episode.nameCnOrName,
    episodeEp = episode.ep,
)
