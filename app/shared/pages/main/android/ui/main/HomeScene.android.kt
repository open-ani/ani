package me.him188.ani.app.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview

@Composable
@Preview
fun PreviewHomeScene() {
    ProvideCompositionLocalsForPreview {
        HomeScene()
    }
}