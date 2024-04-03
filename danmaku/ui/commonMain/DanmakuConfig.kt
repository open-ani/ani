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
    val style: DanmakuStyle = DanmakuStyle.Default,
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
    fun copy(
        style: DanmakuStyle = this.style,
        durationMillis: Int = this.durationMillis,
        safeSeparation: Dp = this.safeSeparation,
    ): DanmakuConfig {
        if (style == this.style &&
            durationMillis == this.durationMillis &&
            safeSeparation == this.safeSeparation
        ) {
            return this
        }
        return DanmakuConfig(
            style = style,
            durationMillis = durationMillis,
            safeSeparation = safeSeparation,
        )
    }

    override fun toString(): String {
        return "DanmakuConfig(style=$style, durationMillis=$durationMillis, safeSeparation=$safeSeparation)"
    }

    companion object {
        val Default = DanmakuConfig()
    }
}

@Immutable
class DanmakuStyle(
    val fontSize: TextUnit = 18.sp,
    val alpha: Float = 0.7f,
    val strokeColor: Color = Color.Black,
    val strokeWidth: Float = 4f,
    val shadow: Shadow? = null,
) {
    @Stable
    fun styleForBorder(): TextStyle = TextStyle(
        fontSize = fontSize,
        color = strokeColor,
        drawStyle = Stroke(
            miter = 3f,
            width = strokeWidth,
            join = StrokeJoin.Round,
        ),
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

    fun copy(
        fontSize: TextUnit = this.fontSize,
        alpha: Float = this.alpha,
        strokeColor: Color = this.strokeColor,
        strokeMidth: Float = this.strokeWidth,
        shadow: Shadow? = this.shadow,
    ): DanmakuStyle {
        if (fontSize == this.fontSize &&
            alpha == this.alpha &&
            strokeColor == this.strokeColor &&
            strokeMidth == this.strokeWidth &&
            shadow == this.shadow
        ) {
            return this
        }
        return DanmakuStyle(
            fontSize = fontSize,
            alpha = alpha,
            strokeColor = strokeColor,
            strokeWidth = strokeMidth,
            shadow = shadow,
        )
    }

    override fun toString(): String {
        return "DanmakuStyle(fontSize=$fontSize, alpha=$alpha, strokeColor=$strokeColor, strokeMiter=$strokeWidth, shadow=$shadow)"
    }

    companion object {
        val Default = DanmakuStyle()
    }
}