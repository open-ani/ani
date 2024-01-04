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

package me.him188.animationgarden.desktop

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.dirs.ProjectDirectories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.him188.animationgarden.app.AppTheme
import me.him188.animationgarden.app.ProvideCompositionLocalsForPreview
import me.him188.animationgarden.app.app.*
import me.him188.animationgarden.app.app.data.map
import me.him188.animationgarden.app.app.settings.*
import me.him188.animationgarden.app.i18n.LocalI18n
import me.him188.animationgarden.app.i18n.ResourceBundle
import me.him188.animationgarden.app.i18n.loadResourceBundle
import me.him188.animationgarden.app.platform.Context
import me.him188.animationgarden.app.platform.LocalContext
import me.him188.animationgarden.app.ui.*
import me.him188.animationgarden.app.ui.interaction.PlatformImplementations
import me.him188.animationgarden.app.ui.interaction.PlatformImplementations.Companion.hostIsMacOs
import me.him188.animationgarden.datasources.dmhy.DmhyClient
import me.him188.animationgarden.datasources.dmhy.DmhyDownloadProvider
import me.him188.animationgarden.utils.logging.logger
import me.him188.animationgarden.utils.logging.trace
import java.io.File

private val logger = logger()


val projectDirectories: ProjectDirectories by lazy {
    ProjectDirectories.from(
        "me",
        "Him188",
        "Animation Garden"
    )
}

object AnimationGardenDesktop {
    @JvmStatic
    fun main(args: Array<String>) {
        projectDirectories.dataDir
        application(exitProcessOnExit = true) {
            val context: Context = LocalContext.current
            val currentBundle = remember(Locale.current.language) { loadResourceBundle(context) }

            val appSettingsProvider = remember {
                LocalAppSettingsManagerImpl(
                    File(projectDirectories.preferenceDir, "settings.dat").also {
                        it.parentFile.mkdirs()
                        logger.trace { "Settings file: ${it.absolutePath}" }
                    }
                ).apply { load() }
            }
            appSettingsProvider.attachAutoSave()
            val platform = remember { PlatformImplementations.current }


            val currentAppSettings by rememberUpdatedState(appSettingsProvider.value.value)

            val localSyncSettingsFlow = snapshotFlow { currentAppSettings.sync.localSync }
            val mainSnackbar = remember { SnackbarHostState() }

            CompositionLocalProvider(
                LocalI18n provides currentBundle,
                LocalAppSettingsManager provides appSettingsProvider,
                LocalAlwaysShowTitlesInSeparateLine provides true, // for performance, and #41
            ) {
                content(
                    currentAppSettings,
                    localSyncSettingsFlow,
                    mainSnackbar,
                    currentBundle,
                    platform
                )
            }
        }
    }

    @Composable
    private fun ApplicationScope.content(
        currentAppSettings: AppSettings,
        localSyncSettingsFlow: Flow<LocalSyncSettings>,
        mainSnackbar: SnackbarHostState,
        currentBundle: ResourceBundle,
        platform: PlatformImplementations,
    ) {
        val dialogHost = rememberDialogHost()
        val app = remember {
            // do not observe dependency change
            createAppState(
                currentAppSettings,
                dialogHost,
                localSyncSettingsFlow,
                mainSnackbar,
                currentBundle
            )
        }
        LaunchedEffect(currentAppSettings.proxy) {
            // proxy changed, update client
            app.client.value = DmhyClient.create {
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

        var showPreferences by remember { mutableStateOf(false) }
        if (showPreferences) {
            val state = rememberWindowState(width = 350.dp, height = 600.dp)
            WindowEx(
                state = state,
                onCloseRequest = {
                    showPreferences = false
                },
                title = LocalI18n.current.getString("window.preferences.title"),
                resizable = false,
                alwaysOnTop = true,
            ) {
                val snackbar = remember { SnackbarHostState() }
                Scaffold(
                    topBar = {},
                    bottomBar = {},
                    snackbarHost = { SnackbarHost(snackbar) },
                ) {
                    PreferencesPage(snackbar)
                }
            }
        }

        WindowEx(
            title = LocalI18n.current.getString("window.main.title"),
            onCloseRequest = {
                runBlocking(Dispatchers.IO) { app.dataSynchronizer.saveNow() }
                exitApplication()
            },
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

            Scaffold(
                topBar = {},
                bottomBar = {},
                snackbarHost = {
                    SnackbarHost(mainSnackbar)
                },
            ) {
                MainWindowContent(
                    hostIsMacOs = hostIsMacOs,
                    app = app,
                    windowImmersed = windowImmersed,
                    onClickProxySettings = {
                        showPreferences = true
                    }
                )
            }
        }
    }
}

private fun createAppState(
    currentAppSettings: AppSettings,
    dialogHost: DialogHost,
    localSyncSettingsFlow: Flow<LocalSyncSettings>,
    snackbarState: SnackbarHostState,
    currentBundle: ResourceBundle,
) = ApplicationState(
    initialClient = DmhyClient.Factory.create {
        proxy = currentAppSettings.proxy.toKtorProxy()
    },
)

@Composable
@Preview
fun PreviewPreferencesWindow() {
    ProvideCompositionLocalsForPreview {
        PreferencesPage(null)
    }
}

@Composable
private fun MainWindowContent(
    hostIsMacOs: Boolean,
    windowImmersed: Boolean,
    app: ApplicationState,
    onClickProxySettings: () -> Unit,
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
                MainPage(
                    app,
                    innerPadding = paddingByWindowSize,
                    onClickProxySettings = onClickProxySettings
                )
            }
        }
    }
}

@Composable
@Preview
fun PreviewMainWindowMacOS() {
    val app = remember {
        ApplicationState(
            initialClient = DmhyClient.Factory.create {},
        )
    }
    ProvideCompositionLocalsForPreview {
        MainWindowContent(
            hostIsMacOs = false,
            windowImmersed = false,
            app,
            onClickProxySettings = {})
    }
}
