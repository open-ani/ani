package me.him188.ani.danmaku.server.ktor.routing

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import me.him188.ani.danmaku.protocol.BangumiLoginRequest
import me.him188.ani.danmaku.protocol.BangumiLoginResponse
import me.him188.ani.danmaku.server.service.AuthService
import me.him188.ani.danmaku.server.service.JwtTokenManager
import me.him188.ani.danmaku.server.util.exception.BadRequestException
import me.him188.ani.danmaku.server.util.exception.InvalidClientVersionException
import me.him188.ani.danmaku.server.util.exception.fromException
import org.koin.ktor.ext.inject
import java.util.Locale

fun Route.authRouting() {
    val service: AuthService by inject()
    val jwtTokenManager: JwtTokenManager by inject()

    route("/login/bangumi") {
        documentation()
        post {
            val request = call.receive<BangumiLoginRequest>()
            val os = request.clientOS?.lowercase(Locale.ENGLISH)
            val arch = request.clientArch?.lowercase(Locale.ENGLISH)
            val platform: String?
            if (os != null && arch != null) {
                if (os !in BangumiLoginRequest.AllowedOSes || arch !in BangumiLoginRequest.AllowedArchs) {
                    throw BadRequestException("Bad client platform or architecture")
                }
                platform = "$os-$arch"
            } else {
                platform = null
            }
            val userId = service.loginBangumi(
                request.bangumiToken, request.clientVersion,
                clientPlatform = platform,
            )
            val userToken = jwtTokenManager.createToken(userId)
            call.respond(BangumiLoginResponse(userToken))
        }
    }
}

private fun Route.documentation() {
    install(NotarizedRoute()) {
        post = PostInfo.builder {
            summary("使用Bangumi token登录")
            description("使用Bangumi token登录并获取用户会话token。")
            request {
                requestType<BangumiLoginRequest>()
                description("Bangumi token字符串")
                examples(
                    "" to BangumiLoginRequest(
                        bangumiToken = "VAcbHKhXqcjpCOVY5KFxwYEeQCOw4i0u",
                        clientVersion = "3.0.0-beta24",
                        clientOS = "Android",
                        clientArch = "aarch64",
                    ),
                )
            }
            response {
                responseCode(HttpStatusCode.OK)
                responseType<BangumiLoginResponse>()
                description("用户会话token字符串")
                examples("" to BangumiLoginResponse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJIZWxsbyB0aGVyZSJ9.TNpICIfOzK-BvxxV72ApTiD4SlAwvzHbu_0O3FXq-s4"))
            }
            canRespond {
                responseCode(HttpStatusCode.Unauthorized)
                responseType<Any>()
                description("Bangumi token无效")
            }
            canRespond {
                responseCode(HttpStatusCode.fromException(InvalidClientVersionException()))
                responseType<Any>()
                description("请求体中客户端版本无效")
            }
        }
    }
}