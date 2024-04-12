package me.him188.ani.danmaku.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import me.him188.ani.danmaku.server.ktor.KtorServer
import org.koin.core.context.GlobalContext.startKoin

/**
 * Server entry point
 */
fun main() {
    val env = EnvironmentVariables()
    val topCoroutineScope = CoroutineScope(SupervisorJob())
    
    startKoin {
        modules(
            getServerKoinModule(env, topCoroutineScope)
        )
    }

    KtorServer.get().start(wait = true)
}