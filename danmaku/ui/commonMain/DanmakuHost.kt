package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import kotlin.concurrent.Volatile
import kotlin.math.roundToInt

@Stable
interface DanmakuHostState {
    val floatingTracks: List<FloatingDanmakuTrackState>
    val topTracks: List<FixedDanmakuTrackState>
    val bottomTracks: List<FixedDanmakuTrackState>

    fun setTrackCount(count: Int)

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

/**
 * 发送弹幕, 挂起直到发送成功
 */
suspend inline fun DanmakuHostState.send(danmaku: DanmakuPresentation) {
    if (!trySend(danmaku)) {
        this.getTracks(danmaku).randomOrNull()?.send(danmaku)
    }
}

@PublishedApi
internal fun DanmakuHostState.getTracks(danmaku: DanmakuPresentation) =
    when (danmaku.danmaku.location) {
        DanmakuLocation.TOP -> topTracks
        DanmakuLocation.BOTTOM -> bottomTracks.asReversed()
        DanmakuLocation.NORMAL -> floatingTracks
    }

/**
 * Creates a [DanmakuHostState].
 */
fun DanmakuHostState(
    danmakuTrackProperties: DanmakuTrackProperties = DanmakuTrackProperties.Default,
): DanmakuHostState = DanmakuHostStateImpl(danmakuTrackProperties)

/**
 * 容纳[弹幕轨道][FloatingDanmakuTrack]的 [Column].
 *
 * @see DanmakuHostState
 */
@Composable
fun DanmakuHost(
    state: DanmakuHostState,
    configProvider: () -> DanmakuConfig,
    modifier: Modifier = Modifier,
    frozen: Boolean = false,
) {
    BoxWithConstraints(modifier) {
        val baseStyle = MaterialTheme.typography.bodyMedium
        state.Content(baseStyle)
        val verticalPadding = 1.dp // to both top and bottom

        val config by remember {
            derivedStateOf(configProvider)
        }

        val screenHeightPx by rememberUpdatedState(constraints.maxHeight)

        // 顶部和底部
        Column(Modifier.matchParentSize().background(Color.Transparent)) {
            if (config.enableTop) {
                for (track in state.topTracks) {
                    FixedDanmakuTrack(
                        track,
                        Modifier.fillMaxWidth(),
                        configProvider,
                        baseStyle = baseStyle,
                        frozen = frozen,
                    ) {
                        HeightHolder(verticalPadding)
                        track.visibleDanmaku?.let {
                            danmaku(it)
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            if (config.enableBottom) {
                for (track in state.bottomTracks) {
                    FixedDanmakuTrack(
                        track,
                        Modifier.fillMaxWidth(),
                        configProvider,
                        baseStyle = baseStyle,
                        frozen = frozen,
                    ) {
                        HeightHolder(verticalPadding)
                        track.visibleDanmaku?.let {
                            danmaku(it)
                        }
                    }
                }
            }
        }

        // 浮动弹幕
        Column(Modifier.background(Color.Transparent)) {
            if (config.enableFloating) {
                val measurer = rememberTextMeasurer(1)
                val density by rememberUpdatedState(LocalDensity.current)
                // 更新显示区域
                LaunchedEffect(measurer) {
                    val configFlow = snapshotFlow { config }
                        .distinctUntilChanged { old, new ->
                            old.displayArea == new.displayArea && old.style == new.style
                        }
                    val screenHeightPxFlow = snapshotFlow { screenHeightPx }
                        .debounce(500)
                    combine(configFlow, screenHeightPxFlow) { config, screenHeightPx ->
                        val danmakuHeightPx = measurer.measure(
                            DummyDanmakuState.presentation.danmaku.text,
                            style = config.style.styleForText(),
                        ).size.height
                        val verticalPaddingPx = with(density) {
                            (verticalPadding * 2).toPx()
                        }
                        val maxRows = screenHeightPx / (danmakuHeightPx + verticalPaddingPx)
                        (config.displayArea * maxRows).roundToInt().coerceAtLeast(1)
                    }.distinctUntilChanged()
                        .collect {
                            state.setTrackCount(it)
                        }
                }

                for (track in state.floatingTracks) {
                    FloatingDanmakuTrack(
                        track,
                        Modifier.fillMaxWidth(),
                        configProvider,
                        baseStyle = baseStyle,
                        frozen = frozen,
                    ) {
                        HeightHolder(verticalPadding)

                        for (danmaku in track.visibleDanmaku) {
                            key(danmaku.presentation.id) {
                                danmaku(danmaku)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DanmakuTrackScope.HeightHolder(verticalPadding: Dp) {
    // fix height even if there is no danmaku showing
    // so that the second track will not move to the top of the screen when the first track is empty.
    danmaku(DummyDanmakuState, Modifier.alpha(0f).padding(vertical = verticalPadding))
}

@Stable
internal class DanmakuHostStateImpl(
    private val danmakuTrackProperties: DanmakuTrackProperties,
) : DanmakuHostState {
    private val _isPaused = mutableStateOf(false)

    override var floatingTracks: List<FloatingDanmakuTrackState> by mutableStateOf(
        listOf(FloatingDanmakuTrackState(_isPaused, 30, danmakuTrackProperties)),
    )
        private set

    override var topTracks: List<FixedDanmakuTrackState> by mutableStateOf(
        listOf(FixedDanmakuTrackState(_isPaused)),
    )
        private set

    override var bottomTracks: List<FixedDanmakuTrackState> by mutableStateOf(
        listOf(FixedDanmakuTrackState(_isPaused)),
    )
        private set

    override fun setTrackCount(count: Int) {
        setTrackCountImpl(
            count = count,
            get = { floatingTracks },
            set = { floatingTracks = it },
            newInstance = { FloatingDanmakuTrackState(_isPaused, 30, danmakuTrackProperties) },
        )
        setTrackCountImpl(
            count = (count / 2).coerceAtLeast(1),
            get = { topTracks },
            set = { topTracks = it },
            newInstance = { FixedDanmakuTrackState(_isPaused) },
        )
        setTrackCountImpl(
            count = (count / 2).coerceAtLeast(1),
            get = { bottomTracks },
            set = { bottomTracks = it },
            newInstance = { FixedDanmakuTrackState(_isPaused) },
        )
    }

    private inline fun <T> setTrackCountImpl(
        count: Int,
        get: () -> List<T>,
        set: (List<T>) -> Unit,
        newInstance: () -> T,
    ) {
        val current = get()
        if (current.size == count) return
        set(
            if (count < current.size) {
                current.take(count)
            } else {
                current + List(count - current.size) {
                    newInstance()
                }
            },
        )
    }

    override fun trySend(danmaku: DanmakuPresentation): Boolean {
        val tracks = getTracks(danmaku)
        return tracks.any { it.trySend(danmaku) }
    }

    override suspend fun repopulate(list: List<DanmakuPresentation>, style: DanmakuStyle) {
        withContext(Dispatchers.Main.immediate) { // immediate: do not pay for dispatch
            // 还没 layout 之前等着
            while (!::textMeasurer.isInitialized
                || !::baseStyle.isInitialized
                || floatingTracks.fastAny { it.trackOffset.isNaN() }
            ) {
                delay(100)
            }
            runPopulate(list, style)
        }
    }

    private fun runPopulate(
        list: List<DanmakuPresentation>,
        style: DanmakuStyle
    ) {
        val textMeasurer = textMeasurer

        // 重置所有轨道以及它们的偏移
        for (track in floatingTracks) {
            track.clear()
            track.trackOffset = 0f
            track.populationVersion++
        }

        if (list.isEmpty()) return // fast path

        class Track(
            val state: FloatingDanmakuTrackState,
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

        // TODO: repopulate 支持顶部和底部 tracks
        topTracks.fastForEach { it.clear() }
        bottomTracks.fastForEach { it.clear() }

        val tracks = floatingTracks.map { Track(it) }
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
                lastSent ?: continue
                if (track.state.lastBaseSpeed.isNaN()) continue
                check(danmaku.danmaku.playTimeMillis >= lastSent.playTimeMillis) {
                    "danmaku list must be sorted by playTime"
                }
                val off =
                    (danmaku.danmaku.playTimeMillis - lastSent.playTimeMillis) / 1000f * track.state.lastBaseSpeed
                check(off >= 0f)
                track.state.trackOffset -= off
            }
            lastSent = danmaku.danmaku

            val track = useNextTrackOrNull() ?: continue // 所有轨道都有弹幕还未完全显示, 也就是都不能发弹幕, 跳过

            track.state.place(danmaku).apply {
                textWidth = textMeasurer.measure(
                    danmaku.danmaku.text,
                    overflow = TextOverflow.Visible,
                    style = danmakuStyle,
                    softWrap = false,
                    maxLines = 1,
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
