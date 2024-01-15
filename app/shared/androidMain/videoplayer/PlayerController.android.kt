@file:OptIn(UnstableApi::class)

package me.him188.ani.app.videoplayer

import androidx.annotation.OptIn
import androidx.annotation.UiThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.him188.ani.app.platform.Context
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.utils.logging.info
import org.koin.core.component.KoinComponent

@Composable
actual fun rememberPlayerController(videoSource: Flow<VideoSource<*>?>): PlayerController {
    val context = LocalContext.current
    return remember {
        ExoPlayerController(videoSource, context)
    }
}

/**
 * Must be remembered
 */
internal class ExoPlayerController @UiThread constructor(
    videoFlow: Flow<VideoSource<*>?>,
    context: Context,
) : PlayerController, AbstractViewModel(), KoinComponent {
    private val mediaSourceFactory = videoFlow.filterNotNull().flatMapLatest { video ->
        when (video) {
            is TorrentVideoSource -> video.startStreaming().map {
                ProgressiveMediaSource.Factory { TorrentDataSource(it) }
            }

            else -> error("Unsupported video type: ${video::class}")
        }
    }

    init {
        logger.info { "ExoPlayerController created" }
    }

    val player = run {
        ExoPlayer.Builder(context).apply {}.build().apply {
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onIsLoadingChanged(isLoading: Boolean) {
                    if (isLoading) {
                        state.value = PlayerState.PAUSED_BUFFERING
                    } else {
                        if (state.value == PlayerState.PAUSED_BUFFERING) {
                            state.value = PlayerState.PLAYING
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    logger.warn("ExoPlayer error: ${error.errorCodeName}")
                }
            })
        }
    }

    init {
        videoFlow.map {
            logger.info { "Video source changed: $it" }
        }.launchIn(backgroundScope)

        videoFlow.combine(mediaSourceFactory) { source, factory ->
            if (source == null) {
                logger.info { "Cleaning up player since source is null" }
                withContext(Dispatchers.Main.immediate) {
                    player.stop()
                    player.clearMediaItems()
                }
                return@combine
            }
            logger.info { "Initializing player with media: $source" }
            val item = factory.createMediaSource(MediaItem.fromUri(source.uri))
            withContext(Dispatchers.Main.immediate) {
                player.setMediaSource(item)
                player.prepare()
                player.play()
            }
            logger.info { "Player initialized" }
        }.launchIn(backgroundScope)
    }


//    private val headersAvailable: Flow<Boolean> = video.flatMapLatest { it.headersAvailable }.shareInBackground()

//    init {
//        video
//            .map { it.file.toURI().toString() }
//            .distinctUntilChanged()
//            .combine(headersAvailable) { uri, headersAvailable ->
//                if (!headersAvailable) {
//                    return@combine
//                } // 等有足够的缓冲再让 Exo 加载视频头
//
//                withContext(Dispatchers.Main.immediate) {
//                    if (player.mediaItemCount != 0) {
//                        return@withContext // already loaded
//                    }
//                    println("加载视频: $uri")
//                    val factory = ProgressiveMediaSource.Factory(TorrentDataSource.Factory(torrentDownloader))
//                    player.setMediaSource(
//                        factory.createMediaSource(MediaItem.fromUri(uri))
//                    )
//                    player.prepare()
//                }
//            }
//            .launchIn(backgroundScope)
//    }

    override val state: MutableStateFlow<PlayerState> = MutableStateFlow(PlayerState.PAUSED_BUFFERING)
    override val isBuffering: Flow<Boolean> by lazy {
        state.map { it == PlayerState.PAUSED_BUFFERING }
    }

    override val playedDuration: Flow<Int> = MutableStateFlow(0)
    override val playProgress: Flow<Float> = MutableStateFlow(0f) // TODO:   
//        playedDuration.combine(video.flatMapLatest { it.length }) { duration, length ->
//            duration.toFloat() / length.toFloat()
//        }


    override fun pause() {
        player.pause()
    }

    override fun resume() {
        player.play()
    }

    override fun onAbandoned() {
        super.onAbandoned()
        player.release()
    }

    override fun onForgotten() {
        super.onForgotten()
        player.release()
    }

    override fun onRemembered() {
        super.onRemembered()
        player
    }
}
