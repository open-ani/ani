package me.him188.ani.danmaku.ui

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class DanmakuRegexFilter(
    val id: String,
    val name: String = "",
    val regex: String = "",
    val enabled: Boolean = true
) {
    companion object {
        @Stable
        val Default = DanmakuRegexFilter(UUID.randomUUID().toString())
    }
}

