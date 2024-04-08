package me.him188.ani.app.videoplayer.ui.state

import androidx.annotation.UiThread
import androidx.compose.runtime.Stable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import me.him188.ani.app.platform.Context
import me.him188.ani.app.videoplayer.data.VideoProperties
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.ui.VideoPlayer
import kotlin.coroutines.CoroutineContext

/**
 * A controller for the [VideoPlayer].
 */
@Stable
interface PlayerState {
    /**
     * Current state of the player.
     *
     * State can be changed internally e.g. buffer exhausted or externally by e.g. [pause], [resume].
     */
    val state: StateFlow<PlaybackState>

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
    val currentPositionMillis: StateFlow<Long>

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

    val playbackSpeed: StateFlow<Float>

    @UiThread
    fun setPlaybackSpeed(speed: Float)

    /**
     * 跳转到指定位置
     */
    @UiThread
    fun seekTo(positionMillis: Long)
}

class UnsupportedVideoSourceException(
    val source: VideoSource<*>,
    player: PlayerState,
) : RuntimeException("Video source is not supported by player '${player}': $source")

fun PlayerState.togglePause() {
    if (state.value.isPlaying) {
        pause()
    } else {
        resume()
    }
}

abstract class AbstractPlayerState : PlayerState {
    override val isBuffering: Flow<Boolean> by lazy {
        state.map { it == PlaybackState.PAUSED_BUFFERING }
    }
}


enum class PlaybackState(
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

fun interface PlayerStateFactory {
    /**
     * Creates a new [PlayerState]
     * [parentCoroutineContext] must have a [Job] so that the player state is bound to the parent coroutine context scope.
     *
     * @param context the platform context to create the underlying player implementation.
     * It is only used by the constructor and not stored.
     */
    fun create(context: Context, parentCoroutineContext: CoroutineContext): PlayerState
}

/**
 * For previewing
 */
class DummyPlayerState : AbstractPlayerState() {
    override val state: MutableStateFlow<PlaybackState> = MutableStateFlow(PlaybackState.PAUSED_BUFFERING)
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
            durationMillis = 100_000,
            fileLengthBytes = 100_000_000
        )
    )
    override val isBuffering: Flow<Boolean> = MutableStateFlow(true)
    override val currentPositionMillis = MutableStateFlow(10_000L)
    override val bufferedPercentage: StateFlow<Int> = MutableStateFlow(50)
    override val playProgress: Flow<Float> = currentPositionMillis.combine(videoProperties) { played, video ->
        (played / video.durationMillis).toFloat()
    }

    override fun pause() {
        state.value = PlaybackState.PAUSED
    }

    override fun resume() {
        state.value = PlaybackState.PLAYING
    }

    override val playbackSpeed: MutableStateFlow<Float> = MutableStateFlow(1.0f)

    override fun setPlaybackSpeed(speed: Float) {
        playbackSpeed.value = speed
    }

    override fun seekTo(positionMillis: Long) {
        this.currentPositionMillis.value = positionMillis
    }
}