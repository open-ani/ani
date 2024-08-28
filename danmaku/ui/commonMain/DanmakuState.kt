package me.him188.ani.danmaku.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextOverflow
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.utils.platform.format2f

/**
 * DanmakuState holds all params which [Canvas] needs to draw a danmaku text.
 */
@Immutable
data class DanmakuState(
    val presentation: DanmakuPresentation,
    val measurer: TextMeasurer,
    val baseStyle: TextStyle,
    val style: DanmakuStyle,
    val enableColor: Boolean,
    val isDebug: Boolean
) {
    val danmakuText = presentation.danmaku.run {
        if (isDebug) "$text (${String.format2f(playTimeMillis.toFloat().div(1000))})" else text
    }
    
    val solidTextLayout = measurer.measure(
        text = danmakuText,
        style = baseStyle.merge(
            style.styleForText(
                color = if (enableColor) {
                    rgbColor(presentation.danmaku.color.toUInt().toLong()).copy(alpha = style.alpha)
                } else Color.White,
            ),
        ),
        overflow = TextOverflow.Clip,
        maxLines = 1,
        softWrap = false,
    )
    
    val borderTextLayout = measurer.measure(
        text = danmakuText,
        style = baseStyle.merge(style.styleForBorder()),
        overflow = TextOverflow.Clip,
        maxLines = 1,
        softWrap = false,
    )
    
    val textWidth = solidTextLayout.size.width
}

/**
 * actually draw
 */
fun DrawScope.drawDanmakuText(
    state: DanmakuState,
    screenPosX: Float,
    screenPosY: Float,
) {
    translate(left = screenPosX, top = screenPosY) {
        // draw black bolder first, then solid text
        drawText(state.borderTextLayout)
        drawText(state.solidTextLayout)
        // draw underline if sent by self
        if (state.presentation.isSelf) {
            drawLine(
                color = state.style.strokeColor,
                strokeWidth = state.style.strokeWidth,
                cap = StrokeCap.Square,
                start = Offset(0f, state.solidTextLayout.size.height.toFloat()),
                end = state.solidTextLayout.size.run { Offset(width.toFloat(), height.toFloat()) }
            )
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun rgbColor(value: Long): Color {
    return Color(0xFF_00_00_00L or value)
}
