package me.him188.ani.app.data.models.preference

import kotlinx.serialization.Serializable

@Serializable
data class ProfileSettings(
    val loginAsGuest: Boolean = false,
) {
    companion object {
        val Default = ProfileSettings()
    }
}
