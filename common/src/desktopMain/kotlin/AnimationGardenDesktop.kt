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

package me.him188.animationgarden.app

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.app.app.*
import me.him188.animationgarden.app.i18n.LocalI18n
import me.him188.animationgarden.app.i18n.loadResourceBundle
import me.him188.animationgarden.app.platform.Context
import me.him188.animationgarden.app.platform.LocalContext
import me.him188.animationgarden.app.ui.MainPage
import me.him188.animationgarden.app.ui.PreferencesPage
import me.him188.animationgarden.app.ui.WindowEx
import me.him188.animationgarden.app.ui.interaction.PlatformImplementations
import me.him188.animationgarden.app.ui.interaction.PlatformImplementations.Companion.hostIsMacOs
import java.io.File

object AnimationGardenDesktop {
    @JvmStatic
    fun main(args: Array<String>) {
        application(exitProcessOnExit = true) {


            val workingDir = remember { File(System.getProperty("user.dir")) }
            val appSettingsProvider = remember {
                LocalAppSettingsManagerImpl(workingDir.resolve("data/settings.yml"))
                    .apply { load() }
            }
            appSettingsProvider.attachAutoSave()
            val platform = remember { PlatformImplementations.current }


            val currentAppSettings by rememberUpdatedState(appSettingsProvider.value.value)

            val app = remember {
                // do not observe dependency change
                ApplicationState(
                    initialClient = AnimationGardenClient.Factory.create {
                        proxy = currentAppSettings.proxy.toKtorProxy()
                    },
                    workingDir = workingDir
                )
            }
            LaunchedEffect(currentAppSettings.proxy) {
                // proxy changed, update client
                app.client.value = AnimationGardenClient.Factory.create {
                    proxy = currentAppSettings.proxy.toKtorProxy()
                }
            }

            val currentDensity by rememberUpdatedState(LocalDensity.current)
            val minimumSize by remember {
                derivedStateOf {
                    with(currentDensity) {
                        Size(200.dp.toPx(), 200.dp.toPx())
                    }
                }
            }

            app.appDataSaver.attachAutoSave()
            val context: Context = LocalContext.current
            val currentBundle = remember(Locale.current.language) { loadResourceBundle(context) }
            CompositionLocalProvider(
                LocalI18n provides currentBundle,
                LocalAppSettingsManager provides appSettingsProvider
            ) {
                var showPreferences by remember { mutableStateOf(false) }
                if (showPreferences) {
                    val state = rememberWindowState(width = 350.dp, height = 280.dp)
                    WindowEx(
                        state = state,
                        onCloseRequest = {
                            showPreferences = false
                        },
                        title = LocalI18n.current.getString("window.preferences.title"),
                        resizable = false,
                        alwaysOnTop = true,
                    ) {
                        PreferencesPage()
                    }
                }

                WindowEx(
                    title = LocalI18n.current.getString("window.main.title"),
                    onCloseRequest = ::exitApplication,
                    minimumSize = minimumSize,
                ) {
                    with(platform.menuBarProvider) {
                        MenuBar(onClickPreferences = {
                            showPreferences = true
                        })
                    }

                    // This actually runs only once since app is never changed.
                    val windowImmersed = LocalAppSettings.current.windowImmersed
                    if (windowImmersed) {
                        SideEffect {
                            window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                            window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                        }
                    } else {
                        SideEffect {
                            window.rootPane.putClientProperty("apple.awt.fullWindowContent", false)
                            window.rootPane.putClientProperty("apple.awt.transparentTitleBar", false)
                        }
                    }

                    MainWindowContent(
                        hostIsMacOs = hostIsMacOs,
                        app = app,
                        windowImmersed = windowImmersed
                    )
                }
            }
        }
    }
}


@Composable
@Preview
fun PreviewPreferencesWindow() {
    ProvideCompositionLocalsForPreview {
        PreferencesPage()
    }
}

@Composable
private fun MainWindowContent(
    hostIsMacOs: Boolean,
    windowImmersed: Boolean,
    app: ApplicationState,
) {
    Box(
        Modifier.background(color = AppTheme.colorScheme.background)
            .padding(top = if (hostIsMacOs && windowImmersed) 16.dp else 0.dp) // safe area for macOS if windowImmersed
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val paddingByWindowSize by animateDpAsState(
                if (maxWidth > 400.dp) {
                    16.dp
                } else {
                    8.dp
                },
            )

            Box(Modifier.padding(all = paddingByWindowSize)) {
                MainPage(app, innerPadding = paddingByWindowSize)
            }
        }
    }
}

@Composable
@Preview
fun PreviewMainWindowMacOS() {
    val app = remember {
        ApplicationState(
            initialClient = AnimationGardenClient.Factory.create {},
            workingDir = File(System.getProperty("user.dir"))
        )
    }
    ProvideCompositionLocalsForPreview {
        MainWindowContent(hostIsMacOs = false, windowImmersed = false, app)
    }
}
