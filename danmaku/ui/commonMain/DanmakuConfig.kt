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
import kotlinx.serialization.Transient
import me.him188.ani.danmaku.api.Danmaku

/**
 * Configuration for the presentation of each [Danmaku].
 */
@Immutable
data class DanmakuConfig(
    // 备注: 增加新的属性后还要修改 [DanmakuConfigData]
    /**
     * Controls the text styles of the [Danmaku].
     * For example, font size, stroke width.
     */
    val style: DanmakuStyle = DanmakuStyle.Default,
    /**
     * Time for the [Danmaku] to move from the right edge to the left edge of the screen.
     * In other words, it controls the movement speed of a [Danmaku].
     *
     * Unit: dp/s
     */
    val speed: Float = 88f,
    /**
     * The minimum distance between two [Danmaku]s so that they don't overlap.
     */
    val safeSeparation: Dp = 48.dp,
    /**
     * 允许彩色弹幕. 禁用时将会把所有彩色弹幕都显示为白色.
     */
    val enableColor: Boolean = true,
    /**
     * 调试模式, 启用发送弹幕的信息.
     */
    val isDebug: Boolean = false,
    @Suppress("PropertyName") @Transient val _placeholder: Int = 0,
) {
    companion object {
        @Stable
        val Default = DanmakuConfig()
    }
}

@Immutable
class DanmakuStyle(
    val fontSize: TextUnit = 18.sp,
    val alpha: Float = 0.8f,
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
    fun styleForText(color: Color = Color.White): TextStyle = TextStyle(
        fontSize = fontSize,
        color = color,
        textMotion = TextMotion.Animated,
    )

    fun copy(
        fontSize: TextUnit = this.fontSize,
        alpha: Float = this.alpha,
        strokeColor: Color = this.strokeColor,
        strokeWidth: Float = this.strokeWidth,
        shadow: Shadow? = this.shadow,
    ): DanmakuStyle {
        if (fontSize == this.fontSize &&
            alpha == this.alpha &&
            strokeColor == this.strokeColor &&
            strokeWidth == this.strokeWidth &&
            shadow == this.shadow
        ) {
            return this
        }
        return DanmakuStyle(
            fontSize = fontSize,
            alpha = alpha,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
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