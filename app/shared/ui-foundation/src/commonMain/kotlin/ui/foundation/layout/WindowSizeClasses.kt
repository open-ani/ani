/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

private object PanePaddings {
    private val compactCompact = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    private val compactExpanded = PaddingValues(horizontal = 16.dp, vertical = 24.dp)
    private val expandedCompact = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    private val expandedExpanded = PaddingValues(horizontal = 24.dp, vertical = 24.dp)

    @Stable
    fun get(windowWidthSizeClass: WindowWidthSizeClass, windowHeightSizeClass: WindowHeightSizeClass): PaddingValues {
        val widthIsCompact = windowWidthSizeClass == WindowWidthSizeClass.COMPACT
        val heightIsCompact = windowHeightSizeClass == WindowHeightSizeClass.COMPACT
        return when {
            widthIsCompact && heightIsCompact -> compactCompact
            widthIsCompact && !heightIsCompact -> compactExpanded
            !widthIsCompact && heightIsCompact -> expandedCompact
            !widthIsCompact && !heightIsCompact -> expandedExpanded
            else -> error("unreachable")
        }
    }
}

@Stable
inline val WindowAdaptiveInfo.isWidthCompact: Boolean
    get() = windowSizeClass.windowWidthSizeClass.isCompact

@Stable
inline val WindowAdaptiveInfo.isWidthAtLeastMedium: Boolean
    get() = windowSizeClass.windowWidthSizeClass.isAtLeastMedium

@Stable
inline val WindowAdaptiveInfo.isHeightCompact: Boolean
    get() = windowSizeClass.windowHeightSizeClass.isCompact

@Stable
inline val WindowAdaptiveInfo.isHeightAtLeastMedium: Boolean
    get() = windowSizeClass.windowHeightSizeClass.isAtLeastMedium

@Stable
inline val WindowWidthSizeClass.isCompact
    get() = this == WindowWidthSizeClass.COMPACT

@Stable
inline val WindowWidthSizeClass.isAtLeastMedium
    get() = this != WindowWidthSizeClass.COMPACT

@Stable
inline val WindowHeightSizeClass.isCompact
    get() = this == WindowHeightSizeClass.COMPACT

@Stable
inline val WindowHeightSizeClass.isAtLeastMedium
    get() = this != WindowHeightSizeClass.COMPACT

@Stable
val WindowSizeClass.panePadding
    get() = PanePaddings.get(windowWidthSizeClass, windowHeightSizeClass)

@Stable
val WindowSizeClass.paneHorizontalPadding
    get() = if (windowWidthSizeClass == WindowWidthSizeClass.COMPACT) 16.dp else 24.dp

@Stable
val WindowSizeClass.paneVerticalPadding
    get() = if (windowHeightSizeClass == WindowHeightSizeClass.COMPACT) 16.dp else 24.dp

/**
 * 在一个主要的滚动列表中卡片的间距
 */
@Stable
val WindowSizeClass.cardHorizontalPadding
    get() = if (windowWidthSizeClass == WindowWidthSizeClass.COMPACT) 16.dp else 20.dp

/**
 * 在一个主要的滚动列表中卡片的间距
 */
@Stable
val WindowSizeClass.cardVerticalPadding
    get() = if (windowHeightSizeClass == WindowHeightSizeClass.COMPACT) 16.dp else 20.dp

private val zeroInsets = WindowInsets(0.dp) // single instance to be shared

@Stable
val WindowInsets.Companion.Zero: WindowInsets
    get() = zeroInsets

private val WindowWidthSizeClass.ordinal
    get() = when (this) {
        WindowWidthSizeClass.COMPACT -> 0
        WindowWidthSizeClass.MEDIUM -> 1
        WindowWidthSizeClass.EXPANDED -> 2
        else -> {
            error("Unsupported WindowWidthSizeClass: $this")
        }
    }

operator fun WindowWidthSizeClass.compareTo(other: WindowWidthSizeClass): Int {
    return ordinal.compareTo(other.ordinal)
}


private val WindowHeightSizeClass.ordinal
    get() = when (this) {
        WindowHeightSizeClass.COMPACT -> 0
        WindowHeightSizeClass.MEDIUM -> 1
        WindowHeightSizeClass.EXPANDED -> 2
        else -> {
            error("Unsupported WindowHeightSizeClass: $this")
        }
    }

operator fun WindowHeightSizeClass.compareTo(other: WindowHeightSizeClass): Int {
    return ordinal.compareTo(other.ordinal)
}
