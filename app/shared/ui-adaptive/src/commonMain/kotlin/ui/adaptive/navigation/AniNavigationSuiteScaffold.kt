/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.adaptive.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.him188.ani.app.ui.foundation.interaction.WindowDragArea
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults

/**
 * @param navigationSuite use [AniNavigationSuite]
 *
 * @see NavigationSuiteScaffoldLayout
 * @see NavigationSuiteScaffold
 */
@Composable
fun AniNavigationSuiteLayout(
    navigationSuite: @Composable () -> Unit, // Ani modified
    modifier: Modifier = Modifier,
    layoutType: NavigationSuiteType =
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo()),
//    navigationSuiteColors: NavigationSuiteColors = NavigationSuiteDefaults.colors(), // Ani modified
    navigationContainerColor: Color = AniThemeDefaults.navigationContainerColor,
    navigationContentColor: Color = contentColorFor(AniThemeDefaults.navigationContainerColor),
    content: @Composable () -> Unit = {},
) {
    Surface(modifier = modifier, color = navigationContainerColor, contentColor = navigationContentColor) {
        NavigationSuiteScaffoldLayout(
            navigationSuite = {
                WindowDragArea { // Ani modified: add WindowDragArea
                    navigationSuite()
                }
            },
            layoutType = layoutType,
            content = {
                Box(
                    Modifier.consumeWindowInsets(
                        when (layoutType) {
                            NavigationSuiteType.NavigationBar ->
                                NavigationBarDefaults.windowInsets

                            NavigationSuiteType.NavigationRail ->
                                NavigationRailDefaults.windowInsets

                            NavigationSuiteType.NavigationDrawer ->
                                DrawerDefaults.windowInsets

                            else -> WindowInsets(0, 0, 0, 0)
                        },
                    ),
                ) {
                    content()
                }
            },
        )
    }
}
