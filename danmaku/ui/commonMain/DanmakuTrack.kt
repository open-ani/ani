package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.danmaku.api.DanmakuSessionAlgorithm
import java.util.UUID

@Stable
class DanmakuState internal constructor(
    val presentation: DanmakuPresentation,
    /**
     * 初始值满足 [offsetInsideTrack] + [FloatingDanmakuTrackState.trackOffset] == [FloatingDanmakuTrackState.trackSize].width
     */
    val offsetInsideTrack: Float = 0f, // positive
) {
    /**
     * Layout width of the view in px, late-init by [onPlaced].
     *
     * Can be `-1` if not yet initialized.
     */
    internal var textWidth by mutableIntStateOf(-1)
    internal var animationStarted by mutableStateOf(false)

    /**
     * Called when the danmaku is placed on the screen.
     */
    internal fun onPlaced(
        layoutCoordinates: LayoutCoordinates,
    ) {
        textWidth = layoutCoordinates.size.width
    }
}

@Stable
internal val DummyDanmakuState: DanmakuState = DanmakuState(
    DanmakuPresentation(
        Danmaku(
            UUID.randomUUID().toString(),
            "dummy",
            0L, "1",
            DanmakuLocation.NORMAL, "dummy 占位 攟 の 😄", 0,
        ),
        isSelf = false,
    ),
)

@Immutable
@Stable
class DanmakuTrackProperties(
    /**
     * Shift of the danmaku to be considered as fully out of the screen.
     */
    val visibilitySafeArea: Int = 0,
) {
    companion object {
        val Default = DanmakuTrackProperties()
    }
}

sealed interface DanmakuTrackState {
    /**
     * 尝试发送一条弹幕到这个轨道. 当轨道已满时返回 `false`.
     */
    @UiThread
    fun trySend(danmaku: DanmakuPresentation): Boolean

    /**
     * 清空所有屏幕上可见的弹幕以及发送队列.
     */
    @UiThread
    fun clear()
}

// Don't use inheritance, because we want to inline this
suspend inline fun DanmakuTrackState.send(danmaku: DanmakuPresentation) {
    return when (this) {
        is FixedDanmakuTrackState -> send(danmaku) // resolves to member function
        is FloatingDanmakuTrackState -> send(danmaku) // resolves to member function
    }
}


class FloatingDanmakuTrackState(
    isPaused: State<Boolean>,
    private val maxCount: Int,
    private val danmakuTrackProperties: DanmakuTrackProperties = DanmakuTrackProperties.Default,
) : DanmakuTrackState {
    /**
     * 正在发送的弹幕. 用于缓存后台逻辑帧发送的弹幕, 以便在下一 UI 帧开始渲染
     *
     * impl notes: 弹幕逻辑帧已经相当于有缓存, 这里不要缓存多余的, 可能造成滞后
     * @see DanmakuSessionAlgorithm
     */
    @PublishedApi
    internal val channel = Channel<DanmakuPresentation>(0, onBufferOverflow = BufferOverflow.SUSPEND)

    internal val isPaused by isPaused

    /**
     * Layout size of the track placed.
     */
    internal var trackSize: IntSize by mutableStateOf(IntSize.Zero)

    /**
     * 在屏幕中可见的弹幕
     */
    @Stable
    internal val visibleDanmaku: MutableList<DanmakuState> = SnapshotStateList() // Random Access is needed


    /**
     * 刚发送的还在屏幕右边边缘的弹幕
     */
    @Stable
    internal val startingDanmaku: MutableList<DanmakuState> = ArrayList() // actually contains only one element

    /**
     * 上次 [animateMove] 的速度. px/s
     */
    @JvmField
    var lastBaseSpeed: Float = 0f

    @JvmField
    var lastSafeSeparation: Float = 0f

    override fun trySend(danmaku: DanmakuPresentation): Boolean = channel.trySend(danmaku).isSuccess

    /**
     * 挂起当前协程, 直到成功发送这条弹幕.
     */
    suspend inline fun send(danmaku: DanmakuPresentation) {
        // inline to avoid additional Continuation as this is called frequently
        channel.send(danmaku)
    }

    /**
     * 立即将弹幕放置到轨道中, 忽视轨道是否已满或是否有弹幕仍然占据了初始位置.
     */
    @UiThread
    fun place(
        presentation: DanmakuPresentation,
        offsetInsideTrack: Float = -trackOffset + trackSize.width,
    ): DanmakuState {
        return DanmakuState(presentation, offsetInsideTrack = offsetInsideTrack).also {
            visibleDanmaku.add(it)
            startingDanmaku.add(it)
        }
    }

    @UiThread
    override fun clear() {
        @Suppress("ControlFlowWithEmptyBody")
        while (channel.tryReceive().isSuccess);
        visibleDanmaku.clear()
        startingDanmaku.clear()
    }


    /**
     * Called on every frame to update the state.
     */
    @UiThread
    internal suspend fun receiveNewDanmaku() {
        if (trackSize == IntSize.Zero) return
        if (trackOffset.isNaN()) return // track 还未放置
        if (visibleDanmaku.size >= maxCount) return // `>` is impossible, just to be defensive
        if (startingDanmaku.isNotEmpty()) return // 有弹幕仍然在屏幕右边   

        val danmaku = channel.receiveCatching().getOrNull() ?: return
        place(danmaku)
    }

    @UiThread
    internal fun checkDanmakuVisibility(
        layoutDirection: LayoutDirection,
        safeSeparation: Float
    ) {
        lastSafeSeparation = safeSeparation
        // Remove the danmaku from the track when it is out of screen, 
        // so that Track view will remove the Danmaku view from the layout.
        val trackOffset = trackOffset
        if (trackOffset.isNaN()) return
        visibleDanmaku.removeAll { danmaku -> // With RandomAccess, fast
            if (danmaku.textWidth == -1) return@removeAll false // not yet placed
            val posInScreen = danmaku.offsetInsideTrack + trackOffset
            posInScreen + danmaku.textWidth + danmakuTrackProperties.visibilitySafeArea <= 0  // out of screen
        }

        // Remove the danmaku from [startingDanmaku] if it is fully visible on the screen (with [safeSeparation]),
        // so that the track will receive the next danmaku and display it.
        startingDanmaku.removeAll { danmaku ->
            val posInScreen = danmaku.offsetInsideTrack + trackOffset
            val isFullyVisible = isFullyVisible(danmaku, safeSeparation, layoutDirection, posInScreen)
            posInScreen < 0 || isFullyVisible
        }
    }

    /**
     * 弹幕是否已经完全显示在屏幕上. 因为弹幕初始的时候是整个都在屏幕右边外面
     */
    fun isFullyVisible(
        danmaku: DanmakuState,
        safeSeparation: Float = lastSafeSeparation,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        posInScreen: Float = danmaku.offsetInsideTrack + trackOffset,
    ) = if (layoutDirection == LayoutDirection.Ltr) {
        posInScreen + danmaku.textWidth + safeSeparation + danmakuTrackProperties.visibilitySafeArea < trackSize.width
    } else {
        posInScreen - safeSeparation > 0
    }

    /**
     * 轨道位置偏移量, 会是负数. 轨道初始在屏幕右边缘.
     *
     * 使用时注意检查 [Float.isNaN]
     */
    var trackOffset: Float by mutableFloatStateOf(Float.NaN)
        internal set

    var populationVersion: Int by mutableIntStateOf(0)
        internal set

    /**
     * 匀速减少 [trackOffset].
     *
     * This function can be safely cancelled,
     * because it remembers the last [trackOffset].
     *
     * Returns when the animation has ended,
     * which means the danmaku has moved out of the screen (to the start).
     *
     * @param baseSpeed px/s
     */
    suspend fun animateMove(
        baseSpeed: Float,
    ) {
        lastBaseSpeed = baseSpeed
        val speed = -baseSpeed / 1_000_000_000f // px/ns
        if (trackOffset.isNaN()) {
            trackOffset = trackSize.width.toFloat()
        }

        restartAnimation@ while (true) {
            val currentVersion = populationVersion
            val startOffset = trackOffset
            val startTime = withFrameNanos { it }

            while (true) { // for each frame
                // Update offset on every frame
                val shouldRestart = withFrameNanos {
                    val elapsed = it - startTime
                    val res = startOffset + speed * elapsed
                    if (currentVersion != populationVersion) { // 必须在赋值 trackOffset 之前检查
                        return@withFrameNanos true
                    }
                    trackOffset = res
                    false
                }
                if (shouldRestart) {
                    continue@restartAnimation
                }
            }
        }
    }

}

abstract class DanmakuTrackScope {
    /**
     * Composes a Danmaku in the track.
     */
    @Composable
    fun danmaku(
        danmaku: DanmakuState,
        modifier: Modifier = Modifier,
    ) {
        danmakuImpl(danmaku, modifier)
    }

    /**
     * @see DanmakuTrackScope.danmaku
     */
    @Composable
    internal abstract fun danmakuImpl(
        // need this because abstract composable cannot have defaults
        danmaku: DanmakuState,
        modifier: Modifier,
    )
}

@Composable
fun FloatingDanmakuTrack(
    trackState: FloatingDanmakuTrackState,
    modifier: Modifier = Modifier,
    config: () -> DanmakuConfig = { DanmakuConfig.Default },
    baseStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    content: @Composable DanmakuTrackScope.() -> Unit, // box scope
) {
    val configUpdated by remember(config) { derivedStateOf(config) }
    val density by rememberUpdatedState(LocalDensity.current)
    val safeSeparation by remember {
        derivedStateOf {
            with(density) {
                configUpdated.safeSeparation.toPx()
            }
        }
    }
    val speedPxPerSecond by remember {
        derivedStateOf {
            with(density) {
                configUpdated.speed.dp.toPx()
            }
        }
    }
    val layoutDirection by rememberUpdatedState(LocalLayoutDirection.current)

    val scope = remember(trackState) {
        DanmakuTrackScopeImpl({ configUpdated }, { baseStyle })
    }

    LaunchedEffect(true) {
        while (isActive) {
            withContext(Dispatchers.Main.immediate) {
                trackState.checkDanmakuVisibility(layoutDirection, safeSeparation)
            }
            // We need this delay to calculate gently, because we need to ensure that the updating of offsets gets completed in every frame.
            delay(1000 / 120)
        }
    }

    LaunchedEffect(true) {
        while (isActive) {
            trackState.receiveNewDanmaku()
            // We need this delay to calculate gently, because we need to ensure that the updating of offsets gets completed in every frame.
            delay(1000 / 60)
        }
    }

    LaunchedEffect(
        trackState.trackSize,
        speedPxPerSecond,
        trackState.isPaused,
    ) {
        if (trackState.trackSize == IntSize.Zero) return@LaunchedEffect
        if (trackState.isPaused) return@LaunchedEffect
        trackState.animateMove(speedPxPerSecond)
    }

    BoxWithConstraints(
        modifier.onPlaced {
            trackState.trackSize = it.size
        },
    ) {
        Box(
            Modifier
                .clipToBounds()
                .fillMaxWidth()
                .graphicsLayer {
                    if (!trackState.trackOffset.isNaN()) {
                        translationX = trackState.trackOffset
                    }
                },
        ) {
            scope.content()
        }
    }
}


@Composable
fun FixedDanmakuTrack(
    trackState: FixedDanmakuTrackState,
    modifier: Modifier = Modifier,
    config: () -> DanmakuConfig = { DanmakuConfig.Default },
    baseStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    content: @Composable DanmakuTrackScope.() -> Unit, // box scope
) {
    val configUpdated by remember(config) { derivedStateOf(config) }

    val scope = remember(trackState) {
        DanmakuTrackScopeImpl({ configUpdated }, { baseStyle })
    }

    LaunchedEffect(true) {
        while (isActive) {
            trackState.receiveNewDanmaku(System.currentTimeMillis())
            // We need this delay to calculate gently, because we need to ensure that the updating of offsets gets completed in every frame.
            delay(1000 / 60)
        }
    }

    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        scope.content()
    }
}

@Stable
private class DanmakuTrackScopeImpl(
    private val getConfig: () -> DanmakuConfig,
    private val getBaseStyle: () -> TextStyle
) : DanmakuTrackScope() {
    @Composable
    override fun danmakuImpl(
        danmaku: DanmakuState,
        modifier: Modifier
    ) {
        Box(
            modifier
                .alpha(if (danmaku.animationStarted) 1f else 0f) // Don't use `danmaku.offset == 0`, see danmaku.offset comments.
                .graphicsLayer {
                    translationX = danmaku.offsetInsideTrack
                }
                .onPlaced { layoutCoordinates ->
                    danmaku.onPlaced(layoutCoordinates)
                }
                .wrapContentSize(),
        ) {
            DanmakuText(
                danmaku,
                config = getConfig(),
                baseStyle = getBaseStyle(),
                onTextLayout = {
                    danmaku.textWidth = it.size.width
                    danmaku.animationStarted = true
                },
            )
        }
    }
}

/**
 * The actual text of the danmaku.
 *
 * It is always white with black border.
 */
@Composable
fun DanmakuText(
    state: DanmakuState,
    modifier: Modifier = Modifier,
    config: DanmakuConfig = DanmakuConfig.Default,
    style: DanmakuStyle = config.style,
    baseStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    Box(modifier.alpha(style.alpha)) {
        // Black text with stronger stroke
        val text = if (config.isDebug) {
            remember(state) {
                state.presentation.danmaku.text +
                        " (${String.format("%.2f", state.presentation.danmaku.playTimeMillis.toFloat().div(1000))})"
            }
        } else {
            state.presentation.danmaku.text
        }
        Text(
            text,
            Modifier,
            overflow = TextOverflow.Clip,
            maxLines = 1,
            softWrap = false,
            style = baseStyle.merge(style.styleForBorder()),
            onTextLayout = onTextLayout,
        )
        // Covered by a white, smaller text.
        // So the resulting look is a white text with black border.
        Text(
            text,
            Modifier,
            overflow = TextOverflow.Clip,
            maxLines = 1,
            softWrap = false,
            style = baseStyle.merge(
                style.styleForText(
                    color = if (config.enableColor) {
                        rgbColor(
                            state.presentation.danmaku.color.toUInt().toLong(),
                        )
                    } else Color.White,
                ),
            ),
            textDecoration = if (state.presentation.isSelf) TextDecoration.Underline else null,
        )
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun rgbColor(value: Long): Color {
    return Color(0xFF_00_00_00L or value)
}
