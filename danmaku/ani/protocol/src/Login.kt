package me.him188.ani.danmaku.protocol

import kotlinx.serialization.Serializable

@Serializable
data class BangumiLoginRequest(
    val bangumiToken: String,
    val clientVersion: String? = null,
)

@Serializable
data class BangumiLoginResponse(
    val token: String,
)