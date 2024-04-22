package me.him188.ani.danmaku.server

import me.him188.ani.danmaku.server.ktor.getKtorServer

/**
 * Server entry point
 */
fun main(args: Array<String>) {
    getKtorServer(args).start(wait = true)
}