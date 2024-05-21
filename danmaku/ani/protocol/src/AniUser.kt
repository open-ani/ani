package me.him188.ani.danmaku.protocol

import kotlinx.serialization.Serializable
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Serializable
data class AniUser(
    val id: String,
    val nickname: String,
    val smallAvatar: String,
    val mediumAvatar: String,
    val largeAvatar: String,
    val registerTime: Long,
    val lastLoginTime: Long,
    val clientVersion: String? = null,
    val clientPlatforms: Set<ClientPlatform> = emptySet(),
) {
    companion object {
        val MAGIC_REGISTER_TIME = ZonedDateTime.of(
            /* year = */ 2024, /* month = */ 4, /* dayOfMonth = */ 30,
            /* hour = */ 0, /* minute = */ 0, /* second = */ 0, /* nanoOfSecond = */ 0,
            /* zone = */ ZoneOffset.UTC
        ).toInstant().toEpochMilli()
    }
}
