package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.backgroundWithGradient
import me.him188.ani.app.ui.foundation.layout.isShowLandscapeUI

@Composable
fun SubjectBlurredBackground(
    coverImageUrl: String?,
    backgroundColor: Color,
    surfaceColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .blur(if (isShowLandscapeUI()) 32.dp else 16.dp)
            .backgroundWithGradient(
                coverImageUrl, backgroundColor,
                brush = if (isSystemInDarkTheme()) {
                    Brush.verticalGradient(
                        0f to surfaceColor.copy(alpha = 0xA2.toFloat() / 0xFF),
                        0.4f to surfaceColor.copy(alpha = 0xA2.toFloat() / 0xFF),
                        1.00f to backgroundColor,
                    )
                } else {
                    Brush.verticalGradient(
                        0f to Color(0xA2FAFAFA),
                        0.4f to Color(0xA2FAFAFA),
                        1.00f to backgroundColor,
                    )
                },
            ),
    )
}
