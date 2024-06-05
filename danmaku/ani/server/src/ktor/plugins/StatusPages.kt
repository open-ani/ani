package me.him188.ani.danmaku.server.ktor.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import me.him188.ani.danmaku.server.util.exception.HttpRequestException
import me.him188.ani.danmaku.server.util.exception.fromException

internal fun Application.configureStatuePages() {
    install(StatusPages) {
        exception<Throwable> { call, throwable ->
            when (throwable) {
                is HttpRequestException -> call.respond(
                    status = HttpStatusCode.fromException(throwable),
                    message = throwable.message ?: ""
                )
                else -> {
                    throwable.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, throwable.message ?: "")
                }
            }
        }
    }
}