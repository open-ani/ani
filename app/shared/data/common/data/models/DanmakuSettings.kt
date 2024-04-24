package me.him188.ani.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DanmakuSettings(
    val useGlobal: Boolean = false,

    @Suppress("PropertyName")
    val _placeholder: Int = 0,
) {
    companion object {
        val Default = DanmakuSettings()
    }
}