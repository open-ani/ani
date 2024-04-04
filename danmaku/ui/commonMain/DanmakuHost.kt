package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import me.him188.ani.danmaku.api.Danmaku

interface DanmakuHostState {
    @Stable
    val tracks: List<DanmakuTrackState>

    /**
     * Sends the [danmaku] to the first track that is currently ready to receive it.
     *
     * @return `true` if the [danmaku] was sent to a track, `false` if all tracks are currently occupied.
     */
    fun trySend(danmaku: Danmaku): Boolean

    val isPaused: Boolean

    /**
     * Pauses the movement of danmaku.
     */
    @UiThread
    fun pause()

    /**
     * Resumes the movement of danmaku. Danmaku will continue to move from where it was paused.
     */
    @UiThread
    fun resume()
}

/**
 * Creates a [DanmakuHostState].
 */
fun DanmakuHostState(
    danmakuProperties: DanmakuProperties = DanmakuProperties.Default,
): DanmakuHostState = DanmakuHostStateImpl(danmakuProperties)

/**
 * 容纳[弹幕轨道][DanmakuTrack]的 [Column].
 *
 * @see DanmakuHostState
 */
@Composable
fun DanmakuHost(
    state: DanmakuHostState,
    modifier: Modifier = Modifier,
    config: DanmakuConfig = DanmakuConfig.Default,
) {
    Column(modifier.background(Color.Transparent)) {
        for (track in state.tracks) {
            DanmakuTrack(track, Modifier.fillMaxWidth(), config) {
                // fix height even if there is no danmaku showing
                // so that the second track will not move to the top of the screen when the first track is empty.
                danmaku(DummyDanmakuState, Modifier.alpha(0f), style = config.style)

                for (danmaku in track.visibleDanmaku) {
                    key(danmaku.danmaku.id) {
                        danmaku(danmaku, style = config.style)
                    }
                }
            }
        }
    }
}

internal class DanmakuHostStateImpl(
    danmakuProperties: DanmakuProperties,
) : DanmakuHostState {
    private val _isPaused = mutableStateOf(false)

    override val tracks: List<DanmakuTrackState> = listOf(
        DanmakuTrackState(_isPaused, 10, danmakuProperties),
        DanmakuTrackState(_isPaused, 10, danmakuProperties),
        DanmakuTrackState(_isPaused, 10, danmakuProperties),
        DanmakuTrackState(_isPaused, 10, danmakuProperties),
        DanmakuTrackState(_isPaused, 10, danmakuProperties)
    )

    override fun trySend(danmaku: Danmaku): Boolean {
        return tracks.any { it.trySend(danmaku) }
    }

    override var isPaused: Boolean by _isPaused

    override fun pause() {
        isPaused = true
    }

    override fun resume() {
        isPaused = false
    }
}


@Composable
internal expect fun PreviewDanmakuHost()
