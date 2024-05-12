package me.him188.ani.app.ui.preference.tabs.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview

@PreviewLightDark
@Composable
private fun PreviewWebUIHelpLayout() {
    ProvideCompositionLocalsForPreview {
        WebUIHelpLayout({})
    }
}

@PreviewLightDark
@Composable
private fun PreviewAniQBHelpLayout() {
    ProvideCompositionLocalsForPreview {
        AniQBHelpLayout({})
    }
}
