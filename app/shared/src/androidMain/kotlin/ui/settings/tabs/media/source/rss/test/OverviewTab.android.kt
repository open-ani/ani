package me.him188.ani.app.ui.settings.tabs.media.source.rss.test

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.him188.ani.app.data.source.media.TestMediaList
import me.him188.ani.app.tools.rss.RssChannel
import me.him188.ani.app.tools.rss.RssEnclosure
import me.him188.ani.app.tools.rss.RssItem
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.preview.PreviewTabletLightDark
import me.him188.ani.datasources.api.topic.titles.ParsedTopicTitle
import me.him188.ani.utils.platform.annotations.TestOnly

@TestOnly
internal val TestRssChannel
    get() = RssChannel(
        title = "Title",
        description = "Description",
        link = "Link",
        ttl = 10,
        items = TestRssItems,
    )

@TestOnly
internal val TestRssItems
    get() = listOf(
        RssItem(
            title = "Title",
            description = "Description",
            pubDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            link = "Link",
            guid = "GUID",
            enclosure = RssEnclosure("https://example.com", type = "application/x-bittorrent"),
        ),
        RssItem(
            title = "Title",
            description = "Description",
            pubDate = LocalDateTime(2024, 7, 1, 1, 1, 1),
            link = "Link",
            guid = "GUID",
            enclosure = RssEnclosure("https://example.com", type = "application/x-bittorrent"),
        ),
    )

@TestOnly
internal val TestRssItemPresentations
    get() = listOf(
        RssItemPresentation(
            RssItem(
                title = "Title",
                description = "Description",
                pubDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                link = "Link",
                guid = "GUID",
                enclosure = RssEnclosure("https://example.com", type = "application/x-bittorrent"),
            ),
            parsed = ParsedTopicTitle(),
            tags = listOf(
                RssItemPresentation.Tag("01..02", isMatch = false),
                RssItemPresentation.Tag("1080P"),
            ),
        ),
        RssItemPresentation(
            RssItem(
                title = "Title",
                description = "Description",
                pubDate = LocalDateTime(2024, 7, 1, 1, 1, 1),
                link = "Link",
                guid = "GUID",
                enclosure = RssEnclosure("https://example.com", type = "application/x-bittorrent"),
            ),
            parsed = ParsedTopicTitle(),
            tags = listOf(
                RssItemPresentation.Tag("01..02", isMatch = false),
                RssItemPresentation.Tag("1080P"),
            ),
        ),
    )

@OptIn(TestOnly::class)
@Composable
@PreviewLightDark
@PreviewTabletLightDark
private fun PreviewOverviewTab() = ProvideCompositionLocalsForPreview {
    Surface(color = Color.Gray) {
        RssTestPaneDefaults.OverviewTab(
            remember {
                RssTestResult.Success(
                    "https://example.com",
                    TestRssChannel,
                    TestRssItemPresentations,
                    TestMediaList,
                    null,
                )
            },
        )

    }
}
