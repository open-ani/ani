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
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.CIOEngineConfig
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
import io.ktor.serialization.ContentConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.charsets.decode
import io.ktor.utils.io.jvm.javaio.toInputStream
import io.ktor.utils.io.streams.asInput
import kotlinx.serialization.json.Json
import me.him188.ani.datasources.api.DownloadProvider
import me.him188.ani.datasources.api.DownloadSearchQuery
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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.seconds


interface MikanClient {
    fun startSearchSession(filter: DownloadSearchQuery): PagedSource<Topic>

    companion object Factory {
        fun create(engineConfig: HttpClientConfig<*>.() -> Unit): MikanClient =
            MikanClientImpl(engineConfig)
    }
}


class MikanDownloadProvider(
    private val client: MikanClient = MikanClient.create { },
) : DownloadProvider {
    companion object {
        const val ID = "mikan"
    }

    override val id: String get() = ID

    override suspend fun startSearch(query: DownloadSearchQuery): PagedSource<Topic> {
        return client.startSearchSession(query)
    }
}

class MikanClientImpl(
    engineConfig: HttpClientConfig<*>.() -> Unit,
) : MikanClient {
    private val client = createHttpClient(engineConfig)
    override fun startSearchSession(filter: DownloadSearchQuery): PagedSource<Topic> {
        fun DownloadSearchQuery.matches(topic: Topic): Boolean {
            val details = topic.details ?: return true

            this.episodeSort?.let { expected ->
                val ep = details.episode
                if (ep != null && ep.raw.removePrefix("0") != expected.removePrefix("0"))
                    return false
            }

            return true
        }

        return PageBasedPagedSource(initialPage = 1) {
            val resp = client.get("https://mikanani.me/RSS/Search") {
                parameter("searchstr", filter.keywords)
            }
            val document: Document = Jsoup.parse(resp.bodyAsChannel().toInputStream(), "UTF-8", "https://mikanani.me/")
            parseDocument(document)
                .filter { filter.matches(it) }
                .run {
                    Paged(size, false, this) // mikan 直接返回全部
                }
        }
    }
}

// 2024-03-31T10:27:49.932
private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH)

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
            link = element.getElementsByTag("link").text(),
        )
    }
}

private fun createHttpClient(
    clientConfig: HttpClientConfig<CIOEngineConfig>.() -> Unit = {},
) = HttpClient(CIO) {
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