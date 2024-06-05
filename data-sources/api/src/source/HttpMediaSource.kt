package me.him188.ani.datasources.api.source

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.ContentConverter
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.decode
import io.ktor.utils.io.jvm.javaio.toInputStream
import io.ktor.utils.io.streams.asInput
import me.him188.ani.utils.ktor.createDefaultHttpClient
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.nio.charset.Charset

/**
 * 支持执行 HTTP 请求的 [MediaSource]. 封装一些便捷的操作
 */
abstract class HttpMediaSource : MediaSource {
    private val closeables = mutableListOf<AutoCloseable>()

    val logger = logger(this::class)

    fun addCloseable(closeable: AutoCloseable) {
        closeables.add(closeable)
    }

    override fun close() {
        super.close()
        this.closeables.forEach { it.close() }
    }
}

/**
 * @param bearerTokens 可便捷地为每个请求附加 bearer token: "Authorization: Bearer token"
 * @param basicAuth 可便捷地为每个请求附加 basic auth: "Authorization: Basic username:password"
 */
fun HttpMediaSource.useHttpClient(
    mediaSourceConfig: MediaSourceConfig,
    timeoutMillis: Long = 30_000,
    bearerTokens: BearerTokens? = null,
    basicAuth: BasicAuthCredentials? = null,
    clientConfig: HttpClientConfig<*>.() -> Unit = {},
): HttpClient {
    return createDefaultHttpClient {
        applyMediaSourceConfig(mediaSourceConfig)
        install(HttpTimeout) {
            requestTimeoutMillis = timeoutMillis
        }
        if (bearerTokens != null) {
            Auth {
                bearer {
                    loadTokens { bearerTokens }
                }
            }
        }
        if (basicAuth != null) {
            Auth {
                basic {
                    credentials { basicAuth }
                }
            }
        }
        Logging {
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    this@useHttpClient.logger.info { message }
                }
            }
            level = LogLevel.INFO
        }
        expectSuccess = true
        install(ContentNegotiation) {
            register(ContentType.Text.Xml, XmlConverter)
            register(ContentType.Text.Html, XmlConverter)
        }

        clientConfig()
    }.also { addCloseable(it) }
}

suspend inline fun HttpResponse.bodyAsDocument(): Document = body()

private object XmlConverter : ContentConverter {
    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel
    ): Any? {
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
}
