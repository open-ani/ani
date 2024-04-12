package me.him188.ani.danmaku.server.ktor.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import me.him188.ani.danmaku.server.service.DanmakuService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

fun Application.danmakuRouting() {
    val koin = object : KoinComponent {}
    val service: DanmakuService by koin.inject()
    
    routing {
        post("/danmaku") {
            val request = call.receive<DanmakuPostRequest>()
            val succeed = service.postDanmaku()
            if (succeed) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/danmaku") {
            val request = call.receive<DanmakuGetRequest>()
            val result = service.getDanmaku()
            call.respond(result)
        }
    }
}

@Serializable
data object DanmakuPostRequest

@Serializable
data object DanmakuGetRequest
