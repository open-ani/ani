package me.him188.ani.app.videoplayer

import androidx.annotation.MainThread
import androidx.annotation.OptIn
import androidx.annotation.UiThread
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.app.platform.Context
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.videoplayer.data.VideoData
import me.him188.ani.app.videoplayer.data.VideoProperties
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.media.VideoDataDataSource
import me.him188.ani.app.videoplayer.ui.state.AbstractPlayerState
import me.him188.ani.app.videoplayer.ui.state.PlaybackState
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import me.him188.ani.app.videoplayer.ui.state.PlayerStateFactory
import me.him188.ani.utils.logging.error
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
) : AbstractPlayerState<ExoPlayerState.ExoPlayerData>(parentCoroutineContext),
    AutoCloseable {
    class ExoPlayerData(
        videoSource: VideoSource<*>,
        videoData: VideoData,
        releaseResource: () -> Unit,
        val factory: ProgressiveMediaSource.Factory,
    ) : Data(videoSource, videoData, releaseResource)

    override suspend fun startPlayer(data: ExoPlayerData) {
        val item =
            data.factory.createMediaSource(MediaItem.fromUri(data.videoSource.uri))
        withContext(Dispatchers.Main.immediate) {
            player.setMediaSource(item)
            player.prepare()
            player.play()
        }
    }

    override suspend fun cleanupPlayer() {
        withContext(Dispatchers.Main.immediate) {
            player.stop()
            player.clearMediaItems()
        }
    }

    override suspend fun openSource(source: VideoSource<*>): ExoPlayerData {
        val data = source.open()
        val file = data.createInput()
        return ExoPlayerData(
            source,
            data,
            releaseResource = {
                file.close()
                data.close()
            },
            factory = ProgressiveMediaSource.Factory {
                VideoDataDataSource(
                    data, file
                )
            }
        )
    }

    private val updateVideoPropertiesTasker = MonoTasker(backgroundScope)

    val player = kotlin.run {
        ExoPlayer.Builder(context).apply {}.build().apply {
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    state.value = PlaybackState.ERROR
                    logger.warn("ExoPlayer error: ${error.errorCodeName}")
                }

                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    super.onVideoSizeChanged(videoSize)
                    updateVideoProperties()
                }

                @MainThread
                private fun updateVideoProperties(): Boolean {
                    val video = videoFormat ?: return false
                    val audio = audioFormat ?: return false
                    val data = openResource.value?.videoData ?: return false
                    val title = mediaMetadata.title
                    val duration = duration

                    // 注意, 要把所有 UI 属性全都读出来然后 captured 到 background -- ExoPlayer 所有属性都需要在主线程

                    updateVideoPropertiesTasker.launch(Dispatchers.IO) {
                        // This is in background
                        videoProperties.value = VideoProperties(
                            title = title?.toString(),
                            heightPx = video.height,
                            widthPx = video.width,
                            videoBitrate = video.bitrate,
                            audioBitrate = audio.bitrate,
                            frameRate = video.frameRate,
                            durationMillis = duration,
                            fileLengthBytes = data.fileLength,
                            fileHash = data.computeHash(),
                            filename = data.filename,
                        )
                    }
                    return true
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            state.value = PlaybackState.PAUSED_BUFFERING
                            isBuffering.value = true
                        }

                        Player.STATE_ENDED -> {
                            state.value = PlaybackState.FINISHED
                            isBuffering.value = false
                        }

                        Player.STATE_IDLE -> {
                            state.value = PlaybackState.READY
                            isBuffering.value = false
                        }

                        Player.STATE_READY -> {
                            state.value = PlaybackState.READY
                            isBuffering.value = false
                        }
                    }
                    updateVideoProperties()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        state.value = PlaybackState.PLAYING
                        isBuffering.value = false
                    } else {
                        state.value = PlaybackState.PAUSED
                    }
                }

                override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                    super.onPlaybackParametersChanged(playbackParameters)
                    playbackSpeed.value = playbackParameters.speed
                }
            })
        }
    }

    override val isBuffering: MutableStateFlow<Boolean> = MutableStateFlow(false) // 需要单独状态, 因为要用户可能会覆盖 [state] 

    override val videoProperties = MutableStateFlow<VideoProperties?>(null)
    override val bufferedPercentage = MutableStateFlow(0)

    override fun seekTo(positionMillis: Long) {
        player.seekTo(positionMillis)
    }

    override val currentPositionMillis: MutableStateFlow<Long> = MutableStateFlow(0)
    override fun getExactCurrentPositionMillis(): Long = player.currentPosition

    init {
        backgroundScope.launch(Dispatchers.Main) {
            while (currentCoroutineContext().isActive) {
                currentPositionMillis.value = player.currentPosition
                bufferedPercentage.value = player.bufferedPercentage
                delay(0.1.seconds) // 100 fps
            }
        }
    }

    override fun pause() {
        player.playWhenReady = false
        player.pause()
    }

    override fun resume() {
        player.playWhenReady = true
        player.play()
    }

    override val playbackSpeed: MutableStateFlow<Float> = MutableStateFlow(1f)

    override fun closeImpl() {
        @kotlin.OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Main) {
            try {
                player.stop()
                player.release()
                logger.info("ExoPlayer $player released")
            } catch (e: Throwable) {
                logger.error(e) { "Failed to release ExoPlayer $player, ignoring" }
            }
        }
    }

    override fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }
}
