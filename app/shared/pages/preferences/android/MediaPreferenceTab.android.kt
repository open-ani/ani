package me.him188.ani.app.ui.preference.tabs

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview

@PreviewLightDark
@Composable
private fun PreviewMediaPreferenceTab() {
    ProvideCompositionLocalsForPreview {
        MediaPreferenceTab()
    }
}