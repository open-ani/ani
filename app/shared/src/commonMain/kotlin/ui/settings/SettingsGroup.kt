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

package me.him188.animationgarden.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.animationgarden.app.AppTheme

@Composable
fun ColumnScope.SettingsGroup(
    title: @Composable () -> Unit,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp, Alignment.Top),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    ProvideTextStyle(AppTheme.typography.titleMedium.copy(lineHeight = 36.sp)) {
        Box(Modifier.padding(start = 8.dp)) { title() }
    }
    ProvideTextStyle(AppTheme.typography.bodyMedium) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement,
            horizontalAlignment
        ) {
            content()
        }
    }
}