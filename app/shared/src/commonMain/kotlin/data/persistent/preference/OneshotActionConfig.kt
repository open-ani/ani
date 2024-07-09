package me.him188.ani.app.data.persistent.preference

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

/**
 * 用于存储一次性提示的开关状态
 */
@Serializable
@Immutable
data class OneshotActionConfig(
    /**
     * 在搜索标签页显示长按删除标签的 Tip
     */
    val deleteSearchTagTip: Boolean = true
) {
    companion object {
        @Stable
        val Default = OneshotActionConfig()
    }
}