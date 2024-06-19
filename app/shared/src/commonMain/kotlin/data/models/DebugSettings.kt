package me.him188.ani.app.data.models

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Immutable
data class DebugSettings(
    val enabled: Boolean = false,
    @Suppress("PropertyName") @Transient val _placeHolder: Int = 0,
) {
    companion object {
        @Stable
        val Default = DebugSettings()
    }
}