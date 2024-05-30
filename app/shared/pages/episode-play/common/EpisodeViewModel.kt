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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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
import me.him188.ani.app.data.media.resolver.ResolutionFailures
import me.him188.ani.app.data.media.resolver.UnsupportedMediaException
import me.him188.ani.app.data.media.resolver.VideoSourceResolutionException
import me.him188.ani.app.data.media.resolver.VideoSourceResolver
import me.him188.ani.app.data.models.MediaSelectorSettings
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.platform.Context
import me.him188.ani.app.tools.caching.ContentPolicy
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.launchInMain
import me.him188.ani.app.ui.subject.episode.danmaku.PlayerDanmakuViewModel
import me.him188.ani.app.ui.subject.episode.mediaFetch.EpisodeMediaFetchSession
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.statistics.DanmakuLoadingState
import me.him188.ani.app.ui.subject.episode.statistics.PlayerStatisticsState
import me.him188.ani.app.videoplayer.data.OpenFailures
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.data.VideoSourceOpenException
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import me.him188.ani.app.videoplayer.ui.state.PlayerStateFactory
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuEvent
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
    val info: SubjectInfo,
) {
    companion object {
        @Stable
        val Placeholder = SubjectPresentation(
            title = "placeholder",
            isPlaceholder = true,
            info = SubjectInfo.Empty,
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
     * 在当前季度中的集数, 例如第二季的第一集为 01
     *
     * @see renderEpisodeEp
     */
    val ep: String,
    /**
     * 在系列中的集数, 例如第二季的第一集为 26
     *
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
            ep = "placeholder",
            sort = "placeholder",
            collectionType = EpisodeCollectionType.WATCHLIST,
            isPlaceholder = true,
        )
    }
}

@Stable
interface EpisodeViewModel : HasBackgroundScope {
    val videoSourceResolver: VideoSourceResolver

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
    val mediaSelectorSettings: MediaSelectorSettings
    val mediaSelectorPresentation: MediaSelectorPresentation

    val playerStatistics: PlayerStatisticsState

    // Media Selection

    /**
     * 是否显示数据源选择器
     */
    var mediaSelectorVisible: Boolean


    // Video

    val videoLoadingState: StateFlow<VideoLoadingState> get() = playerStatistics.videoLoadingState

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
val EpisodeViewModel.mediaSelector get() = episodeMediaFetchSession.mediaSelector

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
    override val videoSourceResolver: VideoSourceResolver by inject()
    private val settingsRepository: SettingsRepository by inject()

    private val subjectInfo = flowOf(subjectId).mapLatest { subjectId ->
        subjectManager.getSubjectInfo(subjectId)
    }.shareInBackground()

    // Media Selection

    override val episodeMediaFetchSession = EpisodeMediaFetchSession(
        subjectId,
        episodeId,
        backgroundScope.coroutineContext,
    )
    override val mediaSelectorSettings: MediaSelectorSettings by
    settingsRepository.mediaSelectorSettings.flow.produceState(MediaSelectorSettings.Default)
    override val mediaSelectorPresentation: MediaSelectorPresentation by lazy {
        MediaSelectorPresentation(
            mediaSelector,
            backgroundScope
        )
    }
    override val playerStatistics: PlayerStatisticsState = PlayerStatisticsState()

    override var mediaSelectorVisible: Boolean by mutableStateOf(false)

    private val selectedMedia = mediaSelectorPresentation.mediaSelector.selected
        .filterNotNull()
        .distinctUntilChanged()

    override val mediaSelected by derivedStateOf {
        mediaSelectorPresentation.selected != null
    }

    override val videoLoadingState get() = playerStatistics.videoLoadingState

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
                    videoLoadingState.value = VideoLoadingState.ResolvingSource
                    emit(
                        videoSourceResolver.resolve(
                            media,
                            EpisodeMetadata(
                                title = presentation.title,
                                ep = EpisodeSort(presentation.ep),
                                sort = EpisodeSort(presentation.sort)
                            )
                        )
                    )
                    videoLoadingState.compareAndSet(VideoLoadingState.ResolvingSource, VideoLoadingState.DecodingData)
                } catch (e: UnsupportedMediaException) {
                    logger.error { IllegalStateException("Failed to resolve video source, unsupported media", e) }
                    videoLoadingState.value = VideoLoadingState.UnsupportedMedia
                    emit(null)
                } catch (e: VideoSourceResolutionException) {
                    videoLoadingState.value = when (e.reason) {
                        ResolutionFailures.FETCH_TIMEOUT -> VideoLoadingState.ResolutionTimedOut
                        ResolutionFailures.ENGINE_ERROR -> VideoLoadingState.UnknownError(e)
                    }
                    emit(null)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    logger.error { IllegalStateException("Failed to resolve video source", e) }
                    videoLoadingState.value = VideoLoadingState.UnknownError(e)
                    emit(null)
                }
            }
        }.shareInBackground(SharingStarted.Lazily)


    override val playerState: PlayerState =
        playerStateFactory.create(context, backgroundScope.coroutineContext)

    override val subjectPresentation: SubjectPresentation by subjectInfo
        .map {
            SubjectPresentation(title = it.displayName, info = it)
        }
        .produceState(SubjectPresentation.Placeholder)

    override val episodePresentation: EpisodePresentation by
    subjectManager.episodeCollectionFlow(subjectId, episodeId, ContentPolicy.CACHE_ONLY)
        .map {
            EpisodePresentation(
                title = it.episode.nameCNOrName(),
                ep = it.episode.renderEpisodeEp(),
                sort = EpisodeSort(it.episode.sort).toString(),
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

    private val danmakuSessionFlow: Flow<DanmakuSession> = playerState.videoData.filterNotNull().mapLatest { data ->
        playerStatistics.danmakuLoadingState.value = DanmakuLoadingState.Loading
        val filename = data.filename
//            ?: selectedMedia.first().originalTitle
        try {

            val subject: SubjectPresentation
            val episode: EpisodePresentation
            withContext(Dispatchers.Main.immediate) {
                subject = subjectPresentation
                episode = episodePresentation
            }
            val result = danmakuManager.fetch(
                request = DanmakuSearchRequest(
                    subjectId = subjectId,
                    subjectPrimaryName = subject.info.displayName,
                    subjectNames = subject.info.allNames,
                    subjectPublishDate = subject.info.publishDate,
                    episodeId = episodeId,
                    episodeSort = EpisodeSort(episode.sort),
                    episodeEp = EpisodeSort(episode.ep),
                    episodeName = episode.title,
                    filename = filename,
                    fileHash = "aa".repeat(16),
                    fileSize = data.fileLength,
                    videoDuration = 0.milliseconds,
//                fileHash = video.fileHash ?: "aa".repeat(16),
//                fileSize = video.fileLengthBytes,
//                videoDuration = video.durationMillis.milliseconds,
                ),
            )
            playerStatistics.danmakuLoadingState.value = DanmakuLoadingState.Success(result.matchInfos)
            result.danmakuSession
        } catch (e: Throwable) {
            playerStatistics.danmakuLoadingState.value = DanmakuLoadingState.Failed(e)
            throw e
        }
    }.shareInBackground(started = SharingStarted.Lazily)

    private val danmakuFlow: Flow<DanmakuEvent> = danmakuSessionFlow.flatMapLatest { session ->
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
            videoLoadingState.collect {
                playerStatistics.videoLoadingState.value = it
            }
        }

        launchInBackground {
            cancellableCoroutineScope {
                val selfId = selfUserId.stateIn(this, started = SharingStarted.Eagerly, initialValue = null)
                val danmakuConfig = settingsRepository.danmakuConfig.flow.stateIn(
                    this,
                    started = SharingStarted.Eagerly,
                    initialValue = null
                )
                danmakuFlow.collect { event ->
                    when (event) {
                        is DanmakuEvent.Add -> {
                            val data = event.danmaku
                            danmaku.danmakuHostState.trySend(
                                createDanmakuPresentation(data, selfId.value),
                            )
                        }

                        is DanmakuEvent.Repopulate -> {
                            danmaku.danmakuHostState.repopulate(
                                event.list.map {
                                    createDanmakuPresentation(it, selfId.value)
                                },
                                danmakuConfig.filterNotNull().first().style,
                            )

                        }
                    }
                }
                cancelScope()
            }
        }

        launchInBackground {
            videoSource.collect {
                logger.info { "EpisodeViewModel got new video source: $it, updating playerState" }
                try {
                    playerState.setVideoSource(it)
                } catch (e: VideoSourceOpenException) {
                    videoLoadingState.value = when (e.reason) {
                        OpenFailures.NO_MATCHING_FILE -> VideoLoadingState.NoMatchingFile
                        OpenFailures.UNSUPPORTED_VIDEO_SOURCE -> VideoLoadingState.UnsupportedMedia
                        OpenFailures.ENGINE_DISABLED -> VideoLoadingState.UnsupportedMedia
                    }
                } catch (_: CancellationException) {
                    // ignore
                    return@collect
                } catch (e: Throwable) {
                    logger.error(e) { "Failed to set video source" }
                    videoLoadingState.value = VideoLoadingState.UnknownError(e)
                }
                videoLoadingState.value = VideoLoadingState.Succeed
            }
        }
    }

    private fun createDanmakuPresentation(
        data: Danmaku,
        selfId: String?,
    ) = DanmakuPresentation(
        data,
        isSelf = selfId == data.senderId
    )

    /**
     * Requests the user to select a media if not already.
     * Returns null if the user cancels the selection.
     */
    @UiThread
    private suspend fun requestMediaOrNull(): Media? {
        mediaSelectorPresentation.selected?.let {
            return it // already selected
        }

        mediaSelectorVisible = true
        snapshotFlow { mediaSelectorVisible }.first { !it } // await closed
        return mediaSelectorPresentation.selected
    }
}

sealed interface VideoLoadingState {
    sealed interface Progressing : VideoLoadingState

    /**
     * 等待选择 [Media]
     */
    data object Initial : VideoLoadingState

    /**
     * 在解析磁力链/寻找文件
     */
    data object ResolvingSource : VideoLoadingState, Progressing

    /**
     * 在寻找种子资源中的正确的文件, 并打开文件
     */
    data object DecodingData : VideoLoadingState, Progressing

    /**
     * 文件成功找到
     */
    data object Succeed : VideoLoadingState, Progressing

    sealed class Failed : VideoLoadingState
    data object ResolutionTimedOut : Failed()

    /**
     * 不支持的媒体, 或者说是未启用支持该媒体的 [VideoSourceResolver]
     */
    data object UnsupportedMedia : Failed()
    data object NoMatchingFile : Failed()
    data class UnknownError(
        val cause: Throwable,
    ) : Failed()
}