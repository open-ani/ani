package me.him188.ani.danmaku.server.ktor.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.callloging.processingTimeMillis
import io.ktor.server.plugins.origin
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import org.slf4j.event.Level

internal fun Application.configureCallLogging() {
    install(CallLogging) {
        mdc("requestId") {
            it.request.queryParameters["requestId"]
        }
        level = Level.INFO
        format { call ->
            val remoteAddress = call.request.origin.remoteAddress
            val remoteHost = call.request.origin.remoteHost
            when (val status = call.response.status() ?: "Unhandled") {
                HttpStatusCode.Found -> "$status: " +
                        "${call.request.toLogString()} -> ${call.response.headers[HttpHeaders.Location]} " +
                        "from $remoteAddress"

                else -> "$status: ${call.request.toLogString()} from $remoteAddress ($remoteHost)"
            }
        }
    }
}

private fun ApplicationRequest.toLogString(): String {
    return "${httpMethod.value} - ${path()} in ${call.processingTimeMillis()}ms"
}
