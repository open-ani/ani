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

package me.him188.ani.app.ui.search

import androidx.compose.material3.SelectableChipElevation
import androidx.compose.runtime.Composable
import me.him188.ani.app.app.RefreshState

@Composable
actual fun <T> FilterChipRowImpl(
    list: List<T>,
    key: (item: T) -> Any,
    isSelected: @Composable (T) -> Boolean,
    onClick: ((T) -> Unit)?,
    enabled: @Composable (T) -> Boolean,
    isExpanded: Boolean,
    elevation: SelectableChipElevation?,
    refreshState: RefreshState?,
    onClickRefreshResult: (() -> Unit)?,
    content: @Composable (T) -> Unit,
) {
    FilterChipRowByLazyRow(
        list,
        key,
        isSelected,
        onClick,
        enabled,
        elevation,
        refreshState,
        onClickRefreshResult,
        content
    )
}