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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.animationgarden.app.AppTheme
import me.him188.animationgarden.app.app.RefreshState
import me.him188.animationgarden.app.i18n.LocalI18n
import me.him188.animationgarden.app.platform.Res

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

    val showSuccessHint by animateFloatAsState(if (refreshState !is RefreshState.Success) 1f else 0f, tween(2000))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        val cardHeight = 32.dp
        val textHeight = 24.sp
        val progressSize = 18.dp
        val tickSize = 24.dp
        items(list, key = key) {
            ElevatedFilterChip(
                selected = isSelected(it),
                onClick = { currentOnClick?.invoke(it) },
                label = {
                    ProvideTextStyle(LocalTextStyle.current.copy(lineHeight = textHeight)) {
                        content(it)
                    }
                },
                enabled = enabled.invoke(it),
                elevation = elevation,
                modifier = Modifier.height(cardHeight),
//                modifier = Modifier.animateItemPlacement(tween(200, 100)),
            )
        }
        if (refreshState != null && (refreshState !is RefreshState.Success || showSuccessHint > 0)) {
            item(key = "refreshing") {
                FilterChip(
                    selected = false,
                    onClick = { currentOnClickRefreshState?.invoke() },
                    label = {
                        ProvideTextStyle(LocalTextStyle.current.copy(lineHeight = textHeight)) {
                            when (refreshState) {
                                is RefreshState.Failed -> Text(LocalI18n.current.getString("starred.update.failed"))
                                RefreshState.Refreshing -> CircularProgressIndicator(
                                    Modifier.size(progressSize),
                                    strokeWidth = 2.dp
                                )
                                is RefreshState.Success -> {
                                    Icon(
                                        Res.painter.check,
                                        LocalI18n.current.getString("starred.update.succeed"),
                                        Modifier.size(tickSize),
                                        tint = AppTheme.colorScheme.primary
                                    )
                                }
                                RefreshState.Cancelled -> {
                                    // nop
                                }
                            }
                        }
                    },
                    enabled = false,
                    elevation = elevation,
                    modifier = Modifier.height(cardHeight),
                    border = null,
//                modifier = Modifier.animateItemPlacement(tween(200, 100)),
                )
            }
        }
    }
}