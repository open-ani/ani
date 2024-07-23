package me.him188.ani.app.ui.profile.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.profile.BangumiOAuthViewModel

@PreviewLightDark
@Composable
private fun PreviewAuthRequestPage() {
    ProvideCompositionLocalsForPreview {
        BangumiOAuthPage(
            remember { BangumiOAuthViewModel() },
            {},
        )
    }
}