package me.him188.ani.app.videoplayer

import androidx.annotation.CallSuper
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
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 播放器控制器. 控制暂停, 播放速度等.
 */
interface PlayerController {
    @Stable
    val state: StateFlow<PlayerState>

    @Stable
    val videoProperties: Flow<VideoProperties>

    /**
     * 是否正在 buffer (暂停视频中)
     */
    @Stable
    val isBuffering: Flow<Boolean>

    /**
     * 当前播放进度
     */
    @Stable
    val playedDuration: StateFlow<Duration>

    @Stable
    val bufferProgress: StateFlow<Float>

    /**
     * 当前播放进度比例 `0..1`
     */
    @Stable
    val playProgress: Flow<Float>

    /**
     * 暂停播放, 直到 [pause]
     */
    fun pause()

    /**
     * 恢复播放
     */
    fun resume()

    fun setSpeed(speed: Float)


    /**
     * 播放进度条的拖动位置 `0..1`. `null` 表示没有拖动.
     */
    @Stable
    val previewingProgress: StateFlow<Float?>

    fun setPreviewingProgress(progress: Float)

    /**
     * 如果当前正在拖动, 则为 [previewingProgress], 否则为 [playedDuration].
     */
    @Stable
    val previewingOrPlayingProgress: Flow<Float>

    /**
     * 跳转到指定位置
     */
    fun seekTo(duration: Duration)

    val subjectId: Flow<Int>
    val episodeId: Flow<Int>

    fun onUpdatePlayerPosition(handler: (position: Long) -> Unit)

    fun updatePlayingInfo(subjectId: Int, episodeId: Int)

    fun updatePlayerPosition(position: Long)
}

abstract class AbstractPlayerController : PlayerController, AbstractViewModel() {
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
//        isPreviewing.transform {
//            coroutineScope {
//                launch {
//                    kotlinx.coroutines.delay(2.seconds)
//                    isPreviewing.value = false
//                }
//                previewingProgress
//                emit()
//            }
//        }
//        combine(
//            previewingProgress,
//            playProgress,
//        ) { previewing, playing ->
//            previewing ?: playing
//        }
    }

    private val seekToDebouncer: MutableStateFlow<Duration> = MutableStateFlow(0.seconds)

    init {
        launchInBackground {
            seekToDebouncer.debounce(0.1.seconds).collect {
                isPreviewing.tryEmit(false)
                onSeekTo(it)
            }
        }
    }

    abstract suspend fun onSeekTo(duration: Duration)

    @CallSuper
    final override fun seekTo(duration: Duration) {
        seekToDebouncer.value = duration
    }

    override var subjectId = MutableStateFlow(0)
    override var episodeId = MutableStateFlow(0)

    override fun updatePlayingInfo(subjectId: Int, episodeId: Int) {
        this.subjectId.value = subjectId
        this.episodeId.value = episodeId
    }

    var handler: (position: Long) -> Unit = {}
        protected set

    override fun onUpdatePlayerPosition(handler: (position: Long) -> Unit) {
        this.handler = handler
    }

    override fun updatePlayerPosition(position: Long) {
        handler.invoke(position)
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
    fun create(context: Context, videoSource: Flow<VideoSource<*>?>): PlayerController
}

/**
 * For previewing
 */
class DummyPlayerController : AbstractPlayerController() {
    override val state: StateFlow<PlayerState> = MutableStateFlow(PlayerState.PAUSED_BUFFERING)
    override val videoProperties: Flow<VideoProperties> = MutableStateFlow(
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
    override val playedDuration: MutableStateFlow<Duration> = MutableStateFlow(10.seconds)
    override val bufferProgress: StateFlow<Float> = MutableStateFlow(0.5f)
    override val playProgress: Flow<Float> = playedDuration.combine(videoProperties) { played, video ->
        (played / video.duration).toFloat()
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun setSpeed(speed: Float) {
    }

    override suspend fun onSeekTo(duration: Duration) {
        this.playedDuration.value = duration
    }
}