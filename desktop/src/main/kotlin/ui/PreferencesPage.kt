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

package me.him188.animationgarden.desktop.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.animationgarden.desktop.AppTheme
import me.him188.animationgarden.desktop.app.AppSettingsManager
import me.him188.animationgarden.desktop.app.LocalAppSettingsManager
import me.him188.animationgarden.desktop.app.ProxyMode
import me.him188.animationgarden.desktop.i18n.LocalI18n
import me.him188.animationgarden.desktop.ui.interaction.PlatformImplementations
import me.him188.animationgarden.desktop.ui.preferences.BoxWithSaveButton
import me.him188.animationgarden.desktop.ui.preferences.LabelledRadioButton
import me.him188.animationgarden.desktop.ui.preferences.OutlinedTextFieldWithSaveButton
import me.him188.animationgarden.desktop.ui.preferences.SettingsOutlinedTextField

@Composable
fun PreferencesPage() {
    val manager = LocalAppSettingsManager.current
    val settings by manager.value
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (PlatformImplementations.hostIsMacOs) {
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

            SettingsGroup({ Text(LocalI18n.current.getString("preferences.proxy")) }) {
                Row(
                    Modifier.height(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LabelledRadioButton(settings.proxy.mode == ProxyMode.DISABLED, {
                        manager.mutate { copy(proxy = proxy.copy(mode = ProxyMode.DISABLED)) }
                    }) {
                        Text(LocalI18n.current.getString("preferences.proxy.mode.disabled"))
                    }

                    LabelledRadioButton(settings.proxy.mode == ProxyMode.HTTP, {
                        manager.mutate { copy(proxy = proxy.copy(mode = ProxyMode.HTTP)) }
                    }) {
                        Text(LocalI18n.current.getString("preferences.proxy.mode.http"))
                    }

                    LabelledRadioButton(settings.proxy.mode == ProxyMode.SOCKS, {
                        manager.mutate { copy(proxy = proxy.copy(mode = ProxyMode.SOCKS)) }
                    }) {
                        Text(LocalI18n.current.getString("preferences.proxy.mode.socks"))
                    }
                }

                AnimatedVisibility(
                    settings.proxy.mode == ProxyMode.HTTP,
                    enter = fadeIn() + expandVertically(tween(durationMillis = 200, delayMillis = 200)),
                    exit = fadeOut() + shrinkVertically(tween(durationMillis = 200)),
                ) {
                    Row(Modifier.padding(vertical = 8.dp, horizontal = 8.dp).height(48.dp)) {
                        var value by remember { mutableStateOf(settings.proxy.http.url) }
                        OutlinedTextFieldWithSaveButton(
                            value = value,
                            onValueChange = { value = it },
                            showButton = value != settings.proxy.http.url,
                            onClickSave = {
                                manager.updateProxyHttpUrl(value)
                            },
                            label = {
                                Text(LocalI18n.current.getString("preferences.proxy.http.url"))
                            }
                        )
                    }
                }

                AnimatedVisibility(
                    settings.proxy.mode == ProxyMode.SOCKS,
                    enter = fadeIn() + expandVertically(tween(durationMillis = 200, delayMillis = 200)),
                    exit = fadeOut() + shrinkVertically(tween(durationMillis = 200)),
                ) {
                    Row(Modifier.padding(vertical = 8.dp, horizontal = 8.dp).height(48.dp)) {
                        var newHost by remember { mutableStateOf(settings.proxy.socks.host) }
                        var newPort by remember { mutableStateOf(settings.proxy.socks.port) }

                        val hostFocus = remember { FocusRequester() }
                        val portFocus = remember { FocusRequester() }
                        val buttonFocus = remember { FocusRequester() }
                        BoxWithSaveButton(
                            showButton = newHost != settings.proxy.socks.host || newPort != settings.proxy.socks.port,
                            onClickSave = {
                                manager.updateProxyHttpUrl(newHost)
                            },
                            buttonHeightOffset = { 8.dp },
                            Modifier.focusGroup(),
                            buttonModifier = Modifier
                                .focusRequester(buttonFocus)
                                .focusProperties {
                                    previous = portFocus
                                    left = portFocus
                                    start = hostFocus
                                    end = buttonFocus
                                }
                        ) { width ->
                            SettingsOutlinedTextField(
                                value = newHost,
                                onValueChange = { newHost = it },
                                width = width * 0.8f,
                                onPressEnter = {
                                    portFocus.requestFocus()
                                    true
                                },
                                Modifier
                                    .focusRequester(hostFocus)
                                    .focusProperties {
                                        next = portFocus
                                        right = portFocus
                                        start = hostFocus
                                        end = buttonFocus
                                    },
                                label = {
                                    Text(LocalI18n.current.getString("preferences.proxy.socks.host"))
                                },
                            )
                            SettingsOutlinedTextField(
                                value = remember(newPort) { newPort.toString() },
                                onValueChange = { newPort = it.toIntOrNull() ?: 0 },
                                width = width * 0.2f,
                                onPressEnter = {
                                    manager.updateProxySocks(newHost, newPort)
                                    true
                                },
                                Modifier
                                    .padding(start = 8.dp)
                                    .focusRequester(portFocus)
                                    .focusProperties {
                                        previous = hostFocus
                                        next = buttonFocus
                                        left = hostFocus
                                        right = buttonFocus
                                        start = hostFocus
                                        end = buttonFocus
                                    },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                label = {
                                    Text(LocalI18n.current.getString("preferences.proxy.socks.port"))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    title: @Composable () -> Unit,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    ProvideTextStyle(AppTheme.typography.titleMedium.copy(lineHeight = 36.sp)) {
        Box(Modifier.padding(start = 8.dp)) {
            title()
        }
    }

    ProvideTextStyle(AppTheme.typography.bodyMedium) {
        Column(Modifier.fillMaxWidth(), verticalArrangement, horizontalAlignment) {
            content()
        }
    }
}


private fun AppSettingsManager.updateProxyHttpUrl(value: String) {
    mutate { copy(proxy = proxy.run { copy(http = http.copy(url = value)) }) }
}

private fun AppSettingsManager.updateProxySocks(host: String, port: Int) {
    mutate { copy(proxy = proxy.run { copy(socks = socks.copy(host = host, port = port)) }) }
}