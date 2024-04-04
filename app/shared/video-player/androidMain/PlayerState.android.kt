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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.app.platform.Context
import me.him188.ani.app.videoplayer.media.TorrentDataSource
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds


class ExoPlayerStateFactory : PlayerStateFactory {
    @OptIn(UnstableApi::class)
    override fun create(context: Context, parentCoroutineContext: CoroutineContext): PlayerState =
        ExoPlayerState(context, parentCoroutineContext)
}


@OptIn(UnstableApi::class)
internal class ExoPlayerState @UiThread constructor(
    context: Context,
    parentCoroutineContext: CoroutineContext
) : AbstractPlayerState(),
    AutoCloseable {
    private val backgroundScope = CoroutineScope(
        parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job])
    ).apply {
        coroutineContext.job.invokeOnCompletion {
            close()
        }
    }

    override val videoSource: MutableStateFlow<VideoSource<*>?> = MutableStateFlow(null)

    private class OpenedVideoSource(
        val videoSource: VideoSource<*>,
        val releaseResource: () -> Unit,
        val mediaSourceFactory: ProgressiveMediaSource.Factory,
    )

    /**
     * Currently playing resource that should be closed when the controller is closed.
     * @see setVideoSource
     */
    private val openResource = MutableStateFlow<OpenedVideoSource?>(null)

    override suspend fun setVideoSource(source: VideoSource<*>?) {
        if (source == null) {
            logger.info { "setVideoSource: Cleaning up player since source is null" }
            withContext(Dispatchers.Main.immediate) {
                player.stop()
                player.clearMediaItems()
            }
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

        val opened = openSource(source)

        try {
            logger.info { "Initializing player with VideoSource: $source" }
            val item = opened.mediaSourceFactory.createMediaSource(MediaItem.fromUri(source.uri))
            withContext(Dispatchers.Main.immediate) {
                player.setMediaSource(item)
                player.prepare()
                player.play()
            }
            logger.info { "Player initialized" }
        } catch (e: Throwable) {
            opened.releaseResource()
            throw e
        }

        this.openResource.value = opened
    }

    private suspend fun openSource(source: VideoSource<*>): OpenedVideoSource {
        when (source) {
            is TorrentVideoSource -> {
                val session = source.open()
                ProgressiveMediaSource.Factory { TorrentDataSource(session) }
                return OpenedVideoSource(
                    source,
                    releaseResource = {
                        session.close()
                    },
                    mediaSourceFactory = ProgressiveMediaSource.Factory { TorrentDataSource(session) }
                )
            }

            else -> throw UnsupportedOperationException("Unsupported video type: ${source::class}")
        }
    }

    val player = kotlin.run {
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

                private fun updateVideoProperties(): Boolean {
                    val video = videoFormat ?: return false
                    val audio = audioFormat ?: return false
                    videoProperties.value = VideoProperties(
                        title = mediaMetadata.title?.toString(),
                        heightPx = video.height,
                        widthPx = video.width,
                        videoBitrate = video.bitrate,
                        audioBitrate = audio.bitrate,
                        frameRate = video.frameRate,
                        durationMillis = duration,
                    )
                    return true
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    updateVideoProperties()
                    when (playbackState) {
                        Player.STATE_BUFFERING -> state.value = PlaybackState.PAUSED_BUFFERING
                        Player.STATE_ENDED -> state.value = PlaybackState.FINISHED
                        Player.STATE_IDLE -> state.value = PlaybackState.READY
                        Player.STATE_READY -> state.value = PlaybackState.READY
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        state.value = PlaybackState.PLAYING
                    } else {
                        state.value = PlaybackState.PAUSED
                    }
                }
            })
        }
    }

    override val state: MutableStateFlow<PlaybackState> = MutableStateFlow(PlaybackState.PAUSED_BUFFERING)

    override val videoProperties = MutableStateFlow<VideoProperties?>(null)
    override val bufferedPercentage = MutableStateFlow(0)
    override val isBuffering: Flow<Boolean> = state.map { it == PlaybackState.PAUSED_BUFFERING }

    override fun seekTo(positionMillis: Long) {
        player.seekTo(positionMillis)
    }

    override val currentPositionMillis: MutableStateFlow<Long> = MutableStateFlow(0)
    override val playProgress: Flow<Float> =
        combine(videoProperties.filterNotNull(), currentPositionMillis) { properties, duration ->
            if (properties.durationMillis == 0L) {
                return@combine 0f
            }
            (duration / properties.durationMillis).toFloat().coerceIn(0f, 1f)
        }

    init {
        backgroundScope.launch(Dispatchers.Main) {
            while (currentCoroutineContext().isActive) {
                currentPositionMillis.value = player.currentPosition
                bufferedPercentage.value = player.bufferedPercentage
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


    @Volatile
    private var closed = false

    @Synchronized
    override fun close() {
        if (closed) return
        closed = true
        player.stop()
        player.release()
        openResource.value?.releaseResource?.invoke()
        backgroundScope.cancel()
    }

    override fun setSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    private companion object {
        val logger = logger(ExoPlayerState::class)
    }
}
