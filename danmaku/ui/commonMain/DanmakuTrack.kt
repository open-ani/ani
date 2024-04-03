package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import me.him188.ani.danmaku.api.Danmaku
import kotlin.math.abs
import kotlin.math.roundToInt

class DanmakuTrackState(
    private val maxCount: Int,
) {
    private val channel = Channel<Danmaku>(1)

    /**
     * 尝试发送一条弹幕到这个轨道. 当轨道已满时返回 `false`.
     */
    fun trySend(danmaku: Danmaku): Boolean = channel.trySend(danmaku).isSuccess

    @Stable
    internal val visibleDanmaku: MutableList<DanmakuState> = SnapshotStateList()


    /**
     * 刚发送的还在屏幕右边边缘的弹幕
     */
    @Stable
    internal val startingDanmaku: MutableList<Danmaku> = SnapshotStateList() // actually contains only one element

    private class DanmakuStateImpl(
        override val danmaku: Danmaku
    ) : DanmakuState

    @UiThread
    internal fun update() {
        if (visibleDanmaku.size >= maxCount) { // `>` is impossible, just to be defensive
            return
        }
        if (startingDanmaku.isNotEmpty()) { // 有弹幕仍然在屏幕右边   
            return
        }

        val danmaku = channel.tryReceive().getOrNull() ?: return
        startingDanmaku.add(danmaku)
        visibleDanmaku.add(DanmakuStateImpl(danmaku))
    }
}

abstract class DanmakuTrackScope {
    @Composable
    internal abstract fun danmakuImpl(
        danmaku: DanmakuState,
        style: DanmakuStyle,
        modifier: Modifier,
    )
}

/**
 * Composes a Danmaku in the track.
 */
@Composable
fun DanmakuTrackScope.danmaku(
    danmaku: DanmakuState,
    modifier: Modifier = Modifier,
    style: DanmakuStyle,
) {
    danmakuImpl(danmaku, style, modifier)
}

@Composable
fun DanmakuTrack(
    state: DanmakuTrackState,
    modifier: Modifier = Modifier,
    config: DanmakuConfig = DanmakuConfig.Default,
    content: @Composable DanmakuTrackScope.() -> Unit, // box scope
) {
    var trackSize by remember { mutableStateOf(IntSize.Zero) }
    val configState by rememberUpdatedState(config)
    val safeSeparation by rememberUpdatedState(
        with(LocalDensity.current) {
            config.safeSeparation.toPx()
        }
    )

    val scope = remember(state) {
        object : DanmakuTrackScope() {
            /**
             * Calculates animated offset of the danmaku.
             * @param textWidth calculated actual width of the danmaku text presented on the screen
             */
            @Composable
            private fun danmakuOffset(
                textWidth: Int
            ): Int {
                val layoutDirection = LocalLayoutDirection.current
                val safeShift = 20

                val windowWidth = trackSize.width
                var offset by remember(layoutDirection) {
                    mutableStateOf(
                        when (layoutDirection) {
                            LayoutDirection.Ltr -> windowWidth
                            LayoutDirection.Rtl -> 0
                        }
                    )
                }
                val offsetState = animateIntAsState(
                    offset,
//                    animationSpec = linear(
//                        pixelPerSec = density.run {
//                            windowWidth.toFloat() / configState.durationMillis
//                        }
//                    )
                    // velocity = windowWidth / durationMillis = actualWidth / t
                    // t = durationMillis * actualWidth / windowWidth
                    animationSpec = tween(
                        durationMillis = (configState.durationMillis * abs(if (windowWidth == 0) 9999999.0 else (windowWidth + textWidth + safeShift).toDouble() / windowWidth)).roundToInt(),
                        easing = LinearEasing,
                    )
                )
                LaunchedEffect(textWidth, layoutDirection) {
                    if (textWidth != 0) {
                        offset = when (layoutDirection) {
                            LayoutDirection.Ltr -> -(textWidth + safeShift)// definitely out of screen
                            LayoutDirection.Rtl -> windowWidth + textWidth + safeShift
                        }
                    }
                }
                return offsetState.value
            }

            @Composable
            override fun danmakuImpl(
                danmaku: DanmakuState,
                style: DanmakuStyle,
                modifier: Modifier
            ) {
                // Whether the rhs edge is fully visible on the screen (in LTR, RTL vice versa)
                var fullyVisible by remember { mutableStateOf(false) }

                // Updated later when first placed
                var textWidth by remember { mutableIntStateOf(0) }

                val danmakuOffset = danmakuOffset(textWidth)

                Box(
                    modifier
                        .offset {
                            IntOffset(x = danmakuOffset, y = 0)
                        }
                        .onPlaced { layoutCoordinates ->
                            val width = layoutCoordinates.size.width
                            textWidth = width

                            val pos = layoutCoordinates.positionInParent() // in the DanmakuTrack
                            if (pos.x + width <= 0) {
                                // out of screen
                                state.visibleDanmaku.remove(danmaku)
                            }

                            if (!fullyVisible &&
                                pos.x + width + safeSeparation < trackSize.width
                            ) {
                                fullyVisible = true
                                // in screen
                                state.startingDanmaku.remove(danmaku.danmaku)
                            }
                        }
                        .wrapContentSize()
                ) {
                    DanmakuText(danmaku, style = style)
                }
            }
        }
    }

    LaunchedEffect(true) {
        while (isActive) {
            withFrameMillis {
                state.update()
            }
        }
    }

    BoxWithConstraints(modifier.onPlaced {
        trackSize = it.size
    }) {
        scope.content()
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
) {
    Box(modifier.alpha(style.alpha)) {
        // Black text with stronger stroke
        Text(
            danmaku.danmaku.text,
            Modifier,
            overflow = TextOverflow.Visible,
            maxLines = 1,
            softWrap = false,
            style = baseStyle.merge(style.styleForBorder()),
        )
        // Covered by a white, smaller text.
        // So the resulting look is a white text with black border.
        Text(
            danmaku.danmaku.text,
            Modifier,
            overflow = TextOverflow.Visible,
            maxLines = 1,
            softWrap = false,
            style = baseStyle.merge(style.styleForText()),
        )
    }
}
