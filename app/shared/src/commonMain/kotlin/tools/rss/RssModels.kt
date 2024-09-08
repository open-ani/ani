package me.him188.ani.app.tools.rss

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.utils.xml.Element


// See  me.him188.ani.app.tools.rss.RssParserTest.dmhy
@Immutable
data class RssChannel(
    val title: String,
    val description: String = "",
    val link: String = "",
    val ttl: Int = 0,
    val items: List<RssItem>,
    val origin: Element? = null,
    // language
)

@Immutable
data class RssItem(
    val title: String,
    val description: String = "",
    val pubDate: LocalDateTime?,
    val link: String,
    val guid: String,
    val enclosure: RssEnclosure?,
    val origin: Element? = null,
)

fun RssItem.guessResourceLocation(): ResourceLocation? {
    val url = this.enclosure?.url ?: this.link.takeIf { it.isNotBlank() } ?: return null
    return if (url.startsWith("magnet:")) {
        ResourceLocation.MagnetLink(url)
    } else {
        ResourceLocation.HttpTorrentFile(url)
    }
}

@Immutable
data class RssEnclosure(
    val url: String,
    val length: Long = 0,
    val type: String,// application/x-bittorrent
)
