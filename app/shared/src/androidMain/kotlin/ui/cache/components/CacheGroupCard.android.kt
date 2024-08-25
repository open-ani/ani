package me.him188.ani.app.ui.cache.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.tools.Progress
import me.him188.ani.app.tools.toProgress
import me.him188.ani.app.ui.cache.TestCacheGroupSates
import me.him188.ani.app.ui.cache.createTestCacheEpisode
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
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
                progress = 0.5f.toProgress(),
                downloadSpeed = 233.megaBytes,
                totalSize = Unspecified,
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
                progress = Progress.Unspecified,
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
                progress = 0.3f.toProgress(),
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
