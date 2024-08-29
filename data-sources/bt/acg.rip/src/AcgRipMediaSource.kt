/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.datasources.acgrip

import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.toInputStream
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.DownloadSearchQuery
import me.him188.ani.datasources.api.source.FactoryId
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceInfo
import me.him188.ani.datasources.api.source.TopicMediaSource
import me.him188.ani.datasources.api.source.useHttpClient
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.Topic
import me.him188.ani.datasources.api.topic.TopicCategory
import me.him188.ani.datasources.api.topic.matches
import me.him188.ani.datasources.api.topic.titles.RawTitleParser
import me.him188.ani.datasources.api.topic.titles.parse
import me.him188.ani.datasources.api.topic.titles.toTopicDetails
import me.him188.ani.utils.logging.error
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AcgRipMediaSource(
    config: MediaSourceConfig,
) : TopicMediaSource() {
    class Factory : MediaSourceFactory {
        override val factoryId: FactoryId get() = FactoryId(ID)
        override val info: MediaSourceInfo get() = INFO
        override fun create(mediaSourceId: String, config: MediaSourceConfig): MediaSource = AcgRipMediaSource(config)
    }

    companion object {
        const val ID = "acg.rip"
        val INFO = MediaSourceInfo(
            "ACG.RIP",
            websiteUrl = "https://acg.rip",
            imageUrl = "https://acg.rip/favicon.ico",
            imageResourceId = "acg-rip.png",
        )
    }

    override val mediaSourceId: String get() = ID

    private val client by lazy { useHttpClient(config) }

    override suspend fun checkConnection(): ConnectionStatus {
        return try {
            client.get("https://acg.rip/").run {
                check(status.isSuccess()) { "Request failed: $status" }
            }
            ConnectionStatus.SUCCESS
        } catch (e: Exception) {
            logger.error(e) { "Failed to connect to acg.rip" }
            ConnectionStatus.FAILED
        }
    }

    override suspend fun startSearch(query: DownloadSearchQuery): PagedSource<Topic> {
        return PageBasedPagedSource(initialPage = 1) { page ->
            if (page == 1) {
                try {
                    val seekPages = client.get("https://acg.rip/$page.html") {
                        parameter("term", query.keywords)
                    }
                    Jsoup.parse(seekPages.bodyAsChannel().toInputStream(), "UTF-8", "https://acg.rip/.xml")
                        .getElementsByClass("pagination")
                        .firstOrNull()
                        ?.getElementsByTag("a")
                        ?.mapNotNull { it.text().toIntOrNull() }
                        ?.maxOrNull()
                        ?.let { setTotalSize(it) }
                } catch (_: Throwable) {
                    // best effort to get total size
                }
            }

            val resp = client.get("https://acg.rip/$page.xml") {
                parameter("term", query.keywords)
            }
            val document: Document = Jsoup.parse(resp.bodyAsChannel().toInputStream(), "UTF-8", "https://acg.rip/.xml")
            parseDocument(document)
                .filter { query.matches(it, allowEpMatch = false) }
                .run {
                    Paged(size, isNotEmpty(), this)
                }
        }
    }

    override val info: MediaSourceInfo get() = INFO
}

private val FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)

private fun parseDocument(document: Document): List<Topic> {
    val items = document.getElementsByTag("item")

    return items.map { element ->
        val title = element.getElementsByTag("title").text()

        val details = RawTitleParser.getDefault().parse(title, null)

        Topic(
            topicId = "acgrip-${element.getElementsByTag("guid").text().substringAfterLast("/")}",
            publishedTimeMillis = element.getElementsByTag("pubDate").text().let {
                // java.time.format.DateTimeParseException: Text 'Sun, 25 Feb 2024 08:32:16 -0800' could not be parsed at index 0
                runCatching { ZonedDateTime.parse(it, FORMATTER).toEpochSecond() * 1000 }.getOrNull()
            },
            category = TopicCategory.ANIME,
            rawTitle = title,
            commentsCount = 0,
            downloadLink = ResourceLocation.HttpTorrentFile(
                element.getElementsByTag("enclosure").attr("url"),
            ),
            size = 0.bytes,
            alliance = title.trim().split("]", "】").getOrNull(0).orEmpty().removePrefix("[").removePrefix("【").trim(),
            author = null,
            details = details.toTopicDetails(),
            originalLink = element.getElementsByTag("link").text(),
        )
    }
}
