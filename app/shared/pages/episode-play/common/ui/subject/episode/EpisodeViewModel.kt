package me.him188.ani.app.ui.subject.episode

import androidx.annotation.UiThread
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.EpisodeRepository
import me.him188.ani.app.data.PreferencesRepository
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.Context
import me.him188.ani.app.torrent.TorrentManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.launchInMain
import me.him188.ani.app.videoplayer.PlayerState
import me.him188.ani.app.videoplayer.PlayerStateFactory
import me.him188.ani.app.videoplayer.TorrentVideoSource
import me.him188.ani.app.videoplayer.VideoSource
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuMatchers
import me.him188.ani.danmaku.api.DanmakuProvider
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.danmaku.ui.DanmakuHostState
import me.him188.ani.datasources.api.DownloadProvider
import me.him188.ani.datasources.api.DownloadSearchQuery
import me.him188.ani.datasources.api.PagedSource
import me.him188.ani.datasources.api.awaitFinished
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.Topic
import me.him188.ani.datasources.api.topic.TopicCategory
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
import me.him188.ani.datasources.bangumi.processing.renderEpisodeSp
import me.him188.ani.utils.coroutines.closeOnReplacement
import me.him188.ani.utils.coroutines.runUntilSuccess
import me.him188.ani.utils.logging.info
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.EpisodeDetail
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Stable
interface EpisodeViewModel : HasBackgroundScope {
    // Subject
    val subjectId: Int

    val subjectTitle: Flow<String>

    // Episode

    val episodeId: Int

    val episode: SharedFlow<EpisodeDetail>

    val episodeTitle: Flow<String>
    val episodeEp: Flow<String>

    val isFullscreen: StateFlow<Boolean>

    fun setFullscreen(fullscreen: Boolean)

    // Collection

    val episodeCollectionType: SharedFlow<EpisodeCollectionType>
    suspend fun setEpisodeCollectionType(type: EpisodeCollectionType)

    // Video

    /**
     * `true` if a play source is selected by user (or automatically)
     */
    val playSourceSelected: Flow<Boolean>

    /**
     * `true` if the list of play sources are still downloading (e.g. from dmhy).
     */
    val isPlaySourcesLoading: StateFlow<Boolean>

    val playSourceSelector: PlaySourceSelector

    /**
     * `true` if the video is ready to play.
     *
     * This does not guarantee that there is enough buffer to play the video.
     * For torrent videos, `true` means the magnet link is resolved and we can start downloading.
     */
    val isVideoReady: Flow<Boolean>

    /**
     * Play controller for video view. This can be saved even when window configuration changes (i.e. everything recomposes).
     */
    val playerState: PlayerState

    /**
     * `true` if the bottom sheet for choosing play source should be shown.
     */
    val isShowPlaySourceSheet: StateFlow<Boolean>
    fun setShowPlaySourceSheet(show: Boolean)

    @UiThread
    suspend fun copyDownloadLink(clipboardManager: ClipboardManager, snackbar: SnackbarHostState)

    @UiThread
    suspend fun browsePlaySource(context: Context, snackbar: SnackbarHostState)

    @UiThread
    suspend fun browseDownload(context: Context, snackbar: SnackbarHostState)

    // Danmaku

    val danmakuHostState: DanmakuHostState

    val danmakuEnabled: Flow<Boolean>

    fun setDanmakuEnabled(enabled: Boolean)

    val danmakuConfig: Flow<DanmakuConfig>
}

fun EpisodeViewModel(
    initialSubjectId: Int,
    initialEpisodeId: Int,
    initialIsFullscreen: Boolean = false,
    context: Context,
): EpisodeViewModel = EpisodeViewModelImpl(initialSubjectId, initialEpisodeId, initialIsFullscreen, context)


private class EpisodeViewModelImpl(
    override val subjectId: Int,
    override val episodeId: Int,
    initialIsFullscreen: Boolean = false,
    context: Context,
) : AbstractViewModel(), KoinComponent, EpisodeViewModel {
    private val bangumiClient by inject<BangumiClient>()
    private val downloadProvider by inject<DownloadProvider>()
    private val browserNavigator: BrowserNavigator by inject()
    private val torrentManager: TorrentManager by inject()
    private val playerStateFactory: PlayerStateFactory by inject()
    private val episodeRepository: EpisodeRepository by inject()
    private val danmakuProvider: DanmakuProvider by inject()
    private val preferencesRepository by inject<PreferencesRepository>()

    private val subjectDetails = flowOf(subjectId).mapLatest { subjectId ->
        runUntilSuccess { withContext(Dispatchers.IO) { bangumiClient.api.getSubjectById(subjectId) } }
        // TODO: replace with data layer 
    }.shareInBackground()

    @Stable
    override val episode: SharedFlow<EpisodeDetail> = flowOf(episodeId).mapLatest { episodeId ->
        runUntilSuccess { withContext(Dispatchers.IO) { bangumiClient.api.getEpisodeById(episodeId) } }
    }.shareInBackground()

    @Stable
    override val subjectTitle: Flow<String> = subjectDetails.filterNotNull().mapLatest { subject ->
        subject.nameCNOrName()
    }

    @Stable
    override val episodeEp = episode.filterNotNull().mapLatest { episode ->
        episode.renderEpisodeSp()
    }

    @Stable
    override val episodeTitle = episode.filterNotNull().mapLatest { episode ->
        episode.nameCNOrName()
    }

    // 动漫花园等数据源搜搜结果
    private val _isPlaySourcesLoading = MutableStateFlow(true)

    @Stable
    override val isPlaySourcesLoading: StateFlow<Boolean> get() = _isPlaySourcesLoading

    @Stable
    private val playSources: SharedFlow<Collection<PlaySource>?> =
        combine(episode, subjectTitle) { episode, subjectTitle ->
            episode to subjectTitle
        }.mapNotNull { (episode, subjectTitle) ->

            _isPlaySourcesLoading.emit(true)
            val session = downloadProvider.startSearch(
                DownloadSearchQuery(
                    keywords = subjectTitle,
                    category = TopicCategory.ANIME,
                )
            )
            // 等完成时将 _isPlaySourcesLoading 设置为 false
            launchInBackground {
                session.awaitFinished()
                _isPlaySourcesLoading.emit(false)
            }

            processDmhyResults(session, episode)
        }.transformLatest { flow ->
            val list = ConcurrentLinkedQueue<PlaySource>()
            flow.collect { source ->
                list.add(source)
                emit(list.distinctBy { it.originalTitle })
            }
        }.shareInBackground()

    private fun processDmhyResults(
        session: PagedSource<Topic>,
        currentEpisode: EpisodeDetail
    ) = session.results
        .filter { it.details != null }
        .filter {
            it.details!!.episode?.toString()?.removePrefix("0") == currentEpisode.ep?.toString()?.removePrefix("0")
        }
        .map {
            val details = it.details!!
            PlaySource(
                id = it.id,
                alliance = it.alliance,
                subtitleLanguage = details.subtitleLanguages.firstOrNull()?.toString() ?: "生肉",
                resolution = details.resolution ?: Resolution.R1080P, // 默认 1080P, 因为目前大概都是 1080P
                dataSource = "TODO",
                originalUrl = it.link,
                magnetLink = it.magnetLink,
                originalTitle = it.rawTitle,
                size = it.size,
            )
        }

    @Stable
    override val playSourceSelector = PlaySourceSelector(
        subjectDetails.map { it.id },
        listOf(),
        playSources.filterNotNull(),
        backgroundScope
    )

    @Stable
    override val playSourceSelected: Flow<Boolean> =
        playSourceSelector.targetPlaySourceCandidate.map { it != null }


    /**
     * The [VideoSource] selected to play.
     *
     * `null` has two possible meanings:
     * - List of video sources are still downloading so user has nothing to select.
     * - The sources are available but user has not yet selected one.
     */
    @Stable
    private val videoSource: SharedFlow<VideoSource<*>?> = playSourceSelector.targetPlaySourceCandidate
        .debounce(1.seconds)
        .distinctUntilChanged()
        .transformLatest { playSource ->
            emit(null)
            playSource?.let {
                try {
                    emit(TorrentVideoSource(torrentManager.downloader.await().fetchMagnet(it.playSource.magnetLink)))
                } catch (e: Exception) {
                    emit(null)
                }
            }
        }.shareInBackground()


    @Stable
    override val isVideoReady: Flow<Boolean> = videoSource.map { it != null }

    @Stable
    override val playerState: PlayerState =
        playerStateFactory.create(context, backgroundScope.coroutineContext)

    override val isShowPlaySourceSheet = MutableStateFlow(false)
    override fun setShowPlaySourceSheet(show: Boolean) {
        isShowPlaySourceSheet.value = show
    }

    @Stable
    override val isFullscreen: MutableStateFlow<Boolean> = MutableStateFlow(initialIsFullscreen)

    override fun setFullscreen(fullscreen: Boolean) {
        isFullscreen.value = fullscreen
    }

    override val episodeCollectionType: MutableSharedFlow<EpisodeCollectionType> = flowOf(episodeId).mapNotNull {
        episodeRepository.getEpisodeCollection(it)?.type
    }.localCachedSharedFlow()

    override suspend fun setEpisodeCollectionType(type: EpisodeCollectionType) {
        episodeCollectionType.tryEmit(type)
        episodeRepository.setEpisodeCollection(subjectId, listOf(episodeId), type)
    }

    override suspend fun copyDownloadLink(clipboardManager: ClipboardManager, snackbar: SnackbarHostState) {
        requestPlaySourceCandidate()?.let {
            clipboardManager.setText(AnnotatedString(it.playSource.magnetLink))
            snackbar.showSnackbar("已复制下载链接")
        }
    }

    override suspend fun browsePlaySource(context: Context, snackbar: SnackbarHostState) {
        requestPlaySourceCandidate()?.let {
            browserNavigator.openBrowser(context, it.playSource.originalUrl)
        }
    }

    override suspend fun browseDownload(context: Context, snackbar: SnackbarHostState) {
        requestPlaySourceCandidate()?.let {
            browserNavigator.openMagnetLink(context, it.playSource.magnetLink)
        }
    }

    private val danmakuFlow: Flow<Danmaku> = combine(
        playSourceSelector.targetPlaySourceCandidate.filterNotNull(),
        playerState.videoProperties.filterNotNull()
    ) { playSourceCandidate, video ->
        val ep = episode.first()
        danmakuProvider.startSession(
            playSourceCandidate.playSource.originalTitle,
            video.fileHash ?: "aa".repeat(16),
            video.fileLengthBytes,
            video.durationMillis.milliseconds,
            DanmakuMatchers.mostRelevant(subjectTitle.first(), "第${ep.ep ?: "1"}话 " + episodeTitle.first()),
        )
    }.filterNotNull()
        .closeOnReplacement()
        .flatMapLatest { session ->
            session.at(playerState.currentPositionMillis.map { it.milliseconds })
        }

    override val danmakuHostState: DanmakuHostState = DanmakuHostState()

    init {
        launchInMain { // state changes must be in main thread
            playerState.state.collect {
                if (it.isPlaying) {
                    danmakuHostState.resume()
                } else {
                    danmakuHostState.pause()
                }
            }
        }

        launchInBackground {
            danmakuFlow.collect { danmaku ->
                danmakuHostState.trySend(danmaku)
            }
        }

        launchInBackground {
            videoSource.collect {
                logger.info { "Got new video source, updating" }
                playerState.setVideoSource(it)
            }
        }
    }

    override val danmakuEnabled: Flow<Boolean> = preferencesRepository.danmakuEnabled.flow

    override fun setDanmakuEnabled(enabled: Boolean) {
        launchInBackground {
            preferencesRepository.danmakuEnabled.set(enabled)
        }
    }

    override val danmakuConfig: Flow<DanmakuConfig> = preferencesRepository.danmakuConfig.flow

    private suspend fun requestPlaySourceCandidate(): PlaySourceCandidate? {
        val candidate = playSourceSelector.targetPlaySourceCandidate.value
        if (candidate != null) {
            return candidate
        }
        setShowPlaySourceSheet(true)
        isShowPlaySourceSheet.first { !it } // await closed
        return playSourceSelector.targetPlaySourceCandidate.value
    }
}
