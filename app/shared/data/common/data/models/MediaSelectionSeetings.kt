package me.him188.ani.app.data.models

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * 数据源选择器 (播放页面点击 "数据源" 按钮弹出的) 的设置
 */
@Serializable
@Immutable
data class MediaSelectorSettings(
    /**
     * 即使数据源禁用, 也在选择器中以灰色显示, 方便临时启用
     */
    val showDisabled: Boolean = true,
    /**
     * 完结后隐藏单集资源
     */
    val hideSingleEpisodeForCompleted: Boolean = true,
    /**
     * 优先选择季度全集资源
     */
    val preferSeasons: Boolean = true,
    @Suppress("PropertyName") @Transient val _placeholder: Int = 0,
) {
    // 这篇小说已经完结了
    companion object {
        @Stable
        val Default = MediaSelectorSettings()
    }
}