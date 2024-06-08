package me.him188.ani.app.data.models

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Immutable
enum class FullscreenSwitchMode {
    /**
     * 在小屏 (竖屏) 模式下也在右下角总是显示全屏按钮.
     */
    ALWAYS_SHOW_FLOATING,

    /**
     * 在小屏 (竖屏) 模式下也在右下角显示全屏按钮, 但在五秒后自动隐藏
     */
    AUTO_HIDE_FLOATING,

    /**
     * 仅在控制器显示时才有全屏按钮.
     */
    ONLY_IN_CONTROLLER
}

@Serializable
@Immutable
data class VideoScaffoldConfig(
    /**
     * 在小屏 (竖屏) 模式下也在右下角显示全屏按钮.
     */
    val fullscreenSwitchMode: FullscreenSwitchMode = FullscreenSwitchMode.AUTO_HIDE_FLOATING,
    @Suppress("PropertyName") @Transient val _placeholder: Int = 0,
) {
    companion object {
        @Stable
        val Default = VideoScaffoldConfig()
    }
}
