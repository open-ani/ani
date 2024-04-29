package me.him188.ani.danmaku.server.data.mongodb

import kotlinx.coroutines.flow.firstOrNull
import me.him188.ani.danmaku.protocol.AniUser
import me.him188.ani.danmaku.server.data.UserRepository
import me.him188.ani.danmaku.server.data.model.UserModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class MongoUserRepositoryImpl : UserRepository, KoinComponent {
    private val mongoCollectionProvider: MongoCollectionProvider by inject()
    private val userTable = mongoCollectionProvider.userTable

    override suspend fun getUserIdOrNull(bangumiId: Int): String? {
        return userTable.find(
            Field("bangumiUserId") eq bangumiId
        ).firstOrNull()?.id?.toString()
    }

    override suspend fun addAndGetId(
        bangumiId: Int,
        nickname: String,
        smallAvatar: String,
        mediumAvatar: String,
        largeAvatar: String
    ): String? {
        val user = UserModel(
            bangumiUserId = bangumiId,
            nickname = nickname,
            smallAvatar = smallAvatar,
            mediumAvatar = mediumAvatar,
            largeAvatar = largeAvatar,
            lastLoginTime = System.currentTimeMillis(),
        )
        return if (userTable.insertOne(user).wasAcknowledged()) {
            user.id.toString()
        } else {
            null
        }
    }

    override suspend fun getBangumiId(userId: String): Int? {
        return userTable.find(
            Field.Id eq UUID.fromString(userId)
        ).firstOrNull()?.bangumiUserId
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
        val user = userTable.find(
            Field.Id eq UUID.fromString(userId)
        ).firstOrNull() ?: return null
        val lastLoginTime = user.lastLoginTime ?: System.currentTimeMillis().also {
            setLastLoginTime(userId, it)
        }
        return AniUser(
            user.id.toString(),
            user.nickname,
            user.smallAvatar,
            user.mediumAvatar,
            user.largeAvatar,
            lastLoginTime,
        )
    }

    override suspend fun setLastLoginTime(userId: String, time: Long): Boolean {
        return userTable.updateOne(
            Field.Id eq UUID.fromString(userId),
            Field(UserModel::lastLoginTime) setTo time
        ).wasAcknowledged()
    }
}