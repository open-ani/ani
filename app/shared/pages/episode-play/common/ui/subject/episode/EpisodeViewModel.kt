package me.him188.ani.app.ui.subject.episode

import androidx.annotation.UiThread
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.media.DownloadProviderMediaFetcher
import me.him188.ani.app.data.media.Media
import me.him188.ani.app.data.media.MediaFetchRequest
import me.him188.ani.app.data.media.MediaFetchSession
import me.him188.ani.app.data.media.MediaFetcher
import me.him188.ani.app.data.media.MediaFetcherConfig
import me.him188.ani.app.data.repositories.EpisodeRepository
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.Context
import me.him188.ani.app.torrent.TorrentManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.launchInMain
import me.him188.ani.app.ui.subject.episode.danmaku.PlayerDanmakuViewModel
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorState
import me.him188.ani.app.videoplayer.PlayerState
import me.him188.ani.app.videoplayer.PlayerStateFactory
import me.him188.ani.app.videoplayer.TorrentVideoSource
import me.him188.ani.app.videoplayer.VideoSource
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuMatchers
import me.him188.ani.danmaku.api.DanmakuProvider
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

    // Media Fetching

    /**
     * Emits the progress of the [MediaFetcher] fetching media.
     * Range is `0..1`
     */
    val mediaFetcherProgress: Flow<Float>

    /**
     * Emits `true` if the [MediaFetcher] has completed fetching media.
     */
    val mediaFetcherCompleted: Flow<Boolean>

    // Media Selection

    val mediaSelectorState: MediaSelectorState

    var mediaSelectorVisible: Boolean


    // Video

    /**
     * `true` if a play source is selected by user (or automatically)
     */
    val mediaSelected: Flow<Boolean>

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

    @UiThread
    suspend fun copyDownloadLink(clipboardManager: ClipboardManager, snackbar: SnackbarHostState)

    @UiThread
    suspend fun browsePlaySource(context: Context, snackbar: SnackbarHostState)

    @UiThread
    suspend fun browseDownload(context: Context, snackbar: SnackbarHostState)

    // Danmaku

    val danmaku: PlayerDanmakuViewModel
}

fun EpisodeViewModel(
    initialSubjectId: Int,
    initialEpisodeId: Int,
    initialIsFullscreen: Boolean = false,
    context: Context,
): EpisodeViewModel = EpisodeViewModelImpl(initialSubjectId, initialEpisodeId, initialIsFullscreen, context)


@Stable
private class EpisodeViewModelImpl(
    override val subjectId: Int,
    override val episodeId: Int,
    initialIsFullscreen: Boolean = false,
    context: Context,
) : AbstractViewModel(), KoinComponent, EpisodeViewModel {
    private val bangumiClient by inject<BangumiClient>()
    private val browserNavigator: BrowserNavigator by inject()
    private val torrentManager: TorrentManager by inject()
    private val playerStateFactory: PlayerStateFactory by inject()
    private val episodeRepository: EpisodeRepository by inject()
    private val danmakuProvider: DanmakuProvider by inject()


    private val subjectDetails = flowOf(subjectId).mapLatest { subjectId ->
        runUntilSuccess { withContext(Dispatchers.IO) { bangumiClient.api.getSubjectById(subjectId) } }
        // TODO: replace with data layer 
    }.shareInBackground()

    override val episode: SharedFlow<EpisodeDetail> = flowOf(episodeId).mapLatest { episodeId ->
        runUntilSuccess { withContext(Dispatchers.IO) { bangumiClient.api.getEpisodeById(episodeId) } }
    }.shareInBackground()

    override val subjectTitle: Flow<String> = subjectDetails.filterNotNull().mapLatest { subject ->
        subject.nameCNOrName()
    }

    override val episodeEp = episode.filterNotNull().mapLatest { episode ->
        episode.renderEpisodeSp()
    }

    override val episodeTitle = episode.filterNotNull().mapLatest { episode ->
        episode.nameCNOrName()
    }


    // Media Fetching

    private val mediaFetcher = DownloadProviderMediaFetcher(
        configProvider = { MediaFetcherConfig.Default },
        parentCoroutineContext = backgroundScope.coroutineContext,
    )

    /**
     * A lazy [MediaFetchSession] that is created when [subjectDetails] and [episode] are available.
     */
    val mediaFetchSession = combine(subjectDetails, episode) { subject, episode ->
        mediaFetcher.fetch(
            MediaFetchRequest(
                subjectName = subject.nameCNOrName(),
                episodeName = episode.nameCNOrName(),
                episodeSort = episode.sort.toString(),
            )
        )
    }.shareInBackground(started = SharingStarted.Eagerly)

    override val mediaFetcherProgress: Flow<Float> = mediaFetchSession.flatMapLatest { it.progress }

    override val mediaFetcherCompleted: Flow<Boolean> = mediaFetchSession.flatMapLatest { it.hasCompleted }

    // Media Selection

    override var mediaSelectorVisible: Boolean by mutableStateOf(false)

    override val mediaSelectorState = kotlin.run {
        val state by mediaFetchSession.flatMapLatest { it.cumulativeResults }
            .produceState(emptyList())
        MediaSelectorState(
            mediaListProvider = { state },
            defaultPreferenceProvider = { MediaPreference.Empty }
        )
    }

    private val selectedMedia = snapshotFlow { mediaSelectorState.selected }
        .flowOn(Dispatchers.Main) // access states in Main
        .debounce(1.seconds)
        .flowOn(Dispatchers.Default)

    override val mediaSelected: Flow<Boolean> = selectedMedia.map { it != null }

    /**
     * The [VideoSource] selected to play.
     *
     * `null` has two possible meanings:
     * - List of video sources are still downloading so user has nothing to select.
     * - The sources are available but user has not yet selected one.
     */
    private val videoSource: SharedFlow<VideoSource<*>?> = selectedMedia
        .debounce(1.seconds)
        .distinctUntilChanged()
        .transformLatest { playSource ->
            emit(null)
            playSource?.let { media ->
                try {
                    emit(
                        TorrentVideoSource(
                            torrentManager.downloader.await().fetchTorrent(media.download.uri)
                        )
                    )
                } catch (e: Exception) {
                    emit(null)
                }
            }
        }.shareInBackground()


    override val isVideoReady: Flow<Boolean> = videoSource.map { it != null }

    override val playerState: PlayerState =
        playerStateFactory.create(context, backgroundScope.coroutineContext)

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
        requestMediaOrNull()?.let {
            clipboardManager.setText(AnnotatedString(it.download.uri))
            snackbar.showSnackbar("已复制下载链接")
        }
    }

    override suspend fun browsePlaySource(context: Context, snackbar: SnackbarHostState) {
        requestMediaOrNull()?.let {
            browserNavigator.openBrowser(context, it.originalUrl)
        }
    }

    override suspend fun browseDownload(context: Context, snackbar: SnackbarHostState) {
        requestMediaOrNull()?.let {
            browserNavigator.openMagnetLink(context, it.download.uri)
        }
    }

    override val danmaku: PlayerDanmakuViewModel = PlayerDanmakuViewModel()

    private val danmakuFlow: Flow<Danmaku> = combine(
        selectedMedia.filterNotNull(),
        playerState.videoProperties.filterNotNull()
    ) { media, video ->
        val ep = episode.first()
        danmakuProvider.startSession(
            media.originalTitle,
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

    init {
        launchInMain { // state changes must be in main thread
            playerState.state.collect {
                if (it.isPlaying) {
                    danmaku.danmakuHostState.resume()
                } else {
                    danmaku.danmakuHostState.pause()
                }
            }
        }

        launchInBackground {
            danmakuFlow.collect { danmaku ->
                this.danmaku.danmakuHostState.trySend(danmaku)
            }
        }

        launchInBackground {
            videoSource.collect {
                logger.info { "Got new video source, updating" }
                playerState.setVideoSource(it)
            }
        }
    }

    /**
     * Requests the user to select a media if not already.
     * Returns null if the user cancels the selection.
     */
    private suspend fun requestMediaOrNull(): Media? {
        mediaSelectorState.selected?.let {
            return it // already selected
        }

        mediaSelectorVisible = true
        snapshotFlow { mediaSelectorVisible }.first { !it } // await closed
        return mediaSelectorState.selected
    }
}
