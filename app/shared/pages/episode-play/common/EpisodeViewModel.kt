package me.him188.ani.app.ui.subject.episode

import androidx.annotation.UiThread
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.danmaku.DanmakuManager
import me.him188.ani.app.data.media.resolver.EpisodeMetadata
import me.him188.ani.app.data.media.resolver.UnsupportedMediaException
import me.him188.ani.app.data.media.resolver.VideoSourceResolver
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.Context
import me.him188.ani.app.torrent.MagnetTimeoutException
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.launchInMain
import me.him188.ani.app.ui.subject.episode.danmaku.PlayerDanmakuViewModel
import me.him188.ani.app.ui.subject.episode.mediaFetch.EpisodeMediaFetchSession
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorState
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import me.him188.ani.app.videoplayer.ui.state.PlayerStateFactory
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.danmaku.api.DanmakuSearchRequest
import me.him188.ani.danmaku.api.DanmakuSession
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
import me.him188.ani.datasources.bangumi.processing.renderEpisodeEp
import me.him188.ani.datasources.bangumi.processing.toCollectionType
import me.him188.ani.utils.coroutines.cancellableCoroutineScope
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.Episode
import org.openapitools.client.models.EpisodeCollectionType
import kotlin.time.Duration.Companion.milliseconds

@Immutable
class SubjectPresentation(
    val title: String,
    val isPlaceholder: Boolean = false,
) {
    companion object {
        @Stable
        val Placeholder = SubjectPresentation(
            title = "placeholder",
            isPlaceholder = true,
        )
    }
}

/**
 * 展示在 UI 的状态
 */
@Immutable
class EpisodePresentation(
    /**
     * 剧集标题
     * @see Episode.nameCNOrName
     */
    val title: String,
    /**
     * @see renderEpisodeEp
     */
    val sort: String,
    val collectionType: EpisodeCollectionType,
    val isPlaceholder: Boolean = false,
) {
    companion object {
        @Stable
        val Placeholder = EpisodePresentation(
            title = "placeholder",
            sort = "placeholder",
            collectionType = EpisodeCollectionType.WATCHLIST,
            isPlaceholder = true,
        )
    }
}

@Stable
interface EpisodeViewModel : HasBackgroundScope {
    val subjectId: Int
    val episodeId: Int

    val subjectPresentation: SubjectPresentation // by state
    val episodePresentation: EpisodePresentation // by state

    var isFullscreen: Boolean

    suspend fun setEpisodeCollectionType(type: EpisodeCollectionType)

    // Media Fetching

    /**
     * 查询剧集数据源资源的会话
     */
    val episodeMediaFetchSession: EpisodeMediaFetchSession

    // Media Selection

    /**
     * 是否显示数据源选择器
     */
    var mediaSelectorVisible: Boolean


    // Video

    val videoSourceState: StateFlow<VideoSourceState>

    /**
     * `true` if a play source is selected by user (or automatically)
     */
    val mediaSelected: Boolean

    /**
     * Play controller for video view. This can be saved even when window configuration changes (i.e. everything recomposes).
     */
    val playerState: PlayerState

    @UiThread
    suspend fun copyDownloadLink(clipboardManager: ClipboardManager, snackbar: SnackbarHostState)

    @UiThread
    suspend fun browseMedia(context: Context, snackbar: SnackbarHostState)

    @UiThread
    suspend fun browseDownload(context: Context, snackbar: SnackbarHostState)

    // Danmaku

    val danmaku: PlayerDanmakuViewModel
}

@Stable
val EpisodeViewModel.mediaSelectorState: MediaSelectorState
    get() = episodeMediaFetchSession.mediaSelectorState

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
    private val browserNavigator: BrowserNavigator by inject()
    private val playerStateFactory: PlayerStateFactory by inject()
    private val subjectManager: SubjectManager by inject()
    private val danmakuManager: DanmakuManager by inject()
    private val videoSourceResolver: VideoSourceResolver by inject()

    private val subject = flowOf(subjectId).mapLatest { subjectId ->
        subjectManager.getSubject(subjectId)
    }.shareInBackground()

    // Media Selection

    override val episodeMediaFetchSession = EpisodeMediaFetchSession(
        subjectId,
        episodeId,
        backgroundScope.coroutineContext,
    )

    override var mediaSelectorVisible: Boolean by mutableStateOf(false)

    private val selectedMedia = mediaSelectorState.selectedFlow
        .filterNotNull()
        .distinctUntilChanged()

    override val mediaSelected by derivedStateOf {
        mediaSelectorState.selected != null
    }

    override val videoSourceState: MutableStateFlow<VideoSourceState> = MutableStateFlow(VideoSourceState.Initial)

    /**
     * The [VideoSource] selected to play.
     *
     * `null` has two possible meanings:
     * - List of video sources are still downloading so user has nothing to select.
     * - The sources are available but user has not yet selected one.
     */
    private val videoSource: SharedFlow<VideoSource<*>?> = selectedMedia
        .distinctUntilChanged()
        .transformLatest { playSource ->
            emit(null)
            playSource.let { media ->
                try {
                    val presentation = withContext(Dispatchers.Main) {
                        episodePresentation
                    }
                    videoSourceState.value = VideoSourceState.Resolving
                    emit(
                        videoSourceResolver.resolve(
                            media,
                            EpisodeMetadata(
                                title = presentation.title,
                                sort = EpisodeSort(presentation.sort)
                            )
                        )
                    )
                    videoSourceState.value = VideoSourceState.Succeed
                } catch (e: UnsupportedMediaException) {
                    logger.error(e) { "Failed to resolve video source" }
                    videoSourceState.value = VideoSourceState.UnsupportedMedia
                    emit(null)
                } catch (e: MagnetTimeoutException) {
                    videoSourceState.value = VideoSourceState.ResolutionTimedOut
                    emit(null)
                } catch (_: CancellationException) {
                } catch (e: Throwable) {
                    logger.error(e) { "Failed to resolve video source" }
                    videoSourceState.value = VideoSourceState.UnknownError
                    emit(null)
                }
            }
        }.shareInBackground()


    override val playerState: PlayerState =
        playerStateFactory.create(context, backgroundScope.coroutineContext)

    override val subjectPresentation: SubjectPresentation by subject
        .map {
            SubjectPresentation(
                title = it.nameCNOrName()
            )
        }
        .produceState(SubjectPresentation.Placeholder)

    override val episodePresentation: EpisodePresentation by subjectManager.episodeCollectionFlow(subjectId, episodeId)
        .map {
            EpisodePresentation(
                title = it.episode.nameCNOrName(),
                sort = it.episode.renderEpisodeEp(),
                collectionType = it.type,
            )
        }.produceState(EpisodePresentation.Placeholder)

    override var isFullscreen: Boolean by mutableStateOf(initialIsFullscreen)

    override suspend fun setEpisodeCollectionType(type: EpisodeCollectionType) {
        subjectManager.setEpisodeCollectionType(subjectId, episodeId, type.toCollectionType())
    }

    override suspend fun copyDownloadLink(clipboardManager: ClipboardManager, snackbar: SnackbarHostState) {
        requestMediaOrNull()?.let {
            clipboardManager.setText(AnnotatedString(it.download.uri))
            snackbar.showSnackbar("已复制下载链接")
        }
    }

    override suspend fun browseMedia(context: Context, snackbar: SnackbarHostState) {
        requestMediaOrNull()?.let {
            browserNavigator.openBrowser(context, it.originalUrl)
        }
    }

    override suspend fun browseDownload(context: Context, snackbar: SnackbarHostState) {
        requestMediaOrNull()?.let {
            browserNavigator.openMagnetLink(context, it.download.uri)
        }
    }

    override val danmaku: PlayerDanmakuViewModel = PlayerDanmakuViewModel().also {
        addCloseable(it)
    }

    private val danmakuSessionFlow: Flow<DanmakuSession> = combine(
        selectedMedia.filterNotNull(),
        playerState.videoProperties.distinctUntilChangedBy { it?.filename }.filterNotNull()
    ) { media, video ->
        val filename = video.filename.takeIf { it.isNotBlank() }
            ?: video.title
            ?: media.originalTitle
        logger.info { "Search for danmaku with filename='$filename', fileHash=${video.fileHash}, length=${video}" }

        val subject: SubjectPresentation
        val episode: EpisodePresentation
        withContext(Dispatchers.Main.immediate) {
            subject = subjectPresentation
            episode = episodePresentation
        }
        danmakuManager.fetch(
            request = DanmakuSearchRequest(
                subjectId = subjectId,
                subjectName = subject.title,
                episodeId = episodeId,
                episodeSort = EpisodeSort(episode.sort),
                episodeName = episode.title,
                filename = filename,
                fileHash = video.fileHash ?: "aa".repeat(16),
                fileSize = video.fileLengthBytes,
                videoDuration = video.durationMillis.milliseconds,
            ),
        )
    }.shareInBackground(started = SharingStarted.Lazily)

    private val danmakuFlow: Flow<Danmaku> = danmakuSessionFlow.flatMapLatest { session ->
        session.at(progress = playerState.currentPositionMillis.map { it.milliseconds })
    }

    private val selfUserId = danmakuManager.selfId

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
            cancellableCoroutineScope {
                val selfId = selfUserId.stateIn(this, started = SharingStarted.Eagerly, initialValue = null)
                danmakuFlow.collect { data ->
                    danmaku.danmakuHostState.trySend(
                        DanmakuPresentation(
                            data,
                            isSelf = selfId.value == data.id
                        ),
                    )
                }
                cancel()
            }
        }

        launchInBackground {
            videoSource.collect {
                logger.info { "EpisodeViewModel got new video source: $it, updating playerState" }
                playerState.setVideoSource(it)
            }
        }
    }

    /**
     * Requests the user to select a media if not already.
     * Returns null if the user cancels the selection.
     */
    @UiThread
    private suspend fun requestMediaOrNull(): Media? {
        mediaSelectorState.selected?.let {
            return it // already selected
        }

        mediaSelectorVisible = true
        snapshotFlow { mediaSelectorVisible }.first { !it } // await closed
        return mediaSelectorState.selected
    }
}

sealed class VideoSourceState {
    data object Initial : VideoSourceState()
    data object Resolving : VideoSourceState()

    sealed class Failed : VideoSourceState()
    data object ResolutionTimedOut : Failed()
    data object UnsupportedMedia : Failed()
    data object UnknownError : Failed()

    data object Succeed : VideoSourceState()
}