package me.him188.ani.app.ui.subject.episode.video.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.theme.aniDarkColorTheme

@Composable
fun EpisodeVideoSettingsSideSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: Dp = 8.dp,
    content: @Composable () -> Unit,
) {
    // Compose does not yet support side sheets, so we use a dropdown menu instead
    // https://m3.material.io/components/side-sheets/overview

    MaterialTheme(aniDarkColorTheme()) {
        BoxWithConstraints(
            Modifier.fillMaxSize()
                .windowInsetsPadding(BottomSheetDefaults.windowInsets)
                .clickable(
                    onClick = onDismissRequest,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ),
            contentAlignment = Alignment.TopEnd,
        ) {
            Surface(
                modifier
                    .clickable(
                        onClick = { }, // just to intercept clicks
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    )
                    .fillMaxHeight()
                    .width((maxWidth * 0.28f).coerceAtLeast(300.dp)),
            ) {
                Column(Modifier.padding(contentPadding)) {
                    content()
                }
            }
        }
    }
}
