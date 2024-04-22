package me.him188.ani.danmaku.server.service

import io.ktor.server.plugins.NotFoundException
import me.him188.ani.danmaku.protocol.AniUser
import me.him188.ani.danmaku.server.data.UserRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface UserService {
    suspend fun getBangumiId(userId: String): Int
    suspend fun getNickname(userId: String): String
    suspend fun getAvatar(userId: String, size: AvatarSize): String
    suspend fun getUser(userId: String): AniUser
}

class UserServiceImpl : UserService, KoinComponent {
    private val userRepository: UserRepository by inject()

    override suspend fun getBangumiId(userId: String): Int {
        return userRepository.getBangumiId(userId) ?: throw NotFoundException()
    }

    override suspend fun getNickname(userId: String): String {
        return userRepository.getNickname(userId) ?: throw NotFoundException()
    }

    override suspend fun getAvatar(userId: String, size: AvatarSize): String {
        return when (size) {
            AvatarSize.SMALL -> userRepository.getSmallAvatar(userId)
            AvatarSize.MEDIUM -> userRepository.getMediumAvatar(userId)
            AvatarSize.LARGE -> userRepository.getLargeAvatar(userId)
        } ?: throw NotFoundException()
    }
    
    override suspend fun getUser(userId: String): AniUser {
        val user = userRepository.getUserById(userId) ?: throw NotFoundException()
        return AniUser(
            user.id.toString(),
            user.nickname,
            user.smallAvatar,
            user.mediumAvatar,
            user.largeAvatar
        )
    }
}

enum class AvatarSize {
    SMALL, MEDIUM, LARGE
}