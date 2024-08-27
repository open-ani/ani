package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.utils.platform.Uuid
import me.him188.ani.utils.platform.currentTimeMillis
import me.him188.ani.utils.platform.format2f
import kotlin.random.Random

@Stable
class DanmakuState internal constructor(
    val presentation: DanmakuPresentation,
    /**
     * 初始值满足 [offsetInsideTrack] + [FloatingDanmakuTrackState.trackOffset] == [FloatingDanmakuTrackState.trackSize].width
     */
    val offsetInsideTrack: Float = 0f, // positive
    internal val style: DanmakuStyle = DanmakuStyle.Default,
    private val enableColor: Boolean = true,
    isDebug: Boolean = false,
) {
    internal val danmakuText by derivedStateOf {
        presentation.danmaku.run {
            if (isDebug) "$text (${String.format2f(playTimeMillis.toFloat().div(1000))})" else text
        }
    }
    
    /**
     * Layout width of the view in px, late-init by [onSizeChanged].
     *
     * Can be `-1` if not yet initialized.
     */
    internal var textWidth by mutableIntStateOf(-1)

    /**
     * Measure this danmaku and get layout.
     * 
     * This method ensures the measure param is same except [baseStyle].
     * So that you are expected to use the same [measurer] to accelerate measurement.
     * 
     * @see TextMeasurer.cacheSize
     */
    internal fun measure(
        measurer: TextMeasurer, 
        baseStyle: TextStyle,
        isForText: Boolean = true,
    ): TextLayoutResult {
        return measurer.measure(
            text = danmakuText,
            style = baseStyle.merge(
                if (isForText) {
                    style.styleForText(
                        color = if (enableColor) {
                            rgbColor(presentation.danmaku.color.toUInt().toLong()).copy(alpha = style.alpha)
                        } else Color.White,
                    )
                } else {
                    style.styleForBorder().run {
                        copy(color.copy(alpha = style.alpha))
                    }
                },
            ),
            overflow = TextOverflow.Clip,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Stable
internal val DummyDanmakuState: DanmakuState = DanmakuState(
    DanmakuPresentation(
        Danmaku(
            Uuid.randomString(),
            "dummy",
            0L, "1",
            DanmakuLocation.NORMAL, "dummy 占位 攟 の 😄", 0,
        ),
        isSelf = Random.Default.nextBoolean(),
    ),
)

@Immutable
@Stable
class DanmakuTrackProperties(
    /**
     * Shift of the danmaku to be considered as fully out of the screen.
     */
    val visibilitySafeArea: Int = 0,
    /**
     * maximum count of danmaku that a floating track can holds.
     */
    val maxDanmakuInTrack: Int = 30,
    /**
     * vertical padding of danmaku track.
     */
    val verticalPadding: Int = 1,
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
fun FixedDanmakuTrack(
    trackState: FixedDanmakuTrackState,
    modifier: Modifier = Modifier,
    config: () -> DanmakuConfig = { DanmakuConfig.Default },
    baseStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    frozen: Boolean = false,
    content: @Composable DanmakuTrackScope.() -> Unit, // box scope
) {
    val configUpdated by remember(config) { derivedStateOf(config) }

    val scope = remember(trackState) {
        DanmakuTrackScopeImpl({ configUpdated }, { baseStyle })
    }

    if (!frozen) {
        LaunchedEffect(true) {
            while (isActive) {
                trackState.receiveNewDanmaku(currentTimeMillis())
                // We need this delay to calculate gently, because we need to ensure that the updating of offsets gets completed in every frame.
                delay(1000 / 10)
            }
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
                //.alpha(if (danmaku.animationStarted) 1f else 0f) // Don't use `danmaku.offset == 0`, see danmaku.offset comments.
                .graphicsLayer {
                    translationX = danmaku.offsetInsideTrack
                }
                .onSizeChanged { size ->
                    danmaku.textWidth = size.width
                }
                .wrapContentSize(),
        ) {
            // DanmakuText(
            //     danmaku,
            //     config = getConfig(),
            //     baseStyle = getBaseStyle(),
            //     onTextLayout = {
            //         danmaku.textWidth = it.size.width
            //         danmaku.animationStarted = true
            //     },
            // )
        }
    }
}

/**
 * The actual text of the danmaku.
 *
 * It is always white with black border.
 */
fun DrawScope.drawDanmakuText(
    state: DanmakuState,
    borderTextMeasurer: TextMeasurer,
    solidTextMeasurer: TextMeasurer,
    offsetX: Float,
    offsetY: Float,
    baseStyle: TextStyle,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    translate(left = offsetX, top = offsetY) {
        val solidTextLayout = state.measure(solidTextMeasurer, baseStyle)
        onTextLayout?.invoke(solidTextLayout)
        // draw black bolder first, then solid text
        drawText(state.measure(borderTextMeasurer, baseStyle, false))
        drawText(solidTextLayout)
        // draw underline if sent by self
        if (state.presentation.isSelf) {
            drawLine(
                color = state.style.strokeColor,
                strokeWidth = state.style.strokeWidth,
                cap = StrokeCap.Square,
                start = Offset(0f, solidTextLayout.size.height.toFloat()),
                end = solidTextLayout.size.run { Offset(width.toFloat(), height.toFloat()) }
            )
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun rgbColor(value: Long): Color {
    return Color(0xFF_00_00_00L or value)
}
