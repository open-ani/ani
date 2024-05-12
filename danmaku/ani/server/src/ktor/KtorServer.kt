@file:JvmName("ApplicationKt")

package me.him188.ani.danmaku.server.ktor

import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import me.him188.ani.danmaku.server.ServerConfig
import me.him188.ani.danmaku.server.ServerConfigBuilder
import me.him188.ani.danmaku.server.ktor.plugins.configureAutoHeadResponse
import me.him188.ani.danmaku.server.ktor.plugins.configureCallLogging
import me.him188.ani.danmaku.server.ktor.plugins.configureKoin
import me.him188.ani.danmaku.server.ktor.plugins.configureNotarizedApplication
import me.him188.ani.danmaku.server.ktor.plugins.configureRouting
import me.him188.ani.danmaku.server.ktor.plugins.configureSecurity
import me.him188.ani.danmaku.server.ktor.plugins.configureSerialization
import me.him188.ani.danmaku.server.ktor.plugins.configureStatuePages
import me.him188.ani.danmaku.server.ktor.plugins.configureSwagger


fun getKtorServer(
    args: Array<String> = arrayOf(),
    configs: ServerConfigBuilder.() -> Unit = {},
): NettyApplicationEngine {
    val config = ServerConfigBuilder.create(args, configs).build()
    return getKtorServer(config)
}

fun getKtorServer(
    config: ServerConfig,
): NettyApplicationEngine {
    return embeddedServer(
        Netty,
        port = config.port,
        host = config.host,
        module = { serverModule(config) },
        configure = {
            this.tcpKeepAlive = true
            this.connectionGroupSize = 40
            this.workerGroupSize = 40
            this.callGroupSize = 40
        }
    )
}

internal fun Application.serverModule(config: ServerConfig) {
    log.info("Starting server in ${if (config.testing) "testing" else "deployment"} mode")
    configureKoin(config)
    configureCallLogging()
    configureStatuePages()
    configureSerialization()
    configureSecurity()
    configureNotarizedApplication()
    configureSwagger()
    
    configureAutoHeadResponse()
    configureRouting()
}

