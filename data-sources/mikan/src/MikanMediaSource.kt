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

package me.him188.ani.datasources.mikan

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.charsets.decode
import io.ktor.utils.io.jvm.javaio.toInputStream
import io.ktor.utils.io.streams.asInput
import kotlinx.serialization.json.Json
import me.him188.ani.datasources.api.ConnectionStatus
import me.him188.ani.datasources.api.DownloadSearchQuery
import me.him188.ani.datasources.api.MediaSource
import me.him188.ani.datasources.api.MediaSourceConfig
import me.him188.ani.datasources.api.MediaSourceFactory
import me.him188.ani.datasources.api.applyMediaSourceConfig
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.titles.RawTitleParser
import me.him188.ani.datasources.api.titles.parse
import me.him188.ani.datasources.api.titles.toTopicDetails
import me.him188.ani.datasources.api.topic.FileSize.Companion.Zero
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.Topic
import me.him188.ani.datasources.api.topic.TopicCategory
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.seconds


class MikanMediaSource(
    private val config: MediaSourceConfig,
) : MediaSource {
    class Factory : MediaSourceFactory {
        override val id: String get() = ID
        override fun create(config: MediaSourceConfig): MediaSource = MikanMediaSource(config)
    }

    companion object {
        const val ID = "mikan"
        private val logger = logger<MikanMediaSource>()
    }

    override val id: String get() = ID

    override suspend fun checkConnection(): ConnectionStatus {
        return try {
            client.get("https://mikanani.me/").run {
                check(status.isSuccess()) { "Request failed: $this" }
            }
            ConnectionStatus.SUCCESS
        } catch (e: Exception) {
            logger.error(e) { "Failed to connect to mikanani.me" }
            ConnectionStatus.FAILED
        }
    }

    private val client = createHttpClient {
        applyMediaSourceConfig(config)
    }

    override suspend fun startSearch(query: DownloadSearchQuery): PagedSource<Topic> {
        fun DownloadSearchQuery.matches(topic: Topic): Boolean {
            val details = topic.details ?: return true

            episodeSort?.let { expected ->
                val ep = details.episode
                if (ep != null && ep.raw.removePrefix("0") != expected.removePrefix("0"))
                    return false
            }

            return true
        }
        return PageBasedPagedSource(initialPage = 1) {
            val resp = client.get("https://mikanani.me/RSS/Search") {
                parameter("searchstr", query.keywords)
            }
            val document: Document = Jsoup.parse(resp.bodyAsChannel().toInputStream(), "UTF-8", "https://mikanani.me/")
            parseDocument(document)
                .filter { query.matches(it) }
                .run {
                    Paged(size, false, this) // mikan 直接返回全部
                }
        }
    }
}

// 2024-03-31T10:27:49.932
private val LINK_REGEX = Regex("https://mikanani.me/Home/Episode/(.+)")

private fun parseDocument(document: Document): List<Topic> {
    val items = document.getElementsByTag("item")

    return items.map { element ->
        val title = element.getElementsByTag("title").text()

        val details = RawTitleParser.getParserFor().parse(title, null)

        Topic(
            id = element.getElementsByTag("guid").text().substringAfterLast("/"),
            publishedTimeMillis = element.getElementsByTag("pubDate").text().let {
                runCatching {
                    ZonedDateTime.of(
                        LocalDateTime.parse(it),
                        ZoneId.of("UTC+8"),
                    ).toEpochSecond() * 1000
                }.getOrNull()
            },
            category = TopicCategory.ANIME,
            rawTitle = title,
            commentsCount = 0,
            magnetLink = element.getElementsByTag("enclosure").attr("url"),
            size = element.getElementsByTag("contentLength").text().toLongOrNull()?.bytes ?: Zero,
            alliance = title.trim().split("]", "】").getOrNull(0).orEmpty().removePrefix("[").removePrefix("【").trim(),
            author = null,
            details = details.toTopicDetails(),
            link = run {
                element.getElementsByTag("link").text().takeIf { it.isNotBlank() }?.let { return@run it }
                // Note: It looks like Jsoup failed to parse the xml. Debug and print `element` to see details.
                LINK_REGEX.find(element.toString())?.value // This should work well
            } ?: "",
        )
    }
}

private fun createHttpClient(
    clientConfig: HttpClientConfig<*>.() -> Unit = {},
) = HttpClient {
    install(HttpRequestRetry) {
        maxRetries = 3
        delayMillis { 3000 }
    }
    install(WebSockets) {
        pingInterval = 20.seconds.inWholeMilliseconds
    }
    install(HttpCookies)
    install(HttpTimeout)
    install(UserAgent) {
        agent = "him188/ani (https://github.com/Him188/ani)"
    }
    clientConfig()
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
        register(
            ContentType.Text.Xml,
            object : ContentConverter {
                override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: ByteReadChannel): Any? {
                    if (typeInfo.type.qualifiedName != Document::class.qualifiedName) return null
                    content.awaitContent()
                    val decoder = Charsets.UTF_8.newDecoder()
                    val string = decoder.decode(content.toInputStream().asInput())
                    return Jsoup.parse(string, charset.name())
                }

                override suspend fun serializeNullable(
                    contentType: ContentType,
                    charset: Charset,
                    typeInfo: TypeInfo,
                    value: Any?
                ): OutgoingContent? {
                    return null
                }
            },
        ) {}
    }
}