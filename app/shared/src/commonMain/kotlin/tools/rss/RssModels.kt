package me.him188.ani.app.tools.rss

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


/**
 * @sample me.him188.ani.app.tools.rss.RssParserTest.dmhy
 */
@Serializable
data class RssChannel(
    val title: String,
    val description: String = "",
    val link: String = "",
    val ttl: Int = 0,
    val items: List<RssItem>
    // language
)

@Serializable
data class RssItem(
    val title: String,
    val description: String = "",
    val pubDate: LocalDateTime?,
    val link: String,
    val guid: String,
    val enclosure: RssEnclosure?
)

@Serializable
data class RssEnclosure(
    val url: String,
    val length: Long = 0,
    val type: String,// application/x-bittorrent
)
