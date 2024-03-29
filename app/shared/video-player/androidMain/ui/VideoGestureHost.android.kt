package me.him188.ani.app.videoplayer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.videoplayer.ui.guesture.SeekPositionIndicator


@PreviewLightDark
@Composable
private fun PreviewSeekPositionIndicatorForward() {
    ProvideCompositionLocalsForPreview {
        SeekPositionIndicator(deltaDuration = 10)
    }
}

@PreviewLightDark
@Composable
private fun PreviewSeekPositionIndicatorBackward() {
    ProvideCompositionLocalsForPreview {
        SeekPositionIndicator(deltaDuration = -10)
    }
}

@PreviewLightDark
@Composable
private fun PreviewSeekPositionIndicatorBackwardMinutes() {
    ProvideCompositionLocalsForPreview {
        SeekPositionIndicator(deltaDuration = -90)
    }
}
