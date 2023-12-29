/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.api.impl

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.jvm.javaio.*
import io.ktor.utils.io.streams.*
import kotlinx.serialization.json.Json
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.impl.protocol.Network
import me.him188.animationgarden.api.model.SearchQuery
import me.him188.animationgarden.api.model.SearchSession
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.time.Duration.Companion.seconds

internal class AnimationGardenClientImpl(
    engineConfig: HttpClientEngineConfig.() -> Unit,
) : AnimationGardenClient {
    private val network: Network = Network(createHttpClient(engineConfig))


    override fun startSearchSession(filter: SearchQuery): SearchSession {
        return SearchSessionImpl(filter, network)
    }
}

fun createHttpClient(
    engineConfig: HttpClientEngineConfig.() -> Unit = {},
    clientConfig: HttpClientConfig<CIOEngineConfig>.() -> Unit = {},
) = HttpClient(CIO) {
    engine {
        engineConfig()
    }
    install(HttpRequestRetry) {
        maxRetries = 3
        delayMillis { 1000 }
    }
    install(WebSockets) {
        pingInterval = 20.seconds.inWholeMilliseconds
    }
    install(HttpCookies)
    install(HttpTimeout)
    clientConfig()
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
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