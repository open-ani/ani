/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.ui.foundation.theme

import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
object AniThemeDefaults {
    // 参考 M3 配色方案:
    // https://m3.material.io/styles/color/roles#63d6db08-59e2-4341-ac33-9509eefd9b4f

    /**
     * Navigation rail on desktop, bottom navigation on mobile.
     */
    val navigationContainerColor
        @Composable
        get() = MaterialTheme.colorScheme.surfaceContainer

    // Use surface roles for more neutral backgrounds, and container colors for components like cards, sheets, and dialogs.
    val appContentPaneColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.surface

    @Composable
    fun topAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    )

    /**
     * 仅充当背景作用的卡片颜色, 例如 RSS 设置页中的那些圆角卡片背景
     */
    @Composable
    fun backgroundCardColors(): CardColors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainerLow),
        )

    /**
     * 适用于整个 pane 都是一堆卡片, 而且这些卡片有一定的作用. 例如追番列表的卡片.
     */
    @Composable
    fun primaryCardColors(): CardColors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainerHigh),
        )
}
