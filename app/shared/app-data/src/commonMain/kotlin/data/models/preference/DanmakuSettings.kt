package me.him188.ani.app.data.models.preference

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class DanmakuSettings(
    val useGlobal: Boolean = false,

    @Suppress("PropertyName")
    @Transient val _placeholder: Int = 0,
) {
    companion object {
        val Default = DanmakuSettings()
    }
}