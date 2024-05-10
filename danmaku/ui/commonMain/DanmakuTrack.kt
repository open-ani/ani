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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import java.util.UUID

@Stable
class DanmakuState internal constructor(
    val danmaku: DanmakuPresentation,
    val offsetInsideTrack: Float = 0f,
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
            DanmakuLocation.NORMAL, "dummy 占位 攟 の 😄", 0
        ),
        isSelf = false
    )
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

class DanmakuTrackState(
    isPaused: State<Boolean>,
    private val maxCount: Int,
    private val danmakuTrackProperties: DanmakuTrackProperties = DanmakuTrackProperties.Default,
) {
    @PublishedApi
    internal val channel = Channel<DanmakuPresentation>(2)

    /**
     * 尝试发送一条弹幕到这个轨道. 当轨道已满时返回 `false`.
     */
    fun trySend(danmaku: DanmakuPresentation): Boolean = channel.trySend(danmaku).isSuccess

    suspend inline fun send(danmaku: DanmakuPresentation) {
        // inline to improve performance as this is called frequently
        channel.send(danmaku)
    }

    internal val isPaused by isPaused

    /**
     * Layout size of the track placed.
     */
    internal var trackSize: IntSize by mutableStateOf(IntSize.Zero)

    @Stable
    internal val visibleDanmaku: MutableList<DanmakuState> = SnapshotStateList() // Random Access is needed


    /**
     * 刚发送的还在屏幕右边边缘的弹幕
     */
    @Stable
    internal val startingDanmaku: MutableList<DanmakuState> = ArrayList() // actually contains only one element

    /**
     * Called on every frame to update the state.
     */
    @UiThread
    internal fun receiveNewDanmaku() {
        if (trackSize == IntSize.Zero) return
        if (trackOffset.isNaN()) return // track 还未放置
        if (visibleDanmaku.size >= maxCount) return // `>` is impossible, just to be defensive
        if (startingDanmaku.isNotEmpty()) return // 有弹幕仍然在屏幕右边   

        val danmaku = channel.tryReceive().getOrNull() ?: return
        val state = DanmakuState(danmaku, offsetInsideTrack = trackOffset - trackSize.width)
        startingDanmaku.add(state)
        visibleDanmaku.add(state)
    }

    @UiThread
    internal fun checkDanmakuVisibility(
        layoutDirection: LayoutDirection,
        safeSeparation: Float
    ) {
        // Remove the danmaku from the track when it is out of screen, 
        // so that Track view will remove the Danmaku view from the layout.
        val trackOffset = trackOffset
        if (trackOffset.isNaN()) return
        visibleDanmaku.removeAll { danmaku -> // With RandomAccess, fast
            if (danmaku.textWidth == -1) return@removeAll false // not yet placed
            val posInScreen = -danmaku.offsetInsideTrack + trackOffset
            posInScreen + danmaku.textWidth + danmakuTrackProperties.visibilitySafeArea <= 0  // out of screen
        }

        // Remove the danmaku from [startingDanmaku] if it is fully visible on the screen (with [safeSeparation]),
        // so that the track will receive the next danmaku and display it.
        startingDanmaku.removeAll { danmaku ->
            val posInScreen = -danmaku.offsetInsideTrack + trackOffset
            val isFullyVisible = if (layoutDirection == LayoutDirection.Ltr) {
                posInScreen + danmaku.textWidth + safeSeparation + danmakuTrackProperties.visibilitySafeArea < trackSize.width
            } else {
                posInScreen - safeSeparation > 0
            }
            posInScreen < 0 || isFullyVisible
        }
    }

    /**
     * 轨道位置偏移量, 会是负数. 轨道初始在屏幕右边缘.
     */
    var trackOffset: Float by mutableFloatStateOf(Float.NaN)
        private set

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
        val startTime = withFrameNanos { it }
        val speed = -baseSpeed / 1_000_000_000f // px/ns

        if (trackOffset.isNaN()) {
            trackOffset = trackSize.width.toFloat()
        }

        val startOffset = trackOffset

        while (true) {
            // Update offset on every frame
            withFrameNanos {
                val elapsed = it - startTime
                trackOffset = startOffset + speed * elapsed
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
        style: DanmakuStyle,
    ) {
        danmakuImpl(danmaku, style, modifier)
    }

    /**
     * @see DanmakuTrackScope.danmaku
     */
    @Composable
    internal abstract fun danmakuImpl(
        // need this because abstract composable cannot have defaults
        danmaku: DanmakuState,
        style: DanmakuStyle,
        modifier: Modifier,
    )
}

@Composable
fun DanmakuTrack(
    trackState: DanmakuTrackState,
    modifier: Modifier = Modifier,
    config: DanmakuConfig = DanmakuConfig.Default,
    content: @Composable DanmakuTrackScope.() -> Unit, // box scope
) {
    val configUpdated by rememberUpdatedState(config)
    val safeSeparation by rememberUpdatedState(
        with(LocalDensity.current) {
            configUpdated.safeSeparation.toPx()
        }
    )
    val density by rememberUpdatedState(LocalDensity.current)
    val speedPxPerSecond by derivedStateOf {
        with(density) {
            configUpdated.speed.dp.toPx()
        }
    }
    val layoutDirection by rememberUpdatedState(LocalLayoutDirection.current)

    val scope = remember(trackState) {
        object : DanmakuTrackScope() {
            @Composable
            override fun danmakuImpl(
                danmaku: DanmakuState,
                style: DanmakuStyle,
                modifier: Modifier
            ) {
                Box(
                    modifier
                        .alpha(if (danmaku.animationStarted) 1f else 0f) // Don't use `danmaku.offset == 0`, see danmaku.offset comments.
                        .graphicsLayer {
                            translationX = -danmaku.offsetInsideTrack
                        }
                        .onPlaced { layoutCoordinates ->
                            danmaku.onPlaced(layoutCoordinates)
                        }
                        .wrapContentSize()
                ) {
                    DanmakuText(
                        danmaku,
                        style = style,
                        onTextLayout = {
                            danmaku.textWidth = it.size.width
                            danmaku.animationStarted = true
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(true) {
        while (isActive) {
            withContext(Dispatchers.Main.immediate) {
                trackState.receiveNewDanmaku()
                trackState.checkDanmakuVisibility(layoutDirection, safeSeparation)
            }
            // We need this delay to calculate gently, because we need to ensure that the updating of offsets gets completed in every frame.
            delay(1000 / 120)
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

    BoxWithConstraints(modifier.onPlaced {
        trackState.trackSize = it.size
    }) {
        Box(Modifier
            .clipToBounds()
            .fillMaxWidth()
            .graphicsLayer {
                if (!trackState.trackOffset.isNaN()) {
                    translationX = trackState.trackOffset
                }
            }
        ) {
            scope.content()
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
    danmaku: DanmakuState,
    modifier: Modifier = Modifier,
    style: DanmakuStyle = DanmakuStyle.Default,
    baseStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    Box(modifier.alpha(style.alpha)) {
        // Black text with stronger stroke
        Text(
            danmaku.danmaku.danmaku.text,
            Modifier,
            overflow = TextOverflow.Clip,
            maxLines = 1,
            softWrap = false,
            style = baseStyle.merge(style.styleForBorder()),
            onTextLayout = onTextLayout
        )
        // Covered by a white, smaller text.
        // So the resulting look is a white text with black border.
        Text(
            danmaku.danmaku.danmaku.text,
            Modifier,
            overflow = TextOverflow.Clip,
            maxLines = 1,
            softWrap = false,
            style = baseStyle.merge(style.styleForText()),
            textDecoration = if (danmaku.danmaku.isSelf) TextDecoration.Underline else null,
        )
    }
}
