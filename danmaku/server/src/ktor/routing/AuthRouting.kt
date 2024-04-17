package me.him188.ani.danmaku.server.ktor.routing

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import me.him188.ani.danmaku.server.service.AuthService
import me.him188.ani.danmaku.server.util.tryOrRespond
import org.koin.ktor.ext.inject

fun Application.authRouting() {
    val service: AuthService by inject()

    routing {
        post("/login/bangumi") {
            val bangumiToken = call.receive<String>()
            tryOrRespond {
                val userToken = service.loginBangumi(bangumiToken)
                call.respond(userToken)
            }
        }
    }
}