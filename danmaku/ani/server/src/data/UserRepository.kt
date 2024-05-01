package me.him188.ani.danmaku.server.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.him188.ani.danmaku.protocol.AniUser
import me.him188.ani.danmaku.server.data.model.UserModel

interface UserRepository {
    suspend fun getUserIdOrNull(bangumiId: Int): String?
    suspend fun addAndGetId(
        bangumiId: Int,
        nickname: String,
        smallAvatar: String,
        mediumAvatar: String,
        largeAvatar: String,
        clientVersion: String? = null,
    ): String?

    suspend fun getBangumiId(userId: String): Int?
    suspend fun getNickname(userId: String): String?
    suspend fun getSmallAvatar(userId: String): String?
    suspend fun getMediumAvatar(userId: String): String?
    suspend fun getLargeAvatar(userId: String): String?
    suspend fun getUserById(userId: String): AniUser?
    suspend fun setLastLoginTime(userId: String, time: Long): Boolean
    suspend fun setClientVersion(userId: String, clientVersion: String)
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
        largeAvatar: String,
        clientVersion: String?,
    ): String? {
        mutex.withLock {
            if (users.any { it.bangumiUserId == bangumiId }) return null
            val now = System.currentTimeMillis()
            val user = UserModel(
                bangumiUserId = bangumiId,
                nickname = nickname,
                smallAvatar = smallAvatar,
                mediumAvatar = mediumAvatar,
                largeAvatar = largeAvatar,
                registerTime = now,
                lastLoginTime = now,
                clientVersion = clientVersion,
            )
            users.add(user)
            return user.id.toString()
        }
    }

    override suspend fun getBangumiId(userId: String): Int? {
        mutex.withLock { 
            return users.find { it.id.toString() == userId }?.bangumiUserId
        }
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

    override suspend fun getUserById(userId: String): AniUser? {
        mutex.withLock {
            val user = users.find { it.id.toString() == userId } ?: return null
            val registerTime = user.registerTime ?: AniUser.MAGIC_REGISTER_TIME.also { 
                users.remove(user)
                users.add(user.copy(registerTime = it))
            }
            val lastLoginTime = user.lastLoginTime ?: System.currentTimeMillis().also {
                setLastLoginTime(userId, it)
            }
            return AniUser(
                id = user.id.toString(),
                nickname = user.nickname,
                smallAvatar = user.smallAvatar,
                mediumAvatar = user.mediumAvatar,
                largeAvatar = user.largeAvatar,
                registerTime = registerTime,
                lastLoginTime = lastLoginTime,
                clientVersion = user.clientVersion,
            )
        }
    }

    override suspend fun setLastLoginTime(userId: String, time: Long): Boolean {
        mutex.withLock {
            val user = users.find { it.id.toString() == userId } ?: return false
            users.remove(user)
            users.add(user.copy(lastLoginTime = time))
            return true
        }
    }

    override suspend fun setClientVersion(userId: String, clientVersion: String) {
        mutex.withLock {
            val user = users.find { it.id.toString() == userId } ?: return
            users.remove(user)
            users.add(user.copy(clientVersion = clientVersion))
        }
    }
}