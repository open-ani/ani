package me.him188.ani.danmaku.server.ktor.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/status") {
            call.respondText("Server is running 2")
        }
    }
}
