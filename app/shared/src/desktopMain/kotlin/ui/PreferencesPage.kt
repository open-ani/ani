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

package me.him188.ani.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.him188.ani.app.app.AppSettings
import me.him188.ani.app.app.AppSettingsManager
import me.him188.ani.app.app.LocalAppSettingsManager
import me.him188.ani.app.i18n.LocalI18n
import me.him188.ani.app.ui.interaction.PlatformImplementations
import me.him188.ani.app.ui.settings.ProxySettingsGroup
import me.him188.ani.app.ui.settings.SettingsGroup
import me.him188.ani.app.ui.settings.SyncSettingsGroup

@Composable
fun PreferencesPage(
    snackbar: SnackbarHostState? // pass null for preview only
) {
    val manager = LocalAppSettingsManager.current
    val settings by manager.value.collectAsState()
    val scope = rememberCoroutineScope()
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (PlatformImplementations.hostIsMacOs) {
                MacOSAppearanceSettings(manager, settings)
            }

            ProxySettingsGroup(settings, manager)
            val i18n by rememberUpdatedState(LocalI18n.current)
            SyncSettingsGroup(settings, manager, onSaved = {
                scope.launch {
                    snackbar?.showSnackbar(
                        i18n.getString("preferences.sync.changes.apply.on.restart"),
                        withDismissAction = true
                    )
                }
            })
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