package me.him188.ani.danmaku.server.ktor.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json

internal val ServerJson = Json {
    ignoreUnknownKeys = true
}

internal fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(ServerJson)
    }
}
