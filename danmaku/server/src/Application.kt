package me.him188.ani.danmaku.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import me.him188.ani.danmaku.server.ktor.getKtorServer
import org.koin.core.context.GlobalContext.startKoin

/**
 * Server entry point
 */
fun main(args: Array<String>) {
    getKtorServer(args).start(wait = true)
}