package me.him188.ani.datasources.ikaros

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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import me.him188.ani.datasources.api.paging.SinglePagePagedSource
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceKind
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

class IkarosMediaSource(
    config: MediaSourceConfig,
    indexCacheProvider: IkarosIndexCacheProvider = MemoryIkarosIndexCacheProvider(),
) : AbstractIkarosMediaSource(ID, config, "https://mikanime.tv", indexCacheProvider) {
    class Factory : MediaSourceFactory {
        override val mediaSourceId: String get() = ID
        override fun create(config: MediaSourceConfig): MediaSource =
            IkarosMediaSource(config)

        fun create(
            config: MediaSourceConfig,
            indexCacheProvider: IkarosIndexCacheProvider = MemoryIkarosIndexCacheProvider()
        ): MediaSource = IkarosMediaSource(config, indexCacheProvider)
    }

    companion object {
        const val ID = "ikaros"
        const val BASE_URL = "http://localhost:9999"
        const val USERNAME = "tomoki"
        const val PASSWORD = "tomoki"
    }

    override suspend fun fetch(query: MediaFetchRequest): SizedSource<MediaMatch> {
        TODO("Not yet implemented")
    }
}

abstract class AbstractIkarosMediaSource(
    override val mediaSourceId: String,
    private val config: MediaSourceConfig,
    baseUrl: String,
    private val indexCacheProvider: IkarosIndexCacheProvider,
) : MediaSource {
    override val kind: MediaSourceKind get() = MediaSourceKind.BitTorrent
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
                    this@AbstractIkarosMediaSource.logger.info { message }
                }
            }
            level = LogLevel.INFO
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