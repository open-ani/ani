package me.him188.ani.app.ui.cache.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.ui.cache.TestCacheGroupSates
import me.him188.ani.app.ui.cache.createTestCacheEpisode
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.Unspecified
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.utils.platform.annotations.TestOnly

@OptIn(TestOnly::class)
@Preview
@Composable
fun PreviewCacheGroupCardMissingTotalSize() = ProvideCompositionLocalsForPreview {
    Box(Modifier.background(Color.DarkGray)) {
        CacheEpisodeItem(
            createTestCacheEpisode(
                1,
                progress = 0.5f,
                downloadSpeed = 233.megaBytes,
                totalSize = FileSize.Unspecified,
            ),
        )
    }
}

@OptIn(TestOnly::class)
@Preview
@Composable
fun PreviewCacheGroupCardMissingProgress() = ProvideCompositionLocalsForPreview {
    Box(Modifier.background(Color.DarkGray)) {
        CacheEpisodeItem(
            createTestCacheEpisode(
                1,
                progress = null,
                downloadSpeed = 233.megaBytes,
                totalSize = 888.megaBytes,
            ),
        )
    }
}

@OptIn(TestOnly::class)
@Preview
@Composable
fun PreviewCacheGroupCardMissingDownloadSpeed() = ProvideCompositionLocalsForPreview {
    Box(Modifier.background(Color.DarkGray)) {
        CacheEpisodeItem(
            createTestCacheEpisode(
                1,
                progress = 0.3f,
                downloadSpeed = Unspecified,
                totalSize = 888.megaBytes,
            ),
        )
    }
}

@OptIn(TestOnly::class)
@PreviewLightDark
@PreviewFontScale
@Composable
fun PreviewCacheGroupCard() = ProvideCompositionLocalsForPreview {
    Box(Modifier.background(Color.DarkGray)) {
        CacheGroupCard(TestCacheGroupSates[0])
    }
}
