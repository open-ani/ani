package me.him188.ani.danmaku.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Configuration for [Danmaku] filters.
 */
@Immutable
@Serializable
data class DanmakuFilterConfig(

    val danmakuRegexFilterEnabled: Boolean = true,

    @Suppress("PropertyName") @Transient val _placeholder: Int = 0

) {
    companion object {
        @Stable
        val Default = DanmakuFilterConfig()
    }
}