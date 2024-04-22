package me.him188.ani.danmaku.server.util

import java.security.SecureRandom


/**
 * Function that generates a secure random 32-byte array to be used as a secret for the [JwtConfig].
 */
fun generateSecureRandomBytes(): ByteArray {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return bytes
}