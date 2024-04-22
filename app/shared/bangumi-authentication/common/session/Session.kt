package me.him188.ani.app.session

/**
 * Bangumi 账号信息.
 */
data class Session(
//    val userId: Long,
    val accessToken: String,
    val expiresAt: Long,
)