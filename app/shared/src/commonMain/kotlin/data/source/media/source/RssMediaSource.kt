package me.him188.ani.app.data.source.media.source

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable
import me.him188.ani.app.data.models.ApiFailure
import me.him188.ani.app.data.models.fold
import me.him188.ani.app.data.models.runApiRequest
import me.him188.ani.app.tools.rss.RssParser
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.DownloadSearchQuery
import me.him188.ani.datasources.api.source.FactoryId
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceInfo
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.TopicMediaSource
import me.him188.ani.datasources.api.source.deserializeArgumentsOrNull
import me.him188.ani.datasources.api.source.useHttpClient
import me.him188.ani.datasources.api.topic.FileSize.Companion.Unspecified
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.Topic
import me.him188.ani.datasources.api.topic.TopicCategory
import me.him188.ani.datasources.api.topic.matches
import me.him188.ani.datasources.api.topic.titles.RawTitleParser
import me.him188.ani.datasources.api.topic.titles.parse
import me.him188.ani.datasources.api.topic.titles.toTopicDetails
import me.him188.ani.utils.ktor.toSource
import me.him188.ani.utils.xml.Xml

@Serializable
class RssMediaSourceArguments(
    val name: String,
    val description: String,
    val searchUrl: String,
    val iconUrl: String,
) {
    companion object {
        val Default = RssMediaSourceArguments(
            name = "RSS",
            description = "",
            searchUrl = "",
            iconUrl = "https://rss.com/blog/wp-content/uploads/2019/10/social_style_3_rss-512-1.png",
        )
    }
}

class RssMediaSource(
    override val mediaSourceId: String,
    config: MediaSourceConfig,
    override val kind: MediaSourceKind = MediaSourceKind.BitTorrent,
) : TopicMediaSource() {
    companion object {
        val FactoryId = FactoryId("rss")
    }

//    object Parameters : MediaSourceParametersBuilder() {
//        val name = string(
//            "name",
//            defaultProvider = { "RSS" },
//            description = "设置显示在列表中的名称",
//        )
//        val description = string(
//            "description",
//            defaultProvider = { "" },
//            description = "可留空",
//        )
//        val searchUrl = string(
//            "searchUrl",
//            description = """
//                替换规则:
//                {keyword} 替换为条目 (番剧) 名称
//                {page} 替换为页码, 如果不需要分页则忽略
//            """.trimIndent(),
//            placeholder = "例如  https://acg.rip/page/{page}.xml?term={keyword}",
//        )
//        val iconUrl = string("iconUrl")
//    }

    private val arguments =
        config.deserializeArgumentsOrNull(RssMediaSourceArguments.serializer()) ?: RssMediaSourceArguments.Default

//    private val name = config[Parameters.name]
//    private val description = config[Parameters.description]
//    private val searchUrl: String = config[Parameters.searchUrl]
//    private val iconUrl: String = config[Parameters.iconUrl]

    private val usePaging = arguments.searchUrl.contains("{page}")

    private val client by lazy { useHttpClient(config) }

    class Factory : MediaSourceFactory {
        override val factoryId: FactoryId get() = FactoryId

        override val info: MediaSourceInfo = MediaSourceInfo(
            displayName = "RSS",
            description = "通用 RSS BT 数据源",
            // https://rss.com/blog/free-rss-icon/
            iconUrl = "https://rss.com/blog/wp-content/uploads/2019/10/social_style_3_rss-512-1.png",
        )

        override val allowMultipleInstances: Boolean get() = true
        override fun create(mediaSourceId: String, config: MediaSourceConfig): MediaSource =
            RssMediaSource(mediaSourceId, config)
    }

    override suspend fun checkConnection(): ConnectionStatus {
        return kotlin.runCatching {
            runApiRequest {
                client.get(arguments.searchUrl) // 提交一个请求, 只要它不是因为网络错误就行
            }.fold(
                onSuccess = { ConnectionStatus.SUCCESS },
                onKnownFailure = {
                    when (it) {
                        ApiFailure.NetworkError -> ConnectionStatus.FAILED
                        ApiFailure.ServiceUnavailable -> ConnectionStatus.FAILED
                        ApiFailure.Unauthorized -> ConnectionStatus.SUCCESS
                    }
                },
            )
        }.recover {
            // 只要不是网络错误就行
            ConnectionStatus.SUCCESS
        }.getOrThrow()
    }

    override val info: MediaSourceInfo = MediaSourceInfo(
        displayName = arguments.name,
        description = arguments.description,
        websiteUrl = arguments.searchUrl,
        iconUrl = arguments.iconUrl,
    )

    // https://garden.breadio.wiki/feed.xml?filter=[{"search":["樱trick"]}]
    // https://acg.rip/page/2.xml?term=%E9%AD%94%E6%B3%95%E5%B0%91%E5%A5%B3
    override suspend fun startSearch(query: DownloadSearchQuery): SizedSource<Topic> {
        return PageBasedPagedSource { page ->
            if (!usePaging && page != 0) return@PageBasedPagedSource null

            val document = try {
                client.get(
                    arguments.searchUrl
                        .replace("{keyword}", encodeUrl(query))
                        .replace("{page}", page.toString()),
                ).let { resp ->
                    Xml.parse(resp.bodyAsChannel().toSource())
                }
            } catch (e: ClientRequestException) {
                if (e.response.status == HttpStatusCode.NotFound) {
                    // 404 Not Found
                    return@PageBasedPagedSource null
                }
                throw e
            }

            val channel = RssParser.parse(document)

            Paged(
                null,
                hasMore = channel.items.isNotEmpty(),
                page = channel.items.mapNotNull { item ->
                    val downloadLink = when {
                        item.enclosure != null -> item.enclosure.url
                        item.link.isNotBlank() -> item.link
                        else -> return@mapNotNull null
                    }
                    val size = when {
                        item.enclosure != null && item.enclosure.length > 1 // 有的源会返回 1
                        -> item.enclosure.length.bytes

                        else -> Unspecified
                    }

                    val details = RawTitleParser.getDefault().parse(item.title, null)

                    Topic(
                        topicId = item.guid,
                        publishedTimeMillis = item.pubDate?.toInstant(TimeZone.currentSystemDefault())
                            ?.toEpochMilliseconds(),
                        category = TopicCategory.ANIME,
                        rawTitle = item.title,
                        commentsCount = 0,
                        downloadLink = guessResourceLocation(downloadLink),
                        size = size,
                        alliance = item.title.trim().split("]", "】").getOrNull(0).orEmpty().removePrefix("[")
                            .removePrefix("【").trim(),
                        author = null,
                        details = details.toTopicDetails(),
                        originalLink = item.link.takeIf { it.isNotBlank() } ?: item.guid,
                    ).takeIf { query.matches(it, allowEpMatch = false) }
                },
            )
        }
    }

    private fun encodeUrl(query: DownloadSearchQuery) =
        URLBuilder().appendPathSegments(query.keywords).encodedPathSegments.first()

    private fun guessResourceLocation(url: String): ResourceLocation {
        return if (url.startsWith("magnet:")) {
            ResourceLocation.MagnetLink(url)
        } else {
            ResourceLocation.HttpTorrentFile(url)
        }
    }
}

// ACG.RIP

// https://acg.rip/page/1.xml?term=%E9%AD%94%E6%B3%95%E5%B0%91%E5%A5%B3
// https://acg.rip/page/2.xml?term=%E9%AD%94%E6%B3%95%E5%B0%91%E5%A5%B3
/*
<rss version="2.0">
<channel>
<title>ACG.RIP</title>
<description>ACG.RIP has super cow power</description>
<link>https://acg.rip/page/2.xml?term=%E9%AD%94%E6%B3%95%E5%B0%91%E5%A5%B3</link>
<ttl>1800</ttl>
<item>
<title>[Up to 21°C] 曾經、魔法少女和邪惡相互為敵。 / Katsute Mahou Shoujo to Aku wa Tekitai shiteita. - 04 (CR 1920x1080 AVC AAC MKV)</title>
<description><img src="https://img1.ak.crunchyroll.com/i/spire4-tmb/f89a681e17239531f343fcf57d59096f1722348663_full.jpg" alt="" /><br /> <br /> [h3]<strong><em><em>Katsute Mahou Shoujo to Aku wa Tekitai shitei...</description>
<pubDate>Tue, 30 Jul 2024 07:47:41 -0700</pubDate>
<link>https://acg.rip/t/309582</link>
<guid>https://acg.rip/t/309582</guid>
<enclosure url="https://acg.rip/t/309582.torrent" type="application/x-bittorrent"/>
</item>
 */


// https://garden.breadio.wiki/resources/1?search=%5B%22%E6%A8%B1trick%22%5D

/// https://garden.breadio.wiki/feed.xml?filter=%255B%257B%2522search%2522%253A%255B%2522%25E6%25A8%25B1trick%2522%255D%257D%255D

/*
<rss version="2.0">
<channel>
<title>樱trick</title>
<description>Anime Garden 是動漫花園資源網的第三方镜像站, 動漫花園資訊網是一個動漫愛好者交流的平台,提供最及時,最全面的動畫,漫畫,動漫音樂,動漫下載,BT,ED,動漫遊戲,資訊,分享,交流,讨论.</description>
<link>https://garden.breadio.wiki/resources?page=1&pageSize=100&search=%5B%22%E6%A8%B1trick%22%5D</link>
<item>
<title>[愛戀&漫猫字幕社]櫻Trick Sakura Trick 01-12 avc_flac mkv 繁體內嵌合集(急招時軸)</title>
<link>https://garden.breadio.wiki/detail/moe/6558436a88897300074bfd42</link>
<guid isPermaLink="true">https://garden.breadio.wiki/detail/moe/6558436a88897300074bfd42</guid>
<pubDate>Sat, 18 Nov 2023 04:54:02 GMT</pubDate>
<enclosure url="magnet:?xt=urn:btih:d22868eee2dae4214476ac865e0b6ec533e09e57" length="0" type="application/x-bittorrent"/>
</item>
 */