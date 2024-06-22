package me.him188.ani.app.ui.foundation.text

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun ProvideTextStyleContentColor(
    value: TextStyle,
    color: Color = LocalContentColor.current,
    content: @Composable () -> Unit
) {
    val mergedStyle = LocalTextStyle.current.merge(value)
    CompositionLocalProvider(
        LocalTextStyle provides mergedStyle,
        LocalContentColor provides color, content = content,
    )
}
