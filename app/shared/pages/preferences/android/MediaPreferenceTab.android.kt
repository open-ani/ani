package me.him188.ani.app.ui.preference

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.preference.tabs.MediaPreferenceTab

@PreviewLightDark
@Composable
private fun PreviewMediaPreferenceTab() {
    ProvideCompositionLocalsForPreview {
        MediaPreferenceTab()
    }
}