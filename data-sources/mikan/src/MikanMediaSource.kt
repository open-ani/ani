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
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
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
import kotlinx.coroutines.flow.asFlow
import kotlinx.serialization.json.Json
import me.him188.ani.datasources.api.paging.SingleShotPagedSource
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.applyMediaSourceConfig
import me.him188.ani.datasources.api.source.toOnlineMedia
import me.him188.ani.datasources.api.topic.FileSize.Companion.Zero
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.Topic
import me.him188.ani.datasources.api.topic.TopicCategory
import me.him188.ani.datasources.api.topic.TopicCriteria
import me.him188.ani.datasources.api.topic.matches
import me.him188.ani.datasources.api.topic.titles.RawTitleParser
import me.him188.ani.datasources.api.topic.titles.parse
import me.him188.ani.datasources.api.topic.titles.toTopicDetails
import me.him188.ani.datasources.api.topic.toTopicCriteria
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class MikanCNMediaSource(
    config: MediaSourceConfig,
    indexCacheProvider: MikanIndexCacheProvider = MemoryMikanIndexCacheProvider(),
) : AbstractMikanMediaSource(ID, config, "https://mikanime.tv", indexCacheProvider) {
    class Factory : MediaSourceFactory {
        override val mediaSourceId: String get() = ID
        override fun create(config: MediaSourceConfig): MediaSource =
            MikanCNMediaSource(config)

        fun create(
            config: MediaSourceConfig,
            indexCacheProvider: MikanIndexCacheProvider = MemoryMikanIndexCacheProvider()
        ): MediaSource = MikanCNMediaSource(config, indexCacheProvider)
    }

    companion object {
        const val ID = "mikan-mikanime-tv"
    }
}

class MikanMediaSource(
    config: MediaSourceConfig,
    indexCacheProvider: MikanIndexCacheProvider = MemoryMikanIndexCacheProvider(),
) : AbstractMikanMediaSource(ID, config, "https://mikanani.me", indexCacheProvider) {
    class Factory : MediaSourceFactory {
        override val mediaSourceId: String get() = ID
        override fun create(config: MediaSourceConfig): MediaSource = MikanMediaSource(config)

        fun create(
            config: MediaSourceConfig,
            indexCacheProvider: MikanIndexCacheProvider = MemoryMikanIndexCacheProvider()
        ): MediaSource = MikanMediaSource(config, indexCacheProvider)
    }

    companion object {
        const val ID = "mikan"
    }
}

abstract class AbstractMikanMediaSource(
    override val mediaSourceId: String,
    private val config: MediaSourceConfig,
    baseUrl: String,
    private val indexCacheProvider: MikanIndexCacheProvider,
) : MediaSource {
    private val logger = logger(this::class)

    private val baseUrl = baseUrl.removeSuffix("/")

    override suspend fun checkConnection(): ConnectionStatus {
        return try {
            client.get(baseUrl).run {
                check(status.isSuccess()) { "Request failed: $this" }
            }
            ConnectionStatus.SUCCESS
        } catch (e: Exception) {
            logger.error(e) { "Failed to connect to $baseUrl" }
            ConnectionStatus.FAILED
        }
    }

    private val client = createHttpClient {
        applyMediaSourceConfig(config)
        Logging {
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    this@AbstractMikanMediaSource.logger.info { message }
                }
            }
            level = LogLevel.INFO
        }
    }

    override suspend fun fetch(query: MediaFetchRequest): SizedSource<MediaMatch> =
        SingleShotPagedSource {
            val list = try {
                searchByIndexOrNull(query)
            } catch (e: Throwable) {
                logger.error(e) { "Failed to search by index for query=$query" }
                null
            } ?: searchByKeyword(query)

            list.asFlow()
        }

    private suspend fun searchByKeyword(query: MediaFetchRequest): List<MediaMatch> {
        val resp = client.get("$baseUrl/RSS/Search") {
            parameter("searchstr", query.subjectNameCN?.take(10))
        }
        return resp.bodyAsChannel().toInputStream().use {
            parseRssTopicList(Jsoup.parse(it, "UTF-8", baseUrl), query.toTopicCriteria(), allowEpMatch = false)
        }.map {
            MediaMatch(it.toOnlineMedia(mediaSourceId), MatchKind.FUZZY)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // by index
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 先搜索番剧索引, 再搜索其下资源
     */
    private suspend fun searchByIndexOrNull(request: MediaFetchRequest): List<MediaMatch>? {
        // 长度限制:
        // "无职转生Ⅱ ～到了异世界就拿出真本事～" 19 chars, 可以搜索, 再长的就会直接没有结果


        val bangumiSubjectId = request.subjectId ?: return null

        val nameCN = request.subjectNameCN ?: return null
        val subjectId =
            indexCacheProvider.getMikanSubjectId(bangumiSubjectId)
                ?: findMikanSubjectIdByName(nameCN, bangumiSubjectId)?.also {
                    indexCacheProvider.setMikanSubjectId(bangumiSubjectId, it)
                }
                ?: return null

        // https://mikanani.me/RSS/Bangumi?bangumiId=3060
        return client.get("$baseUrl/RSS/Bangumi?bangumiId=$subjectId").bodyAsChannel().toInputStream().use {
            parseRssTopicList(Jsoup.parse(it, "UTF-8", baseUrl), request.toTopicCriteria(), allowEpMatch = true)
        }.map {
            MediaMatch(it.toOnlineMedia(mediaSourceId), MatchKind.EXACT)
        }
    }

    private suspend fun findMikanSubjectIdByName(
        name: String,
        bangumiSubjectId: String,
    ): String? {
        val resp = client.get("$baseUrl/Home/Search") {
            parameter("searchstr", name.trim().substringBefore(" ").take(19))
        }
        if (!resp.status.isSuccess()) {
            logger.warn { "Failed to search by index for name '$name', resp=$resp" }
            return null
        }

        val mikanIds = resp.bodyAsChannel().toInputStream().use {
            Jsoup.parse(it, "UTF-8", baseUrl)
        }.let {
            parseMikanSubjectIdsFromSearch(it)
        }

        if (mikanIds.isEmpty()) return null

        val mikanIdToBangumiId = mikanIds.associateWith { subjectId ->
            val document = client.get("$baseUrl/Home/Bangumi/$subjectId").bodyAsChannel().toInputStream().use {
                Jsoup.parse(it, "UTF-8", baseUrl)
            }
            parseBangumiSubjectIdFromMikanSubjectDetails(document)
        }

        val target = mikanIdToBangumiId.entries.firstOrNull {
            it.value == bangumiSubjectId
        }?.key ?: return null

        return target
    }

    companion object {
        private fun parseDocument(document: Document, linkRegex: Regex): List<Topic> {
            val items = document.getElementsByTag("item")

            return items.map { element ->
                val title = element.getElementsByTag("title").text()

                val details = RawTitleParser.getDefault().parse(title, null)

                Topic(
                    topicId = element.getElementsByTag("guid").text().substringAfterLast("/"),
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
                    downloadLink = ResourceLocation.HttpTorrentFile(element.getElementsByTag("enclosure").attr("url")),
                    size = element.getElementsByTag("contentLength").text().toLongOrNull()?.bytes ?: Zero,
                    alliance = title.trim().split("]", "】").getOrNull(0).orEmpty().removePrefix("[").removePrefix("【")
                        .trim(),
                    author = null,
                    details = details.toTopicDetails(),
                    originalLink = run {
                        element.getElementsByTag("link").text().takeIf { it.isNotBlank() }?.let { return@run it }
                        // Note: It looks like Jsoup failed to parse the xml. Debug and print `element` to see details.
                        linkRegex.find(element.toString())?.value // This should work well
                    } ?: "",
                )
            }
        }

        private val linkRegex = Regex("/Home/Episode/(.+)")

        fun parseRssTopicList(
            document: Document,
            criteria: TopicCriteria,
            allowEpMatch: Boolean,
        ): List<Topic> {
            return parseDocument(document, linkRegex = linkRegex)
                .filter { criteria.matches(it, allowEpMatch = allowEpMatch) }
        }


        fun parseMikanSubjectIdsFromSearch(document: Document): List<String> {
            return document.getElementsByClass("an-info").orEmpty().mapNotNull { anInfo ->
                anInfo.parent()?.let { a ->
                    val attr = a.attr("href")
                    if (attr.isEmpty()) return@let null

                    attr.substringAfter("/Home/Bangumi/", "")
                        .takeIf { it.isNotBlank() }
                }
            }
        }

        fun parseBangumiSubjectIdFromMikanSubjectDetails(document: Document) =
            document.getElementsByClass("bangumi-info")
                .filter(predicate = {
                    it.text().contains("Bangumi番组计划链接：")
                }).firstNotNullOfOrNull { element ->
                    element.getElementsByTag("a").attr("href").substringAfter("subject/", "")
                        .takeIf { it.isNotBlank() }
                }
    }
}

private fun createHttpClient(
    clientConfig: HttpClientConfig<*>.() -> Unit = {},
) = HttpClient {
    install(HttpRequestRetry) {
        maxRetries = 1
        delayMillis { 1000 }
    }
    install(HttpCookies)
    install(HttpTimeout) {
        requestTimeoutMillis = 5000
    }
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