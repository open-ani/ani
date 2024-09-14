package me.him188.ani.app.ui.cache.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.source.media.TestMediaList
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.mikan.MikanCNMediaSource
import me.him188.ani.utils.platform.annotations.TestOnly

@OptIn(TestOnly::class)
@Composable
@Preview
fun PreviewCacheGroupDetailsColumn() = ProvideCompositionLocalsForPreview {
    MediaDetailsLazyGrid(
        TestMediaList[0],
        MikanCNMediaSource.INFO,
    )
} 
