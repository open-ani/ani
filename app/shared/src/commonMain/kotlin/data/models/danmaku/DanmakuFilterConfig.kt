package me.him188.ani.app.data.models.danmaku

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.utils.platform.annotations.SerializationOnly

/**
 * Configuration for danmaku filters.
 */
@Immutable
@Serializable
data class DanmakuFilterConfig @SerializationOnly constructor(
    val danmakuRegexFilterEnabled: Boolean = true,
    @Suppress("PropertyName") @Transient val _placeholder: Int = 0
) {
    companion object {
        @OptIn(SerializationOnly::class)
        @Stable
        val Default = DanmakuFilterConfig()
    }
}
