@file:OptIn(UnstableApi::class)

package me.him188.ani.app.videoplayer

import androidx.annotation.OptIn
import androidx.annotation.UiThread
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.him188.ani.app.platform.Context
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.videoplayer.media.TorrentDataSource
import me.him188.ani.utils.logging.info
import org.koin.core.component.KoinComponent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


class ExoPlayerControllerFactory : PlayerControllerFactory {
    override fun create(context: Context, videoSource: Flow<VideoSource<*>?>): PlayerController {
        return ExoPlayerController(videoSource, context)
    }
}


/**
 * Must be remembered
 */
internal class ExoPlayerController @UiThread constructor(
    videoFlow: Flow<VideoSource<*>?>,
    context: Context,
) : PlayerController, AbstractViewModel(), KoinComponent {

    private var playingResource: AutoCloseable? = null
    private val mediaSourceFactory = videoFlow.filterNotNull()
        .flatMapLatest { video ->
            when (video) {
                is TorrentVideoSource -> video.startStreaming().map {
                    playingResource = it
                    ProgressiveMediaSource.Factory { TorrentDataSource(it) }
                }

                else -> error("Unsupported video type: ${video::class}")
            }
        }

    init {
        logger.info { "ExoPlayerController created" }

        videoFlow.combine(mediaSourceFactory) { source, factory ->
            if (source == null) {
                logger.info { "Cleaning up player since source is null" }
                withContext(Dispatchers.Main.immediate) {
                    player.stop()
                    player.clearMediaItems()
                }
                return@combine
            }
            logger.info { "Initializing player with VideoSource: $source" }
            val item = factory.createMediaSource(MediaItem.fromUri(source.uri))
            withContext(Dispatchers.Main.immediate) {
                player.setMediaSource(item)
                player.prepare()
                player.play()
            }
            logger.info { "Player initialized" }
        }.launchIn(backgroundScope)
    }

    val player = run {
        ExoPlayer.Builder(context).apply {}.build().apply {
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    logger.warn("ExoPlayer error: ${error.errorCodeName}")
                }

                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    super.onVideoSizeChanged(videoSize)
                    updateVideoProperties()
                }

                private fun updateVideoProperties() {
                    val video = videoFormat!!
                    val audio = audioFormat!!
                    videoProperties.value = VideoProperties(
                        title = mediaMetadata.title?.toString(),
                        heightPx = video.height,
                        widthPx = video.width,
                        videoBitrate = video.bitrate,
                        audioBitrate = audio.bitrate,
                        frameRate = video.frameRate,
                        duration = duration.milliseconds,
                    )
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> state.value = PlayerState.PAUSED_BUFFERING
                        Player.STATE_ENDED -> state.value = PlayerState.FINISHED
                        Player.STATE_IDLE -> state.value = PlayerState.READY
                        Player.STATE_READY -> state.value = PlayerState.READY
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        state.value = PlayerState.PLAYING
                    } else {
                        state.value = PlayerState.PAUSED
                    }
                }
            })
        }
    }

    override val state: MutableStateFlow<PlayerState> = MutableStateFlow(PlayerState.PAUSED_BUFFERING)
    override val videoProperties: MutableStateFlow<VideoProperties> = MutableStateFlow(VideoProperties.EMPTY)
    override val bufferProgress: MutableStateFlow<Float> = MutableStateFlow(0f)
    override val isBuffering: Flow<Boolean> by lazy {
        state.map { it == PlayerState.PAUSED_BUFFERING }
    }

    override val playedDuration: MutableStateFlow<Duration> = MutableStateFlow(0.milliseconds)
    override val playProgress: StateFlow<Float> =
        combine(videoProperties, playedDuration) { properties, duration ->
            if (properties.duration == 0.milliseconds) {
                return@combine 0f
            }
            (duration / properties.duration).toFloat()
        }.filterNotNull().stateInBackground(0f)

    init {
        launchInBackground {
            while (true) {
                withContext(Dispatchers.Main) {
                    playedDuration.value = player.currentPosition.milliseconds
                    bufferProgress.value = player.bufferedPercentage.toFloat()
                }
                delay(1.seconds)
            }
        }
    }

    override fun pause() {
        player.pause()
    }

    override fun resume() {
        player.play()
    }

    override fun onAbandoned() {
        super.onAbandoned()
        close()
    }

    override fun onForgotten() {
        super.onForgotten()
        close()
    }

    override fun close() {
        super.close()
        player.stop()
        player.release()
        playingResource?.close()
    }

    override fun setSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }
}
