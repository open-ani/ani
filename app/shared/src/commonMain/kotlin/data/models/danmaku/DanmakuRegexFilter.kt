package me.him188.ani.app.data.models.danmaku

import kotlinx.serialization.Serializable

@Serializable
data class DanmakuRegexFilter(
    val id: String,
    val name: String = "",
    val regex: String = "",
    val enabled: Boolean = true
)
