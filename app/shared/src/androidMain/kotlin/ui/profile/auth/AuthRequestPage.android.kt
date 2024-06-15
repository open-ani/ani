package me.him188.ani.app.ui.profile.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.profile.AuthViewModel

@Preview
@Composable
private fun PreviewAuthRequestPage() {
    ProvideCompositionLocalsForPreview {
        AuthRequestPage(
            remember { AuthViewModel() },
        )
    }
}