package me.him188.ani.app.ui.profile.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview


@Composable
@PreviewLightDark
fun PreviewWelcomePage() {
    ProvideCompositionLocalsForPreview {
        WelcomePage({}, {})
    }
}