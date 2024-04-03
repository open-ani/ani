package me.him188.ani.app.videoplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.videoplayer.ui.guesture.SeekPositionIndicator


@PreviewLightDark
@Composable
private fun PreviewSeekPositionIndicatorForward() {
    ProvideCompositionLocalsForPreview {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
            ) {

            }
            SeekPositionIndicator(deltaDuration = 10)
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewSeekPositionIndicatorBackward() {
    ProvideCompositionLocalsForPreview {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
            ) {

            }
            SeekPositionIndicator(deltaDuration = -10)
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewSeekPositionIndicatorBackwardMinutes() {
    ProvideCompositionLocalsForPreview {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
            ) {

            }
            SeekPositionIndicator(deltaDuration = -90)
        }
    }
}
