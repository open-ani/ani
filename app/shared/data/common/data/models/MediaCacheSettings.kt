package me.him188.ani.app.data.models

import kotlinx.serialization.Serializable


@Serializable
data class MediaCacheSettings(
    val enabled: Boolean = false,
    val maxCountPerSubject: Int = 1,

    val mostRecentOnly: Boolean = false,
    val mostRecentCount: Int = 8,
) {
    companion object {
        val Default = MediaCacheSettings()
    }
}