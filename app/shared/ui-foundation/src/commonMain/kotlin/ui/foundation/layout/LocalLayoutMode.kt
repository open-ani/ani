/*
 * Copyright 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

package me.him188.ani.app.ui.foundation.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.DpSize

@Stable
@Deprecated("Use currentWindowAdaptiveInfo() instead")
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