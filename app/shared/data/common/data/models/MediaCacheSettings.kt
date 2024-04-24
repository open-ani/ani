package me.him188.ani.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class MediaCacheSettings(
    @SerialName("enabled") // compatibility
    val autoCache: Boolean = false,
    val maxCountPerSubject: Int = 1,

    val mostRecentOnly: Boolean = false,
    val mostRecentCount: Int = 8,

    val autoDelete: Boolean = true,
) {
    companion object {
        val Default = MediaCacheSettings()
    }
}