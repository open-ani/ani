package me.him188.ani.app.session

import kotlin.time.Duration.Companion.hours

/**
 * Bangumi 账号信息.
 */
data class Session(
    val accessToken: String,
    val expiresAtMillis: Long,
)

fun Session.isValid() = !isExpired()
fun Session.isExpired() = expiresAtMillis <= System.currentTimeMillis() + 1.hours.inWholeMilliseconds
