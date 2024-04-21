package me.him188.ani.danmaku.server.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.him188.ani.danmaku.server.data.model.UserModel

interface UserRepository {
    suspend fun getUserIdOrNull(bangumiId: Int): String?
    suspend fun addAndGetId(
        bangumiId: Int,
        nickname: String,
        smallAvatar: String,
        mediumAvatar: String,
        largeAvatar: String
    ): String?

    suspend fun getBangumiId(userId: String): Int?
    suspend fun getNickname(userId: String): String?
    suspend fun getSmallAvatar(userId: String): String?
    suspend fun getMediumAvatar(userId: String): String?
    suspend fun getLargeAvatar(userId: String): String?
    suspend fun getUserById(userId: String): UserModel?
}

class InMemoryUserRepositoryImpl : UserRepository {
    private val users = mutableListOf<UserModel>()
    private val mutex = Mutex()

    override suspend fun getUserIdOrNull(bangumiId: Int): String? {
        mutex.withLock {
            return users.find { it.bangumiUserId == bangumiId }?.id?.toString()
        }
    }

    override suspend fun addAndGetId(
        bangumiId: Int,
        nickname: String,
        smallAvatar: String,
        mediumAvatar: String,
        largeAvatar: String
    ): String? {
        mutex.withLock {
            if (users.any { it.bangumiUserId == bangumiId }) return null
            val user = UserModel(
                bangumiUserId = bangumiId,
                nickname = nickname,
                smallAvatar = smallAvatar,
                mediumAvatar = mediumAvatar,
                largeAvatar = largeAvatar
            )
            users.add(user)
            return user.id.toString()
        }
    }

    override suspend fun getBangumiId(userId: String): Int? {
        return getUserById(userId)?.bangumiUserId
    }

    override suspend fun getNickname(userId: String): String? {
        return getUserById(userId)?.nickname
    }

    override suspend fun getSmallAvatar(userId: String): String? {
        return getUserById(userId)?.smallAvatar
    }

    override suspend fun getMediumAvatar(userId: String): String? {
        return getUserById(userId)?.mediumAvatar
    }

    override suspend fun getLargeAvatar(userId: String): String? {
        return getUserById(userId)?.largeAvatar
    }

    override suspend fun getUserById(userId: String): UserModel? {
        mutex.withLock {
            return users.find { it.id.toString() == userId }
        }
    }
}