package me.him188.ani.danmaku.server.util

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import me.him188.ani.danmaku.server.util.exception.HttpRequestException

suspend inline fun PipelineContext<*, ApplicationCall>.tryOrRespond(
    block: () -> Unit
) {
    try {
        block()
    } catch (e: HttpRequestException) {
        call.respond(HttpStatusCode(e.statusCode, e.statusMessage))
    } catch (e: Exception) {
        throw e
    }
}