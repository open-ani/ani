package me.him188.ani.app.ui.collection

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview

@Composable
@Preview(widthDp = 1080 / 2, heightDp = 2400 / 2, showBackground = true)
internal actual fun PreviewCollectionPage() {
    ProvideCompositionLocalsForPreview {
        CollectionPage(contentPadding = PaddingValues(0.dp), viewModel = remember {
            MyCollectionsViewModel()
        })
    }
}