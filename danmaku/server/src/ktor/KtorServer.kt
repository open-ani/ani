@file:JvmName("ApplicationKt")

package me.him188.ani.danmaku.server.ktor

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import me.him188.ani.danmaku.server.EnvironmentVariables
import me.him188.ani.danmaku.server.ktor.plugins.configureRouting
import me.him188.ani.danmaku.server.ktor.plugins.configureSecurity
import me.him188.ani.danmaku.server.ktor.plugins.configureSerialization


internal fun getServer(
    env: EnvironmentVariables = EnvironmentVariables(),
): NettyApplicationEngine {
    return embeddedServer(
        Netty,
        port = env.port ?: 4392,
        host = "0.0.0.0",
        module = { module() },
        configure = {
            this.tcpKeepAlive = true
            this.connectionGroupSize = 40
            this.workerGroupSize = 40
            this.callGroupSize = 40
        }
    )
}

private fun Application.module() {
    install(CallLogging) {
        mdc("requestId") {
            it.request.queryParameters["requestId"]
        }
        level = org.slf4j.event.Level.INFO
    }
    install(StatusPages) {
        exception<Throwable> { call, throwable ->
            throwable.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Internal server error")
        }
    }

    configureSecurity()
    configureSerialization()
    configureRouting()
}
