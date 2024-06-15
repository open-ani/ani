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

package me.him188.ani.app.interaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.FrameWindowScope
import me.him188.ani.app.i18n.LocalI18n
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.awt.Desktop
import androidx.compose.ui.window.MenuBar as ComposeMenuBar

sealed interface PlatformImplementations {
    val menuBarProvider: MenuBarProvider
    val keyShortcuts: KeyShortcuts


    private class MacOS : PlatformImplementations {
        override val menuBarProvider: MenuBarProvider = object : MenuBarProvider {
            @Composable
            override fun FrameWindowScope.MenuBar(onClickPreferences: () -> Unit) {
                val currentOnClickPreferences by rememberUpdatedState(onClickPreferences)
                DisposableEffect(true) {
                    val desktop = Desktop.getDesktop()

                    desktop.setPreferencesHandler {
                        currentOnClickPreferences()
                    }

                    desktop.setAboutHandler {
                        currentOnClickPreferences()
                    }

                    this.onDispose {
                        desktop.setPreferencesHandler(null)
                        desktop.setAboutHandler(null)
                    }
                }
            }

        }
        override val keyShortcuts = object : KeyShortcuts() {
        }
    }

    private class NonMacOS : PlatformImplementations {
        override val menuBarProvider: MenuBarProvider = object : MenuBarProvider {
            @Composable
            override fun FrameWindowScope.MenuBar(onClickPreferences: () -> Unit) {
                ComposeMenuBar {
                    Menu("Help", 'H') {
                        Item(
//                            if (hostIsMacOs) LocalI18n.current.getString("menu.preferences")// on macOS there is unified design "Preference", on other platforms use "Settings" instead.
                            LocalI18n.current.getString("menu.settings"),
                            icon = painterResource("src/commonMain/composeResources/drawable/cog.svg"),
                            mnemonic = 'S',
                            shortcut = keyShortcuts.menuPreferences,
                            onClick = onClickPreferences
                        )
                    }
                }
            }
        }

        override val keyShortcuts = KeyShortcutsImpl()

        private class KeyShortcutsImpl : KeyShortcuts() {
            val menuPreferences: KeyShortcut = KeyShortcut(Key.S, ctrl = true, alt = true)
        }
    }

    companion object {
        @Stable
        val hostIsMacOs: Boolean = hostOs == OS.MacOS

        @Stable
        val current by lazy {
            when (hostOs) {
                OS.MacOS -> MacOS()
                else -> NonMacOS()
            }
        }
    }
}