package me.him188.ani.danmaku.server.ktor.routing

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import me.him188.ani.danmaku.server.service.AuthService
import me.him188.ani.danmaku.server.service.JwtTokenManager
import me.him188.ani.danmaku.server.util.tryOrRespond
import org.koin.core.component.inject
import org.koin.ktor.ext.inject

fun Route.authRouting() {
    val service: AuthService by inject()
    val jwtTokenManager: JwtTokenManager by inject()

    post("/login/bangumi") {
        val bangumiToken = call.receive<String>()
        tryOrRespond {
            val userId = service.loginBangumi(bangumiToken)
            val userToken = jwtTokenManager.createToken(userId)
            call.respond(userToken)
        }
    }
}