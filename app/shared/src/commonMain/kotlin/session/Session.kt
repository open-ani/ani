package me.him188.ani.app.session

data class Session(
    val userId: Long,
    val accessToken: String,
    val expiresIn: Long,
)