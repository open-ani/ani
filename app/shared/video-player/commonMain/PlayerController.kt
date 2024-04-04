package me.him188.ani.app.videoplayer

import androidx.annotation.UiThread
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import me.him188.ani.app.platform.Context
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A controller for the [VideoPlayer].
 */
@Stable
interface PlayerController {
    /**
     * Current state of the player.
     *
     * State can be changed internally e.g. buffer exhausted or externally by e.g. [pause], [resume].
     */
    val state: StateFlow<PlayerState>

    /**
     * The video source that is currently being played.
     */
    val videoSource: StateFlow<VideoSource<*>?>

    /**
     * Sets the video source to play, by [opening][VideoSource.open] the [source],
     * updating [videoSource], and resetting the progress to 0.
     *
     * Suspends until the new source has been updated.
     *
     * If this function failed to [start video streaming][VideoSource.open], it will throw an exception.
     *
     * This function must not be called on the main thread as it will call [VideoSource.open].
     *
     * @param source the video source to play. `null` to stop playing.
     * @throws UnsupportedVideoSourceException if the video source is not supported by this player.
     */
    suspend fun setVideoSource(source: VideoSource<*>?)

    /**
     * Properties of the video being played.
     *
     * Note that it may not be available immediately after [setVideoSource] returns,
     * since the properties may be callback from the underlying player implementation.
     */
    val videoProperties: StateFlow<VideoProperties?>

    /**
     * 是否正在 buffer (暂停视频中)
     */
    val isBuffering: Flow<Boolean>

    /**
     * Current position of the video being played.
     *
     * `0` if no video is being played.
     */
    val currentPosition: StateFlow<Duration>

    /**
     * `0..100`
     */
    val bufferedPercentage: StateFlow<Int>

    /**
     * 当前播放进度比例 `0..1`
     */
    val playProgress: Flow<Float>

    /**
     * 暂停播放, 直到 [pause]
     */
    @UiThread
    fun pause()

    /**
     * 恢复播放
     */
    @UiThread
    fun resume()


    /**
     * 播放进度条的拖动位置 `0..1`. `null` 表示没有拖动.
     */
    @Deprecated("To be moved out")
    val previewingProgress: StateFlow<Float?>

    @Deprecated("To be moved out")
    fun setPreviewingProgress(progress: Float)

    /**
     * 如果当前正在拖动, 则为 [previewingProgress], 否则为 [currentPosition].
     */
    val previewingOrPlayingProgress: Flow<Float>


    @UiThread
    fun setSpeed(speed: Float)

    /**
     * 跳转到指定位置
     */
    @UiThread
    fun seekTo(duration: Duration)
}

class UnsupportedVideoSourceException(
    val source: VideoSource<*>,
    player: PlayerController,
) : RuntimeException("Video source is not supported by player '${player}': $source")

fun PlayerController.togglePause() {
    if (state.value.isPlaying) {
        pause()
    } else {
        resume()
    }
}

abstract class AbstractPlayerController : PlayerController {
    override val isBuffering: Flow<Boolean> by lazy {
        state.map { it == PlayerState.PAUSED_BUFFERING }
    }

    final override val previewingProgress: MutableStateFlow<Float?> = MutableStateFlow(null)

    final override fun setPreviewingProgress(progress: Float) {
        previewingProgress.value = progress
        isPreviewing.tryEmit(true)
    }

    private val isPreviewing: MutableSharedFlow<Boolean> = MutableSharedFlow(replay = 1, extraBufferCapacity = 1)

    final override val previewingOrPlayingProgress: Flow<Float> by lazy {
        combine(
            previewingProgress.filterNotNull(),
            playProgress,
            isPreviewing.debounce {
                if (it) {
                    0.seconds
                } else {
                    2.seconds
                }
            },
        ) { previewingProgress, playingProgress, isPreviewing ->
            if (isPreviewing) {
                previewingProgress
            } else {
                playingProgress
            }
        }
    }
}


enum class PlayerState(
    val isPlaying: Boolean,
) {
    /**
     * Player is loaded and will be playing as soon as metadata and first frame is available.
     */
    READY(isPlaying = false),

    /**
     * 用户主动暂停. buffer 继续充, 但是充好了也不要恢复 [PLAYING].
     */
    PAUSED(isPlaying = false),

    PLAYING(isPlaying = true),

    /**
     * 播放中但因没 buffer 就暂停了. buffer 填充后恢复 [PLAYING].
     */
    PAUSED_BUFFERING(isPlaying = false),

    FINISHED(isPlaying = false),
    ;
}

fun interface PlayerControllerFactory {
    fun create(context: Context, parentCoroutineContext: CoroutineContext): PlayerController
}

/**
 * For previewing
 */
class DummyPlayerController : AbstractPlayerController() {
    override val state: StateFlow<PlayerState> = MutableStateFlow(PlayerState.PAUSED_BUFFERING)
    override val videoSource: MutableStateFlow<VideoSource<*>?> = MutableStateFlow(null)
    override suspend fun setVideoSource(source: VideoSource<*>?) {
        videoSource.value = source
    }

    override val videoProperties: MutableStateFlow<VideoProperties> = MutableStateFlow(
        VideoProperties(
            title = "Test Video",
            heightPx = 1080,
            widthPx = 1920,
            videoBitrate = 100,
            audioBitrate = 100,
            frameRate = 30f,
            duration = 100.seconds,
        )
    )
    override val isBuffering: Flow<Boolean> = MutableStateFlow(true)
    override val currentPosition: MutableStateFlow<Duration> = MutableStateFlow(10.seconds)
    override val bufferedPercentage: StateFlow<Int> = MutableStateFlow(50)
    override val playProgress: Flow<Float> = currentPosition.combine(videoProperties) { played, video ->
        (played / video.duration).toFloat()
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun setSpeed(speed: Float) {
    }

    override fun seekTo(duration: Duration) {
        this.currentPosition.value = duration
    }
}