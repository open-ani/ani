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
import me.him188.ani.danmaku.server.getServerKoinModule
import me.him188.ani.danmaku.server.ktor.plugins.configureRouting
import me.him188.ani.danmaku.server.ktor.plugins.configureSecurity
import me.him188.ani.danmaku.server.ktor.plugins.configureSerialization
import me.him188.ani.danmaku.server.util.exception.HttpRequestException
import org.koin.ktor.plugin.Koin


fun getKtorServer(env: EnvironmentVariables = EnvironmentVariables()): NettyApplicationEngine {
    return embeddedServer(
        Netty,
        port = env.port ?: 4394,
        host = "0.0.0.0",
        module = { serverModule(env) },
        configure = {
            this.tcpKeepAlive = true
            this.connectionGroupSize = 40
            this.workerGroupSize = 40
            this.callGroupSize = 40
        }
    )
}

internal fun Application.serverModule(env: EnvironmentVariables) {
    install(CallLogging) {
        mdc("requestId") {
            it.request.queryParameters["requestId"]
        }
        level = org.slf4j.event.Level.INFO
    }
    install(StatusPages) {
        exception<Throwable> { call, throwable ->
            when (throwable) {
                is HttpRequestException -> call.respond(HttpStatusCode(throwable.statusCode, throwable.statusMessage))
                else -> {
                    throwable.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Internal server error")                }
            }
        }
    }
    install(Koin) {
        modules(getServerKoinModule(env = env, topCoroutineScope = this@serverModule))
    }

    configureSerialization()
    configureSecurity()
    configureRouting()
}
