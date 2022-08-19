@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package me.him188.animationgarden.desktop.ui.interaction

import androidx.compose.runtime.*
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.FrameWindowScope
import me.him188.animationgarden.desktop.i18n.LocalI18n
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
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
                    val application = com.apple.eawt.Application.getApplication()

                    application.setPreferencesHandler {
                        currentOnClickPreferences()
                    }

                    this.onDispose {
                        application.setAboutHandler(null)
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
                            icon = painterResource("drawable/cog.svg"),
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