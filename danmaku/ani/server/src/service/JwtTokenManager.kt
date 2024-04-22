package me.him188.ani.danmaku.server.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import me.him188.ani.danmaku.server.ServerConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Date

interface JwtTokenManager {
    fun createToken(userId: String): String
    fun getTokenVerifier(): JWTVerifier
}

class JwtTokenManagerImpl : JwtTokenManager, KoinComponent {
    private val config: ServerConfig by inject()
    override fun createToken(userId: String): String {
        return JWT.create()
            .withAudience(config.jwt.audience)
            .withIssuer(config.jwt.issuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + config.jwt.expiration))
            .sign(Algorithm.HMAC256(config.jwt.secret))
    }

    override fun getTokenVerifier(): JWTVerifier {
        return JWT
            .require(Algorithm.HMAC256(config.jwt.secret))
            .withAudience(config.jwt.audience)
            .withIssuer(config.jwt.issuer)
            .build()
    }
}