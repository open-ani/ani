/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.rss

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.utils.xml.Element


// See  me.him188.ani.app.tools.rss.RssParserTest.dmhy
@Immutable
@Serializable // for testing
data class RssChannel(
    val title: String,
    val description: String = "",
    val link: String = "",
    val ttl: Int = 0,
    val items: List<RssItem>,
    /**
     * 原始 XML. 仅在测试时才有值, 其他时候为 `null` 以避免保持内存占用.
     */
    @Transient val origin: Element? = null,
)

@Immutable
@Serializable // for testing
data class RssItem(
    val title: String,
    val description: String = "",
    val pubDate: LocalDateTime?,
    val link: String,
    val guid: String,
    val enclosure: RssEnclosure?,
    /**
     * 原始 XML. 仅在测试时才有值, 其他时候为 `null` 以避免保持内存占用.
     */
    @Transient val origin: Element? = null,
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
@Serializable // for testing
data class RssEnclosure(
    val url: String,
    val length: Long = 0,
    val type: String,// application/x-bittorrent
)
