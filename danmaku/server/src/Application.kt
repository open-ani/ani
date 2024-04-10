package me.him188.ani.danmaku.server

import me.him188.ani.danmaku.server.ktor.getServer

/**
 * Server entry point
 */
fun main() {
    getServer().start(wait = true)
}