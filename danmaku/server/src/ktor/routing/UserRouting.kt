package me.him188.ani.danmaku.server.ktor.routing

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import me.him188.ani.danmaku.protocol.AniUser
import me.him188.ani.danmaku.server.service.AvatarSize
import me.him188.ani.danmaku.server.service.UserService
import me.him188.ani.danmaku.server.util.getAuthenticated
import me.him188.ani.danmaku.server.util.getUserIdOrRespond
import org.koin.ktor.ext.inject

fun Route.userRouting() {
    val service : UserService by inject()
    
    getAuthenticated("/me") {
        val userId = getUserIdOrRespond() ?: return@getAuthenticated
        val user = AniUser(
            userId,
            service.getNickname(userId),
            service.getAvatar(userId, AvatarSize.SMALL),
            service.getAvatar(userId, AvatarSize.MEDIUM),
            service.getAvatar(userId, AvatarSize.LARGE),
        )
        call.respond(user)
    }
}