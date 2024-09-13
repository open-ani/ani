package me.him188.ani.app.ui.foundation.layout

import androidx.compose.foundation.layout.PaddingValues
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
