package me.him188.ani.app.ui.cache.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.ui.cache.TestCacheGroupSates
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.utils.platform.annotations.TestOnly

@OptIn(TestOnly::class)
@PreviewLightDark
@PreviewFontScale
@Composable
fun PreviewCacheGroupCard() = ProvideCompositionLocalsForPreview {
    Box(Modifier.background(Color.DarkGray)) {
        CacheGroupCard(TestCacheGroupSates[0])
    }
}
