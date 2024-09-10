package me.him188.ani.app.ui.settings.tabs.media.source.rss.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.data.source.media.TestMediaList
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.preview.PreviewTabletLightDark
import me.him188.ani.app.ui.settings.tabs.media.source.rss.test.TestRssItemPresentations
import me.him188.ani.utils.platform.annotations.TestOnly

@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
@PreviewTabletLightDark
fun PreviewRssDetailPaneRssItemShowTopBar() = ProvideCompositionLocalsForPreview {
    RssDetailPane(
        RssViewingItem.ViewingRssItem(
            TestRssItemPresentations[0],
        ),
    )
}

@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
@PreviewTabletLightDark
fun PreviewRssDetailPaneRssItem() = ProvideCompositionLocalsForPreview {
    RssDetailPane(
        RssViewingItem.ViewingRssItem(
            TestRssItemPresentations[0],
        ),
    )
}

@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
@PreviewTabletLightDark
fun PreviewRssDetailPaneMedia() = ProvideCompositionLocalsForPreview {
    RssDetailPane(
        RssViewingItem.ViewingMedia(
            TestMediaList[0],
        ),
    )
}
