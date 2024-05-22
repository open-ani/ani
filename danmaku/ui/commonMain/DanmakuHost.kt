package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuPresentation

interface DanmakuHostState {
    @Stable
    val tracks: List<DanmakuTrackState>

    /**
     * Sends the [danmaku] to the first track that is currently ready to receive it.
     *
     * @return `true` if the [danmaku] was sent to a track, `false` if all tracks are currently occupied.
     */
    fun trySend(danmaku: DanmakuPresentation): Boolean

    /**
     * 清空所有弹幕轨道并重新填充
     */
    suspend fun repopulate(list: List<DanmakuPresentation>, style: DanmakuStyle)

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

    @Composable
    fun Content(baseStyle: TextStyle)
}

suspend inline fun DanmakuHostState.send(danmaku: DanmakuPresentation) {
    if (!trySend(danmaku)) {
        tracks.randomOrNull()?.send(danmaku)
    }
}

/**
 * Creates a [DanmakuHostState].
 */
fun DanmakuHostState(
    danmakuTrackProperties: DanmakuTrackProperties = DanmakuTrackProperties.Default,
): DanmakuHostState = DanmakuHostStateImpl(danmakuTrackProperties)

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
        val baseStyle = MaterialTheme.typography.bodyMedium
        state.Content(baseStyle)
        for (track in state.tracks) {
            DanmakuTrack(track, Modifier.fillMaxWidth(), config, baseStyle = baseStyle) {
                // fix height even if there is no danmaku showing
                // so that the second track will not move to the top of the screen when the first track is empty.
                danmaku(DummyDanmakuState, Modifier.alpha(0f).padding(vertical = 1.dp), style = config.style)

                for (danmaku in track.visibleDanmaku) {
                    key(danmaku.presentation.id) {
                        danmaku(danmaku, style = config.style)
                    }
                }
            }
        }
    }
}

internal class DanmakuHostStateImpl(
    danmakuTrackProperties: DanmakuTrackProperties,
) : DanmakuHostState {
    private val _isPaused = mutableStateOf(false)

    override val tracks: List<DanmakuTrackState> = listOf(
        DanmakuTrackState(_isPaused, 10, danmakuTrackProperties),
        DanmakuTrackState(_isPaused, 10, danmakuTrackProperties),
        DanmakuTrackState(_isPaused, 10, danmakuTrackProperties),
        DanmakuTrackState(_isPaused, 10, danmakuTrackProperties),
        DanmakuTrackState(_isPaused, 10, danmakuTrackProperties)
    )

    override fun trySend(danmaku: DanmakuPresentation): Boolean {
        return tracks.any { it.trySend(danmaku) }
    }

    override suspend fun repopulate(list: List<DanmakuPresentation>, style: DanmakuStyle) {
        withContext(Dispatchers.Main.immediate) { // immediate: do not pay for dispatch
            // 还没 layout 之前等着
            while (!::textMeasurer.isInitialized
                || !::baseStyle.isInitialized
                || tracks.fastAny { it.trackOffset.isNaN() }
            ) {
                delay(100)
            }
            runPopulate(list, style)
        }
    }

    // 不能 suspend, 否则 `track.populationVersion++` 可能无效
    private fun runPopulate(
        list: List<DanmakuPresentation>,
        style: DanmakuStyle
    ) {
        val textMeasurer = textMeasurer

        // 重置所有轨道以及它们的偏移
        for (track in tracks) {
            track.clear()
            track.trackOffset = 0f
            track.populationVersion++
        }

        if (list.isEmpty()) return // fast path

        class Track(
            val state: DanmakuTrackState,
        ) {
            var lastSent: Danmaku? = null

            /**
             * 是否有弹幕还没有完全显示
             */
            fun hasStartingDanmaku(): Boolean {
                if (state.startingDanmaku.isEmpty()) {
                    return false
                }
                return state.startingDanmaku.fastAny {
                    !state.isFullyVisible(it)
                }
            }
        }

        val tracks = tracks.map { Track(it) }
        var curTrack = 0

        fun useNextTrackOrNull(): Track? {
            for (i in curTrack until (curTrack + tracks.size)) {
                val track = tracks[i % tracks.size]
                if (!track.hasStartingDanmaku()) {
                    curTrack = (i + 1) % tracks.size
                    return track
                }
            }
            return null
        }

        val danmakuStyle = baseStyle.merge(style.styleForText())


        var lastSent: Danmaku? = null
        for (danmaku in list) { // 时间由旧到新
            // 调整到正确时间间隔 (模拟出两条弹幕之间的间隔)
            for (track in tracks) {
                //                    val lastSent = track.lastSent ?: continue // 轨道还未发送过弹幕, 不需要模拟运动
                lastSent ?: continue
                if (track.state.lastBaseSpeed.isNaN()) continue
                check(danmaku.danmaku.playTimeMillis >= lastSent.playTimeMillis) {
                    "danmaku list must be sorted by playTime"
                }
                val off =
                    (danmaku.danmaku.playTimeMillis - lastSent.playTimeMillis) / 1000f * track.state.lastBaseSpeed
                check(off >= 0f)
                track.state.trackOffset -= off //+ width + track.state.lastSafeSeparation
            }
            lastSent = danmaku.danmaku

            val track = useNextTrackOrNull() ?: continue // 所有轨道都有弹幕还未完全显示, 也就是都不能发弹幕, 跳过

            //                track.state.trackOffset -= width + track.state.lastSafeSeparation
            // 先把轨道往左移动, 留出足够放得下这个弹幕的空间, 然后放置
            track.state.place(danmaku).apply {
                textWidth = textMeasurer.measure(
                    danmaku.danmaku.text,
                    overflow = TextOverflow.Visible,
                    style = danmakuStyle,
                    softWrap = false,
                    maxLines = 1
                ).size.width
            }
            track.lastSent = danmaku.danmaku
        }
    }

    override var isPaused: Boolean by _isPaused

    override fun pause() {
        isPaused = true
    }

    override fun resume() {
        isPaused = false
    }

    @Volatile
    private lateinit var textMeasurer: TextMeasurer

    @Volatile
    private lateinit var baseStyle: TextStyle

    @Composable
    override fun Content(baseStyle: TextStyle) {
        SideEffect {
            this.baseStyle = baseStyle
        }
        textMeasurer = rememberTextMeasurer(cacheSize = 0)
    }
}


@Composable
internal expect fun PreviewDanmakuHost()
