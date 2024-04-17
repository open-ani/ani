package me.him188.ani.danmaku.server.ktor.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import me.him188.ani.danmaku.server.ServerConfig
import me.him188.ani.danmaku.server.service.JwtTokenManager
import org.koin.ktor.ext.inject

internal fun Application.configureSecurity() {
    val jwtTokenManager: JwtTokenManager by inject()
    val config : ServerConfig by inject()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = config.jwt.realm
            verifier(jwtTokenManager.getTokenVerifier())
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}
