@file:Suppress("NOTHING_TO_INLINE")

package me.him188.ani.app.ui.foundation.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.DpSize

@Stable
val LocalLayoutMode: ProvidableCompositionLocal<LayoutMode> = staticCompositionLocalOf {
    error("No LayoutMode provided")
}

/**
 * 当前设备布局模式
 */
@Immutable
class LayoutMode(
    /**
     * 显示横屏 UI. 例如在 PC 和平板. 注意, 在这些平台上, 若应用的窗口大小很窄, 则仍然需要显示竖屏 UI.
     */
    val showLandscapeUI: Boolean,
    val deviceSize: DpSize,
)

@Composable
inline fun isShowLandscapeUI(): Boolean = LocalLayoutMode.current.showLandscapeUI