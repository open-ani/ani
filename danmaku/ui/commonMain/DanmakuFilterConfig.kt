package me.him188.ani.danmaku.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import me.him188.ani.danmaku.api.Danmaku

/**
 * Defines which [Danmaku] can be displayed.
 */
@Immutable
data class DanmakuFilterConfig (

    /**
     * Defines wheather Danmaku filter is on.
     */
    
    val danmakuFilterOn: Boolean = false,
    
    /**
     * Defines which [Danmaku] can be displayed according to a list of regular expression.
     */
    
    val danmakuFilterList: List<String> = emptyList()

) {
    companion object {
        @Stable
        val Default = DanmakuFilterConfig()
    }
}

