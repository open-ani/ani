package me.him188.animationgarden.api.impl

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.jvm.javaio.*
import io.ktor.utils.io.streams.*
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.impl.protocol.Network
import me.him188.animationgarden.api.model.SearchQuery
import me.him188.animationgarden.api.model.SearchSession
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

internal class AnimationGardenClientImpl : AnimationGardenClient {
    private val network: Network = Network(createHttpClient())


    override fun startSearchSession(filter: SearchQuery): SearchSession {
        return SearchSessionImpl(filter, network)
    }
}

internal fun createHttpClient() = HttpClient(CIO) {
    engine {
        proxy = ProxyBuilder.http("http://localhost:7890")
    }
    install(HttpRequestRetry) {
        maxRetries = 3
        delayMillis { 1000 }
    }
    install(HttpCookies)
    install(HttpTimeout)
    install(Logging) {
        logger = object : Logger {
            private val delegate = LoggerFactory.getLogger(Network::class.java)
            private val marker = MarkerFactory.getMarker("HTTP")
            override fun log(message: String) {
                delegate.trace(marker, message)
            }
        }
        level = LogLevel.ALL
    }
    install(ContentNegotiation) {
        register(
            ContentType.Text.Html,
            object : ContentConverter {
                override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: ByteReadChannel): Any? {
                    if (typeInfo.type.qualifiedName != Document::class.qualifiedName) return null
                    content.awaitContent()
                    val decoder = Charsets.UTF_8.newDecoder()
                    val string = decoder.decode(content.toInputStream().asInput())
                    return Jsoup.parse(string, charset.name())
                }

                override suspend fun serialize(
                    contentType: ContentType,
                    charset: Charset,
                    typeInfo: TypeInfo,
                    value: Any
                ): OutgoingContent? {
                    return null
//                    if (contentType != ContentType.Application.Xml && contentType != ContentType.Text.Xml) return null
//                    if (typeInfo.type.qualifiedName != Document::class.qualifiedName) return null
//                    if (value !is String) return null
//                    Jsoup.parse(value, charset.name())
                }
            },
        ) {}
    }
}