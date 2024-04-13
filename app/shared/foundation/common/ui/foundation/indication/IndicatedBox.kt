package me.him188.ani.app.ui.foundation.indication

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Composable
fun IndicatedBox(
    indicator: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier) {
        content()
        indicator()
    }
}

@Composable
fun BoxScope.HorizontalIndicator(
    height: Dp,
    shape: Shape,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clip(shape)
            .matchParentSize()
            .drawBehind {
                drawLine(
                    color,
                    start = Offset(0f, size.height - (height / 2).toPx()),
                    end = Offset(size.width, size.height - (height / 2).toPx()),
                    strokeWidth = (height).toPx()
                )
            }
    )
}