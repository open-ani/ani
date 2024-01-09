package me.him188.ani.datasources.bangumi.models

class BangumiAccessToken(
    val username: String,
    val accessToken: String,
    val expiresIn: Long,
)

class BangumiRefreshToken(
    val value: String,
)