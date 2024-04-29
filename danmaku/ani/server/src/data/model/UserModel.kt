package me.him188.ani.danmaku.server.data.model

import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID

data class UserModel(
    @BsonId
    val id: UUID = UUID.randomUUID(),
    val bangumiUserId: Int, // unique index
    val nickname: String,
    val smallAvatar: String,
    val mediumAvatar: String,
    val largeAvatar: String,
    val lastLoginTime: Long? = null,
    val clientVersion: String? = null,
)