package me.him188.ani.app.ui.settings.tabs.media.source

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.data.source.media.TestMediaList
import me.him188.ani.app.ui.settings.tabs.media.source.rss.test.RssTestResultMediaItem
import me.him188.ani.utils.platform.annotations.TestOnly

@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
fun PreviewRssTestResultItem() {
    RssTestResultMediaItem(
        TestMediaList[0],
        false,
        {},
    )
}
