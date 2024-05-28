package me.him188.ani.app.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.preview.PHONE_LANDSCAPE

@Composable
@Preview
@Preview(device = PHONE_LANDSCAPE)
fun PreviewHomeScene() {
    ProvideCompositionLocalsForPreview {
        HomeScene()
    }
}
