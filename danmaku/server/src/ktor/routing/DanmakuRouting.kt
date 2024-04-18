package me.him188.ani.danmaku.server.ktor.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import me.him188.ani.danmaku.protocol.DanmakuGetResponse
import me.him188.ani.danmaku.protocol.DanmakuPostRequest
import me.him188.ani.danmaku.server.service.DanmakuService
import me.him188.ani.danmaku.server.util.exception.BadRequestException
import me.him188.ani.danmaku.server.util.getUserIdOrRespond
import me.him188.ani.danmaku.server.util.postAuthenticated
import org.koin.ktor.ext.inject

fun Route.danmakuRouting() {
    val service: DanmakuService by inject()

    postAuthenticated("/danmaku/{episodeId}") {
        val userId = getUserIdOrRespond() ?: return@postAuthenticated
        val request = call.receive<DanmakuPostRequest>()
        val episodeId = call.parameters["episodeId"] ?: throw BadRequestException()
        service.postDanmaku(episodeId, request.danmakuInfo, userId)
        call.respond(HttpStatusCode.OK)
    }

    get("/danmaku/{episodeId}") {
        val maxCount = call.request.queryParameters["maxCount"]?.toIntOrNull()
        val fromTime = call.request.queryParameters["fromTime"]?.toLongOrNull()
        val toTime = call.request.queryParameters["toTime"]?.toLongOrNull()
        val episodeId = call.parameters["episodeId"] ?: throw BadRequestException()
        val result = service.getDanmaku(episodeId, maxCount, fromTime, toTime)
        call.respond(DanmakuGetResponse(result))
    }
}