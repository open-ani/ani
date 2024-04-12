package me.him188.ani.danmaku.server.ktor.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import me.him188.ani.danmaku.protocol.DanmakuGetRequest
import me.him188.ani.danmaku.protocol.DanmakuGetResponse
import me.him188.ani.danmaku.protocol.DanmakuPostRequest
import me.him188.ani.danmaku.server.service.DanmakuService
import me.him188.ani.danmaku.server.util.tryOrRespond
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

fun Application.danmakuRouting() {
    val koin = object : KoinComponent {}
    val service: DanmakuService by koin.inject()

    routing {
        post("/danmaku") {
            val userId = UUID.randomUUID().toString() // TODO: Implement user authentication
            val request = call.receive<DanmakuPostRequest>()
            tryOrRespond {
                service.postDanmaku(request.episodeId, request.danmakuInfo, userId)
                call.respond(HttpStatusCode.OK)
            }
        }

        get("/danmaku") {
            val request = call.receive<DanmakuGetRequest>()
            tryOrRespond {
                val result = service.getDanmaku(request.episodeId, request.maxCount, request.fromTime, request.toTime)
                call.respond(DanmakuGetResponse(result))
            }
        }
    }
}
