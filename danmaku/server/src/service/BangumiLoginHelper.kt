package me.him188.ani.danmaku.server.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface BangumiLoginHelper {
    suspend fun login(bangumiToken: String): BangumiUser?
}

class TestBangumiLoginHelperImpl : BangumiLoginHelper {
    override suspend fun login(bangumiToken: String): BangumiUser? {
        return when(bangumiToken) {
            "test_token_1" -> BangumiUser(1, "test", "small", "medium", "large")
            "test_token_2" -> BangumiUser(2, "test2", "small2", "medium2", "large2")
            "test_token_3" -> BangumiUser(3, "test3", "small3", "medium3", "large3")
            else -> null
        }
    }
}

class BangumiLoginHelperImpl : BangumiLoginHelper {
    override suspend fun login(bangumiToken: String): BangumiUser? {
        return BangumiUser(1, "test", "small", "medium", "large")
    }
}

@Serializable
private data class User(
    val avatar: Avatar,
    val sign: String,
    val username: String,
    val nickname: String,
    val id: Int,
    @SerialName("user_group") val userGroup: Int,
)

@Serializable
private data class Avatar(
    val large: String,
    val medium: String,
    val small: String,
)

data class BangumiUser(
    val id: Int,
    val nickname: String,
    val smallAvatar: String,
    val mediumAvatar: String,
    val largeAvatar: String,
)

