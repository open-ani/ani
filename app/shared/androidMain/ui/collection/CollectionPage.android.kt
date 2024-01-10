package me.him188.ani.app.ui.collection

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import me.him188.ani.app.ProvideCompositionLocalsForPreview

@Composable
@Preview(widthDp = 1080 / 2, heightDp = 2400 / 2, showBackground = true)
internal actual fun PreviewCollectionPage() {
    ProvideCompositionLocalsForPreview {
        CollectionPage(viewModel = remember {
            MyCollectionsViewModel()
        })
    }
}