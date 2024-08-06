package me.him188.ani.app.data.models.preference

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.platform.isMobile

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
    // TODO: 这个名字可能不好 
    /**
     * 在小屏 (竖屏) 模式下也在右下角显示全屏按钮.
     */
    val fullscreenSwitchMode: FullscreenSwitchMode = FullscreenSwitchMode.AUTO_HIDE_FLOATING,
    /**
     * 在编辑弹幕时暂停视频.
     * @since 3.2.0-beta01
     */
    val pauseVideoOnEditDanmaku: Boolean = currentPlatform.isMobile(),
    /**
     * 在观看到 90% 进度后, 自动标记看过
     */
    val autoMarkDone: Boolean = true,
    /**
     * 在点击选择剧集后, 立即隐藏 media selector
     */
    val hideSelectorOnSelect: Boolean = false,
    /**
     * 横屏时自动全屏
     */
    val autoFullscreenOnLandscapeMode: Boolean = false,
    /**
     * 自动连播
     */
    val autoPlayNext: Boolean = false,
    /**
     * 跳过 OP 和 ED
     */
    val autoSkipOpEd: Boolean = false,
    @Suppress("PropertyName") @Transient val _placeholder: Int = 0,
) {
    companion object {
        @Stable
        val Default = VideoScaffoldConfig()
    }
}
