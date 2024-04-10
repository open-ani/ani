package me.him188.ani.danmaku.server.ktor.plugins

import io.ktor.server.application.Application
import io.ktor.server.auth.authentication

fun Application.configureSecurity() {
    authentication {
    }
}
