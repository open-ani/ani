@file:OptIn(UnstableApi::class)

package me.him188.ani.app.videoplayer

import androidx.annotation.OptIn
import androidx.annotation.UiThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.FileDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import me.him188.ani.app.platform.Context
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.torrent.PieceState
import me.him188.ani.app.ui.foundation.AbstractViewModel
import kotlin.time.Duration.Companion.seconds

@Composable
actual fun rememberPlayerController(video: Video?): PlayerController {
    if (video == null) {
        return PlayerController.AlwaysBuffering
    }
    val context = LocalContext.current
    return remember {
        ExoPlayerController(snapshotFlow { video }, context)
    }
}

internal class ExoPlayerController @UiThread constructor(
    video: Flow<Video>,
    context: Context,
) : PlayerController, AbstractViewModel() {
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

    /**
     * 是否有足够的缓冲来播放视频
     */
    val hasEnoughBuffer: Flow<Boolean> = video.transformLatest {
        while (currentCoroutineContext().isActive) {
            val pieces = it.torrentSource!!.pieces.value

            val firstNotFinished = pieces.indexOfFirst { it != PieceState.FINISHED }
            if (firstNotFinished == -1) {
                println("全部下载完成")
                emit(true)
                return@transformLatest
            }
            if (pieces.asSequence().take(16).all { it == PieceState.FINISHED }
                && pieces.reversed().asSequence().take(16).all { it == PieceState.FINISHED }
            ) {
                println("First and last pieces downloaded")
                emit(true)
            }
            delay(1.seconds)
        }
    }.shareInBackground()

    init {
        video
            .map { it.file.toURI().toString() }
            .distinctUntilChanged()
            .combine(hasEnoughBuffer) { uri, hasEnoughBuffer ->
//                .map { uri ->
                if (!hasEnoughBuffer) {
                    println("没有 buffer")
                    return@combine
                } // 等有足够的缓冲再让 Exo 加载视频头
                withContext(Dispatchers.Main.immediate) {
                    if (player.mediaItemCount != 0) {
                        return@withContext
                    }
                    println("加载视频: $uri")
                    val factory = ProgressiveMediaSource.Factory(FileDataSource.Factory())
                    player.setMediaSource(
                        factory.createMediaSource(MediaItem.fromUri(uri))
                    )
                    player.prepare()
                }
            }
            .launchIn(backgroundScope)
    }

    override val state: MutableStateFlow<PlayerState> = MutableStateFlow(PlayerState.PAUSED_BUFFERING)
    override val isBuffering: Flow<Boolean> by lazy {
        state.map { it == PlayerState.PAUSED_BUFFERING }
    }

    override val playedDuration: Flow<Int> = MutableStateFlow(0)
    override val playProgress: Flow<Float> =
        playedDuration.combine(video.flatMapLatest { it.length }) { duration, length ->
            duration.toFloat() / length.toFloat()
        }


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
