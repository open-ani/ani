package me.him188.ani.app.ui.subject.episode.video

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.data.models.episode.displayName
import me.him188.ani.app.data.source.media.resolver.EpisodeMetadata
import me.him188.ani.app.data.source.media.resolver.ResolutionFailures
import me.him188.ani.app.data.source.media.resolver.TorrentVideoSource
import me.him188.ani.app.data.source.media.resolver.UnsupportedMediaException
import me.him188.ani.app.data.source.media.resolver.VideoSourceResolutionException
import me.him188.ani.app.data.source.media.resolver.VideoSourceResolver
import me.him188.ani.app.data.source.media.selector.MediaSelector
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.subject.episode.statistics.DelegateVideoStatistics
import me.him188.ani.app.ui.subject.episode.statistics.VideoLoadingState
import me.him188.ani.app.ui.subject.episode.statistics.VideoStatistics
import me.him188.ani.app.videoplayer.data.OpenFailures
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.data.VideoSourceOpenException
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import kotlin.coroutines.CoroutineContext

/**
 * 将 [MediaSelector] 和 [videoSourceResolver] 结合, 为 [playerState] 提供视频源.
 * 还会提供 [videoStatistics], 可以获取当前的加载状态.
 *
 * 这实际上就是启动了一个一直运行的协程:
 * 当 [MediaSelector] 选择到资源的时候, 使用 [videoSourceResolver] 解析出 [VideoSource],
 * 然后把它设置给 [playerState] (通过 [PlayerState.setVideoSource]).
 */
class PlayerLauncher(
    mediaSelector: MediaSelector,
    private val videoSourceResolver: VideoSourceResolver,
    private val playerState: PlayerState,
    episodeInfo: Flow<EpisodeInfo?>,
    mediaSourceLoading: Flow<Boolean>,
    parentCoroutineContext: CoroutineContext,
) : HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    private companion object {
        val logger = logger<PlayerLauncher>()
    }

    private val videoLoadingStateFlow: MutableStateFlow<VideoLoadingState> = MutableStateFlow(VideoLoadingState.Initial)

    val videoStatistics: VideoStatistics = DelegateVideoStatistics(
        playingMedia = mediaSelector.selected.produceState(),
        playingFilename = playerState.videoData.map { it?.filename }.produceState(null),
        mediaSourceLoading = mediaSourceLoading.produceState(true),
        videoLoadingState = videoLoadingStateFlow.produceState(VideoLoadingState.Initial),
    )

    /**
     * The [VideoSource] selected to play.
     *
     * `null` has two possible meanings:
     * - List of video sources are still downloading so user has nothing to select.
     * - The sources are available but user has not yet selected one.
     */
    private val videoSource: SharedFlow<VideoSource<*>?> = mediaSelector.selected
        .transformLatest { media ->
            emit(null)
            if (media == null) return@transformLatest

            try {
                val info = episodeInfo.filterNotNull().first()
                videoLoadingStateFlow.value = VideoLoadingState.ResolvingSource
                emit(
                    videoSourceResolver.resolve(
                        media,
                        EpisodeMetadata(
                            title = info.displayName,
                            ep = info.ep,
                            sort = info.sort,
                        ),
                    ),
                )
                videoLoadingStateFlow.compareAndSet(VideoLoadingState.ResolvingSource, VideoLoadingState.DecodingData)
            } catch (e: UnsupportedMediaException) {
                logger.error { IllegalStateException("Failed to resolve video source, unsupported media", e) }
                videoLoadingStateFlow.value = VideoLoadingState.UnsupportedMedia
                emit(null)
            } catch (e: VideoSourceResolutionException) {
                logger.error { IllegalStateException("Failed to resolve video source with known error", e) }
                videoLoadingStateFlow.value = when (e.reason) {
                    ResolutionFailures.FETCH_TIMEOUT -> VideoLoadingState.ResolutionTimedOut
                    ResolutionFailures.ENGINE_ERROR -> VideoLoadingState.UnknownError(e)
                    ResolutionFailures.NETWORK_ERROR -> VideoLoadingState.NetworkError
                }
                emit(null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                logger.error { IllegalStateException("Failed to resolve video source with unknown error", e) }
                videoLoadingStateFlow.value = VideoLoadingState.UnknownError(e)
                emit(null)
            }
        }.shareInBackground(SharingStarted.Lazily)

    init {
        launchInBackground {
            videoSource.collectLatest { source ->
                logger.info { "Got new video source: $source, updating playerState" }
                try {
                    playerState.setVideoSource(source)
                    if (source == null) {
                        videoLoadingStateFlow.value = VideoLoadingState.Initial
                    } else {
                        logger.info { "playerState.setVideoSource success" }
                        videoLoadingStateFlow.value = VideoLoadingState.Succeed(isBt = source is TorrentVideoSource)
                    }
                } catch (e: VideoSourceOpenException) {
                    videoLoadingStateFlow.value = when (e.reason) {
                        OpenFailures.NO_MATCHING_FILE -> VideoLoadingState.NoMatchingFile
                        OpenFailures.UNSUPPORTED_VIDEO_SOURCE -> VideoLoadingState.UnsupportedMedia
                        OpenFailures.ENGINE_DISABLED -> VideoLoadingState.UnsupportedMedia
                    }
                } catch (_: CancellationException) {
                    videoLoadingStateFlow.value = VideoLoadingState.Cancelled
                    // ignore
                    return@collectLatest
                } catch (e: Throwable) {
                    logger.error(e) { "Failed to set video source" }
                    videoLoadingStateFlow.value = VideoLoadingState.UnknownError(e)
                }
            }
        }
    }
}
