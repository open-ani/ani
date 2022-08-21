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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.animationgarden.app.app.AppSettings
import me.him188.animationgarden.app.app.AppSettingsManager
import me.him188.animationgarden.app.app.LocalAppSettingsManager
import me.him188.animationgarden.app.i18n.LocalI18n
import me.him188.animationgarden.app.ui.interaction.PlatformImplementations
import me.him188.animationgarden.app.ui.preferences.ProxySettings
import me.him188.animationgarden.app.ui.preferences.SettingsGroup

@Composable
fun PreferencesPage() {
    val manager = LocalAppSettingsManager.current
    val settings by manager.value
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (PlatformImplementations.hostIsMacOs) {
                MacOSAppearanceSettings(manager, settings)
            }

            ProxySettings(settings, manager)
        }
    }
}

@Composable
private fun ColumnScope.MacOSAppearanceSettings(
    manager: AppSettingsManager,
    settings: AppSettings
) {
    SettingsGroup(
        title = {
            Text(LocalI18n.current.getString("preferences.appearance"))
        },
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Row(
            Modifier.height(24.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        manager.mutate { copy(windowImmersed = !settings.windowImmersed) }
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = settings.windowImmersed,
                onCheckedChange = {
                    manager.mutate { copy(windowImmersed = it) }
                },
                interactionSource = interactionSource,
            )
            Text(LocalI18n.current.getString("preferences.appearance.window.immersed"))
        }
    }
}