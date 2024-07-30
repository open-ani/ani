package me.him188.ani.app.videoplayer.ui.guesture

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ScreenshotButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PlayerFloatingButtonBox(
        modifier = modifier,
        content = {
            IconButton(onClick) {
                val color = Color.White
                CompositionLocalProvider(LocalContentColor provides color) {
                    Icon(Icons.Rounded.PhotoCamera, contentDescription = "Lock screen")
                }
            }
        },
    )
}

