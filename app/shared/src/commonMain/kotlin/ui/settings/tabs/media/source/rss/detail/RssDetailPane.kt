package me.him188.ani.app.ui.settings.tabs.media.source.rss.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.cache.details.MediaDetailsColumn

@Composable
fun RssDetailPane(
    item: RssViewingItem,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    when (item) {
        is RssViewingItem.ViewingMedia -> MediaDetailsColumn(
            item.value,
            null,
            modifier.padding(contentPadding),
            showSourceInfo = false,
        )

        is RssViewingItem.ViewingRssItem -> {}
    }
}
