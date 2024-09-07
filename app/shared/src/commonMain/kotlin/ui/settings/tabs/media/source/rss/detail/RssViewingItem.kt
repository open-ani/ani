package me.him188.ani.app.ui.settings.tabs.media.source.rss.detail

import androidx.compose.runtime.Immutable
import me.him188.ani.app.tools.rss.RssItem
import me.him188.ani.datasources.api.Media

/**
 * 正在查看详情的项目
 */
@Immutable
sealed class RssViewingItem {
    abstract val value: Any

    @Immutable
    class ViewingMedia(override val value: Media) : RssViewingItem()

    @Immutable
    class ViewingRssItem(override val value: RssItem) : RssViewingItem()
}