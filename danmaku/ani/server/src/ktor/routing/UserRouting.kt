package me.him188.ani.danmaku.server.ktor.routing

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import me.him188.ani.danmaku.protocol.AniUser
import me.him188.ani.danmaku.server.service.UserService
import me.him188.ani.danmaku.server.util.getUserIdOrRespond
import org.koin.ktor.ext.inject

fun Route.userRouting() {
    val service: UserService by inject()

    route("/me") {
        authenticate("auth-jwt") {
            documentation()
            get {
                val userId = getUserIdOrRespond() ?: return@get
                val user = service.getUser(userId)
                call.respond(user)
            }
        }
    }
}

private fun Route.documentation() {
    install(NotarizedRoute()) {
        get = GetInfo.builder {
            summary("查看当前用户信息")
            description("查看当前携带的token对应用户的信息，包含其Ani ID，Bangumi昵称以及Bangumi头像URL。")
            response {
                responseCode(HttpStatusCode.OK)
                responseType<AniUser>()
                description("用户信息")
                examples(
                    "" to AniUser(
                        id = "762e10b5-37c2-4a2b-a39b-b3033a5979f8",
                        nickname = "Him188",
                        smallAvatar = "https://example.com/avatarSmall.jpg",
                        mediumAvatar = "https://example.com/avatarMedium.jpg",
                        largeAvatar = "https://example.com/avatarLarge.jpg",
                        lastLoginTime = 1714404248957,
                    )
                )
            }
            canRespond {
                responseCode(HttpStatusCode.Unauthorized)
                responseType<Any>()
                description("未登录或用户token无效")
            }
            canRespond {
                responseCode(HttpStatusCode.NotFound)
                responseType<Any>()
                description("用户token对应的用户不存在")
            }
        }
    }
}
