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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.animationgarden.app.app.RefreshState
import me.him188.animationgarden.app.i18n.LocalI18n

@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS") // otherwise Compose compiler will complain
@Composable
actual fun <T> FilterChipRow(
    list: List<T>,
    key: (item: T) -> Any,
    isSelected: @Composable (T) -> Boolean,
    onClick: ((T) -> Unit)?,
    enabled: @Composable (T) -> Boolean,
    elevation: SelectableChipElevation? = FilterChipDefaults.elevatedFilterChipElevation(),
    refreshState: RefreshState? = null,
    onClickRefreshResult: (() -> Unit)? = null,
    content: @Composable (T) -> Unit = { Text(it.toString()) },
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnClickRefreshState by rememberUpdatedState(onClickRefreshResult)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        items(list, key = key) {
            ElevatedFilterChip(
                selected = isSelected(it),
                onClick = { currentOnClick?.invoke(it) },
                label = { content(it) },
                enabled = enabled.invoke(it),
                elevation = elevation,
                modifier = Modifier.height(16.dp)
//                modifier = Modifier.animateItemPlacement(tween(200, 100)),
            )
        }
        if (refreshState != null && refreshState !is RefreshState.Success) {
            item(key = "refreshing") {
                ElevatedFilterChip(
                    selected = false,
                    onClick = { currentOnClickRefreshState?.invoke() },
                    label = {
                        when (refreshState) {
                            is RefreshState.Failed -> Text(LocalI18n.current.getString("starred.update.failed"))
                            RefreshState.Refreshing -> CircularProgressIndicator()
                            is RefreshState.Cancelled -> {
                                // nop
                            }
                            else -> {}
                        }
                    },
                    enabled = false,
                    elevation = elevation,
//                modifier = Modifier.animateItemPlacement(tween(200, 100)),
                )
            }
        }
    }
}