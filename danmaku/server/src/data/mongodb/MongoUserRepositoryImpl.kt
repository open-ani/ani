package me.him188.ani.danmaku.server.data.mongodb

import kotlinx.coroutines.flow.firstOrNull
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
            Field("bangumiId") eq bangumiId
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
            largeAvatar = largeAvatar
        )
        return if (userTable.insertOne(user).wasAcknowledged()) {
            user.id.toString()
        } else {
            null
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
        return userTable.find(
            Field("_id") eq UUID.fromString(userId)
        ).firstOrNull()
    }
}