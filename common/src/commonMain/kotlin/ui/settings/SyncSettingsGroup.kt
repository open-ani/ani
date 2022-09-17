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

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.animationgarden.app.app.AppSettings
import me.him188.animationgarden.app.app.AppSettingsManager
import me.him188.animationgarden.app.i18n.LocalI18n

@Composable
fun ColumnScope.SyncSettingsGroup(
    settings: AppSettings,
    manager: AppSettingsManager,
    onSaved: () -> Unit,
) {
    SettingsGroup({ Text(LocalI18n.current.getString("preferences.sync")) }) {
        Row(
            Modifier.height(30.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelledCheckBox(settings.sync.localSyncEnabled, {
                manager.updateLocalSyncEnabled(it)
                onSaved()
            }) {
                Text(LocalI18n.current.getString("preferences.sync.local.enabled"))
            }
        }

        Row(
            Modifier.height(30.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelledCheckBox(settings.sync.remoteSyncEnabled, {
                manager.updateRemoteSyncEnabled(it)
                onSaved()
            }) {
                Text(LocalI18n.current.getString("preferences.sync.remote.enabled"))
            }
        }

        Row(Modifier.padding(vertical = 8.dp, horizontal = 8.dp).height(48.dp)) {
            var value by remember { mutableStateOf(settings.sync.remoteSync.apiUrl) }
            OutlinedTextFieldWithSaveButton(
                value = value,
                onValueChange = { value = it },
                showButton = value != settings.sync.remoteSync.apiUrl,
                onClickSave = {
                    manager.updateRemoteSyncApiUrl(value)
                    onSaved()
                },
                label = {
                    Text(LocalI18n.current.getString("preferences.sync.remote.server.api.url"))
                }
            )
        }

        Row(Modifier.padding(vertical = 8.dp, horizontal = 8.dp).height(48.dp)) {
            var value by remember { mutableStateOf(settings.sync.remoteSync.token) }
            OutlinedTextFieldWithSaveButton(
                value = value,
                onValueChange = { value = it },
                showButton = value != settings.sync.remoteSync.token,
                onClickSave = {
                    manager.updateRemoteSyncToken(value)
                    onSaved()
                },
                label = {
                    Text(LocalI18n.current.getString("preferences.sync.remote.token"))
                }
            )
        }
    }
}
