package me.him188.ani.danmaku.protocol

import kotlinx.serialization.Serializable

@Serializable
data class BangumiLoginRequest(
    val bangumiToken: String,
    val clientVersion: String? = null,

    /**
     * @since 3.0.0-beta27
     */
    val clientOS: String? = null,
    /**
     * @since 3.0.0-beta27
     */
    val clientArch: String? = null,
) {
    companion object {
        val AllowedOSes = listOf(
            "windows", "macos", "android", "ios",
            "linux", "debian", "ubuntu", "redhat"
        )
        val AllowedArchs = listOf(
            "aarch64", "x86", "x86_64"
        )
    }
}

@Serializable
data class BangumiLoginResponse(
    val token: String,
)

