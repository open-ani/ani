package me.him188.ani.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DanmakuSettings(
    val useGlobal: Boolean = false,
) {
    companion object {
        val Default = DanmakuSettings()
    }
}