package me.him188.ani.danmaku.server.ktor.plugins

import io.bkbn.kompendium.core.plugin.NotarizedApplication
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.info.Info
import io.bkbn.kompendium.oas.server.Server
import io.ktor.server.application.Application
import io.ktor.server.application.install
import java.net.URI

fun Application.configureNotarizedApplication() {
    install(NotarizedApplication()) {
        spec = OpenApiSpec(
            info = Info(
                title = "Ani",
                version = "1.0.0",
                description = "Ani API"
            ),
            servers = mutableListOf(Server(URI("https://danmaku.api.myani.org/"))),
        )
    }
}