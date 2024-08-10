package me.him188.ani.app.videoplayer.ui.state

import androidx.annotation.UiThread
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import me.him188.ani.app.platform.Context
import me.him188.ani.app.tools.torrent.TorrentMediaCacheProgressState
import me.him188.ani.app.ui.foundation.produceState
import me.him188.ani.app.videoplayer.data.VideoData
import me.him188.ani.app.videoplayer.data.VideoProperties
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.data.VideoSourceOpenException
import me.him188.ani.app.videoplayer.torrent.FileVideoData
import me.him188.ani.app.videoplayer.torrent.TorrentVideoData
import me.him188.ani.app.videoplayer.ui.VideoPlayer
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

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
     * The video data of the currently playing video.
     */
    val videoData: Flow<VideoData?>

    /**
     * 视频数据缓存进度
     */
    val cacheProgress: MediaCacheProgressState

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
     * @throws VideoSourceOpenException 当打开失败时抛出, 包含原因
     */
    @Throws(VideoSourceOpenException::class, CancellationException::class)
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

    @UiThread
    fun getExactCurrentPositionMillis(): Long

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
     * 停止播放, 之后不能恢复, 必须 [setVideoSource]
     */
    @UiThread
    fun stop()

    /**
     * 视频播放速度 (倍速)
     *
     * 1.0 为原速度, 2.0 为两倍速度, 0.5 为一半速度, etc.
     */
    val playbackSpeed: StateFlow<Float>

    @UiThread
    fun setPlaybackSpeed(speed: Float)

    /**
     * 跳转到指定位置
     */
    @UiThread
    fun seekTo(positionMillis: Long)

    val subtitleTracks: TrackGroup<SubtitleTrack>

    val audioTracks: TrackGroup<AudioTrack>

    fun saveScreenshotFile(filename: String)

    val chapters: StateFlow<List<Chapter>>
}

@Immutable
data class Chapter(
    val name: String,
    val durationMillis: Long,
    val offsetMillis: Long
)

fun PlayerState.togglePause() {
    if (state.value.isPlaying) {
        pause()
    } else {
        resume()
    }
}

abstract class AbstractPlayerState<D : AbstractPlayerState.Data>(
    parentCoroutineContext: CoroutineContext,
) : PlayerState, SynchronizedObject() {
    protected val backgroundScope = CoroutineScope(
        parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]),
    ).apply {
        coroutineContext.job.invokeOnCompletion {
            close()
        }
    }

    protected val logger = logger(this::class)
    override val videoSource: MutableStateFlow<VideoSource<*>?> = MutableStateFlow(null)

    override val state: MutableStateFlow<PlaybackState> = MutableStateFlow(PlaybackState.PAUSED_BUFFERING)

    /**
     * Currently playing resource that should be closed when the controller is closed.
     * @see setVideoSource
     */
    protected val openResource = MutableStateFlow<D?>(null)

    open class Data(
        open val videoSource: VideoSource<*>,
        open val videoData: VideoData,
        open val releaseResource: () -> Unit,
    )

    override val isBuffering: Flow<Boolean> by lazy {
        state.map { it == PlaybackState.PAUSED_BUFFERING }
    }

    final override val videoData: Flow<VideoData?> = openResource.map {
        it?.videoData
    }

    private val isCacheFinished by videoData.flatMapLatest {
        when (it) {
            null -> flowOf(false)
            is TorrentVideoData -> it.isCacheFinished
            else -> flowOf(false)
        }
    }.produceState(false, backgroundScope)

    private val cacheProgressFlow = videoData.map {
        when (it) {
            null -> staticMediaCacheProgressState(ChunkState.NONE)
            is TorrentVideoData -> TorrentMediaCacheProgressState(
                it.pieces,
                isFinished = { isCacheFinished },
            )

            is FileVideoData -> staticMediaCacheProgressState(ChunkState.DONE)

            else -> staticMediaCacheProgressState(ChunkState.NONE)
        }
    }.stateIn(backgroundScope, SharingStarted.WhileSubscribed(), staticMediaCacheProgressState(ChunkState.NONE))

    init {
        backgroundScope.launch {
            while (true) {
                cacheProgressFlow.value.update()
                delay(1000)
            }
        }
    }

    override val cacheProgress: UpdatableMediaCacheProgressState by
    cacheProgressFlow.produceState(staticMediaCacheProgressState(ChunkState.NONE), backgroundScope)

    final override val playProgress: Flow<Float> by lazy {
        combine(videoProperties.filterNotNull(), currentPositionMillis) { properties, duration ->
            if (properties.durationMillis == 0L) {
                return@combine 0f
            }
            (duration / properties.durationMillis).toFloat().coerceIn(0f, 1f)
        }
    }

    final override suspend fun setVideoSource(source: VideoSource<*>?) {
        if (source == null) {
            logger.info { "setVideoSource: Cleaning up player since source is null" }
            cleanupPlayer()
            this.videoSource.value = null
            this.openResource.value = null
            return
        }

        val previousResource = openResource.value
        if (source == previousResource?.videoSource) {
            return
        }

        openResource.value = null
        previousResource?.releaseResource?.invoke()

        val opened = try {
            openSource(source)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            logger.error(e) { "Failed to open VideoSource: $source" }
            throw e
        }

        try {
            logger.info { "Initializing player with VideoSource: $source" }
            state.value = PlaybackState.PAUSED_BUFFERING
            startPlayer(opened)
            logger.info { "Player is now initialized with media and will play when ready" }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            logger.error(e) { "Player failed to initialize" }
            opened.releaseResource()
            throw e
        }

        this.openResource.value = opened
    }

    fun closeVideoSource() {
        synchronized(this) {
            val value = openResource.value
            openResource.value = null
            value?.releaseResource?.invoke()
        }
    }

    final override fun stop() {
        stopImpl()
        closeVideoSource()
    }

    protected abstract fun stopImpl()

    /**
     * 开始播放
     */
    protected abstract suspend fun startPlayer(data: D)

    /**
     * 停止播放, 因为要释放资源了
     */
    protected abstract suspend fun cleanupPlayer()

    @Throws(VideoSourceOpenException::class, CancellationException::class)
    protected abstract suspend fun openSource(source: VideoSource<*>): D

    @Volatile
    private var closed = false
    fun close() {
        if (closed) return
        synchronized(this) {
            if (closed) return
            closed = true

            closeImpl()
            closeVideoSource()
            backgroundScope.cancel()
        }
    }

    protected abstract fun closeImpl()
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

    ERROR(isPlaying = false),
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
class DummyPlayerState : AbstractPlayerState<AbstractPlayerState.Data>(EmptyCoroutineContext) {
    override val state: MutableStateFlow<PlaybackState> = MutableStateFlow(PlaybackState.PAUSED_BUFFERING)
    override fun stopImpl() {

    }

    override suspend fun cleanupPlayer() {
        // no-op
    }

    override suspend fun openSource(source: VideoSource<*>): Data {
        val data = source.open()
        return Data(
            source,
            data,
            releaseResource = {
                backgroundScope.launch {
                    data.close()
                }
            },
        )
    }

    override fun closeImpl() {
    }

    override suspend fun startPlayer(data: Data) {
        // no-op
    }

    override val videoSource: MutableStateFlow<VideoSource<*>?> = MutableStateFlow(null)

    override val videoProperties: MutableStateFlow<VideoProperties> = MutableStateFlow(
        VideoProperties(
            title = "Test Video",
            heightPx = 1080,
            widthPx = 1920,
            videoBitrate = 100,
            audioBitrate = 100,
            frameRate = 30f,
            durationMillis = 100_000,
            fileLengthBytes = 100_000_000,
            fileHash = null,
            filename = "test.mp4",
        ),
    )
    override val currentPositionMillis = MutableStateFlow(10_000L)
    override fun getExactCurrentPositionMillis(): Long {
        return currentPositionMillis.value
    }

    override val bufferedPercentage: StateFlow<Int> = MutableStateFlow(50)

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

    override val subtitleTracks: TrackGroup<SubtitleTrack> = emptyTrackGroup()
    override val audioTracks: TrackGroup<AudioTrack> = emptyTrackGroup()

    override fun saveScreenshotFile(filename: String) {
    }

    override val chapters: MutableStateFlow<List<Chapter>> = MutableStateFlow(
        listOf(
            Chapter("chapter1", durationMillis = 90_000L, 0L),
            Chapter("chapter2", durationMillis = 5_000L, 90_000L),
        ),
    )
}