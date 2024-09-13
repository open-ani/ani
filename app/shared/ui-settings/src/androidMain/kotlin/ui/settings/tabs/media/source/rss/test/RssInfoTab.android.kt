package me.him188.ani.app.ui.settings.tabs.media.source.rss.test

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.ui.foundation.preview.PreviewTabletLightDark
import me.him188.ani.utils.platform.annotations.TestOnly

@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
@PreviewTabletLightDark
fun PreviewRssInfoTab() {
    RssTestPaneDefaults.RssInfoTab(
        items = TestRssItemPresentations,
        onViewDetails = { },
        selectedItemProvider = { TestRssItemPresentations[1] },
    )
}
