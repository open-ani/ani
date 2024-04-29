package me.him188.ani.danmaku.server.ktor.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.callloging.processingTimeMillis
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
            """${call.response.status()}: ${call.request.toLogString()}. Request details:
    Headers:
        ${call.request.headers.entries().joinToString("\n        ") { (key, value) -> "$key=$value" }}
    Queries:
        ${call.request.queryParameters.entries().joinToString("\n        ") { (key, value) -> "$key=$value" }}
            """
        }
    }
}

private fun ApplicationRequest.toLogString(): String {
    return "${httpMethod.value} - ${path()} in ${call.processingTimeMillis()}ms"
}
