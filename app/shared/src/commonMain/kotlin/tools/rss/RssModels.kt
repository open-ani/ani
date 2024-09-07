package me.him188.ani.app.tools.rss

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


// See  me.him188.ani.app.tools.rss.RssParserTest.dmhy
@Serializable
@Immutable
data class RssChannel(
    val title: String,
    val description: String = "",
    val link: String = "",
    val ttl: Int = 0,
    val items: List<RssItem>
    // language
)

@Serializable
@Immutable
data class RssItem(
    val title: String,
    val description: String = "",
    val pubDate: LocalDateTime?,
    val link: String,
    val guid: String,
    val enclosure: RssEnclosure?
)

@Serializable
@Immutable
data class RssEnclosure(
    val url: String,
    val length: Long = 0,
    val type: String,// application/x-bittorrent
)
