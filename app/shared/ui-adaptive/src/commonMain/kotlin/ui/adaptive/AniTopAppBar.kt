/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.interaction.WindowDragArea
import me.him188.ani.app.ui.foundation.layout.isAtLeastMedium
import me.him188.ani.app.ui.foundation.layout.paddingIfNotEmpty
import me.him188.ani.app.ui.foundation.layout.paneHorizontalPadding
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults

/**
 * 小屏幕上使用默认 TopAppBar 高度, 使用 36dp 头像; MEDIUM 及以上上增加额外 padding(all=8.dp), 并使用 48dp 头像
 *
 * 默认颜色为 [AniThemeDefaults.topAppBarColors]
 *
 * @see TopAppBar
 */
@Composable
fun AniTopAppBar(
    title: @Composable () -> Unit,
    windowInsets: WindowInsets,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    avatar: @Composable () -> Unit = {},
    searchBar: @Composable () -> Unit = {},
    expandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    colors: TopAppBarColors = AniThemeDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    WindowDragArea {
        TopAppBar(
            title,
            modifier
                .ifThen(windowSizeClass.windowWidthSizeClass.isAtLeastMedium && windowSizeClass.windowHeightSizeClass.isAtLeastMedium) {
                    padding(all = 8.dp)
                },
            navigationIcon,
            actions = {
                val horizontalPadding =
                    windowSizeClass.paneHorizontalPadding // refer to design on figma

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    actions()
                    searchBar()
                }

                Box(Modifier.paddingIfNotEmpty(horizontal = horizontalPadding)) {
                    val maxSize =
                        if (windowSizeClass.windowWidthSizeClass.isAtLeastMedium && windowSizeClass.windowWidthSizeClass.isAtLeastMedium) {
                            48.dp
                        } else {
                            36.dp
                        }
                    Box(Modifier.sizeIn(maxHeight = maxSize, maxWidth = maxSize)) {
                        avatar()
                    }
                }
            },
            expandedHeight,
            windowInsets,
            colors,
            scrollBehavior,
        )
    }
}