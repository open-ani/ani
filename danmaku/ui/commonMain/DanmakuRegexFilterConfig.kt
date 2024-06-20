package me.him188.ani.danmaku.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.danmaku.api.Danmaku
import java.util.UUID

/**4
 * Defines which [Danmaku] can be displayed.
 */
@Immutable
data class DanmakuRegexFilterConfig (

    /**
     * Defines wheather Danmaku filter is on.
     */
    
    val danmakuRegexFilterOn: Boolean = false,

    /**
     * Defines which [Danmaku] can be displayed according to a list of regular expression.
     */
    
    val danmakuRegexFilterList: List<DanmakuRegexFilter> = mutableListOf(),
    @Suppress("PropertyName") @Transient val _placeholder: Int = 0,


    ) {
    companion object {
        @Stable
        val Default = DanmakuRegexFilterConfig(danmakuRegexFilterOn = true)
    }
}

@Serializable
data class DanmakuRegexFilter (
    val instanceID: String = UUID.randomUUID().toString(),
    val name: String = "",
    val re: String = "",
    val isEnabled: Boolean = true
) {
    companion object {
        @Stable
        val Default = DanmakuRegexFilter()
    }
}

