package me.him188.ani.danmaku.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.ani.danmaku.api.Danmaku

/**
 * Configuration for the presentation of each [Danmaku].
 */
@Immutable
class DanmakuConfig(
    /**
     * Controls the text styles of the [Danmaku].
     * For example, font size, stroke width.
     */
    val style: DanmakuStyle = DanmakuStyle(),
    /**
     * Time for the [Danmaku] to move from the right edge to the left edge of the screen.
     * In other words, it controls the movement speed of a [Danmaku].
     */
    val durationMillis: Int = 15_000,
    /**
     * The minimum distance between two [Danmaku]s so that they don't overlap.
     */
    val safeSeparation: Dp = 32.dp,
) {
    companion object {
        val Default = DanmakuConfig()
    }
}

@Immutable
class DanmakuStyle(
    private val fontSize: TextUnit = 18.sp,
    val alpha: Float = 0.7f,
    private val strokeColor: Color = Color.Black,
    private val stroke: Stroke? = Stroke(
        miter = 1.5f,
        width = 6f,
        join = StrokeJoin.Round,
    ),
    private val shadow: Shadow? = null,
) {
    @Stable
    fun styleForBorder(): TextStyle = TextStyle(
        fontSize = fontSize,
        color = strokeColor,
        drawStyle = stroke,
        textMotion = TextMotion.Animated,
        shadow = shadow,
    )

    // 'inside' the border
    @Stable
    fun styleForText(): TextStyle = TextStyle(
        fontSize = fontSize,
        color = Color.White,
        textMotion = TextMotion.Animated,
    )
}