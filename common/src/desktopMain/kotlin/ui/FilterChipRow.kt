/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.app.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import me.him188.animationgarden.app.app.RefreshState

@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS") // otherwise Compose compiler will complain
@Composable
actual fun <T> FilterChipRow(
    list: List<T>,
    key: (item: T) -> Any,
    isSelected: @Composable (T) -> Boolean,
    onClick: ((T) -> Unit)?,
    enabled: @Composable (T) -> Boolean,
    isExpanded: Boolean,
    elevation: SelectableChipElevation? = FilterChipDefaults.elevatedFilterChipElevation(),
    refreshState: RefreshState? = null,
    onClickRefreshResult: (() -> Unit)? = null,
    content: @Composable (T) -> Unit = { Text(it.toString()) },
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