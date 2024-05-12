package me.him188.ani.danmaku.server.ktor.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.autohead.AutoHeadResponse


fun Application.configureAutoHeadResponse() {
    install(AutoHeadResponse)
}