package me.him188.ani.danmaku.ui

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import me.him188.ani.utils.platform.Uuid

@Serializable
data class DanmakuRegexFilter(
    val id: String,
    val name: String = "",
    val regex: String = "",
    val enabled: Boolean = true
) {
    companion object {
        @Stable
        val Default = DanmakuRegexFilter(Uuid.randomString())
    }
}

