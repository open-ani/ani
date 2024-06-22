package me.him188.ani.danmaku.server.ktor.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import me.him188.ani.danmaku.server.ServerConfig
import me.him188.ani.danmaku.server.getServerKoinModule
import org.koin.ktor.plugin.Koin


internal fun Application.configureKoin(config: ServerConfig) {
    install(Koin) {
        modules(
            getServerKoinModule(
                config = config,
                topCoroutineScope = this@configureKoin,
                logger = log,
            ),
        )
    }
}