package me.him188.ani.app.data.source.danmaku.protocol

import kotlinx.serialization.Serializable

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
    val clientPlatforms: Set<String> = emptySet(),
)
