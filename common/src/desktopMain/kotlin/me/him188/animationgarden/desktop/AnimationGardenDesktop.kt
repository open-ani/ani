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
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.dirs.ProjectDirectories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.impl.createHttpClient
import me.him188.animationgarden.api.logging.logger
import me.him188.animationgarden.api.logging.trace
import me.him188.animationgarden.api.logging.warn
import me.him188.animationgarden.api.protocol.CommitRef
import me.him188.animationgarden.app.AppTheme
import me.him188.animationgarden.app.ProvideCompositionLocalsForPreview
import me.him188.animationgarden.app.app.*
import me.him188.animationgarden.app.app.data.AppDataSynchronizerImpl
import me.him188.animationgarden.app.app.data.ConflictAction
import me.him188.animationgarden.app.app.data.Migrations
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
import java.io.File

private val logger = logger()


val projectDirectories: ProjectDirectories by lazy { ProjectDirectories.from("me", "Him188", "Animation Garden") }

private fun Migrations.tryMigrate(
    newAppDat: File = File(projectDirectories.dataDir, "app.dat"),
    newSettings: File = File(projectDirectories.preferenceDir, "settings.dat")
) {
    // 2.0.0-beta01
    migrateFile(
        legacy = File(projectDirectories.dataDir, "app.yml"),
        new = newAppDat,
    )
    migrateFile(
        legacy = File(projectDirectories.preferenceDir, "settings.yml"),
        new = newSettings,
    )

    // 1.x
    migrateFile(
        legacy = File(System.getProperty("user.dir"), "app.yml"),
        new = newAppDat,
    )
    migrateFile(
        legacy = File(System.getProperty("user.dir"), "settings.yml"),
        new = newSettings,
    )
}

object AnimationGardenDesktop {
    @JvmStatic
    fun main(args: Array<String>) {
        Migrations.tryMigrate()

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
                LocalAppSettingsManager provides appSettingsProvider
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
    }
}

private fun createAppState(
    currentAppSettings: AppSettings,
    dialogHost: DialogHost,
    localSyncSettingsFlow: Flow<LocalSyncSettings>,
    snackbarState: SnackbarHostState,
    currentBundle: ResourceBundle
) = ApplicationState(
    initialClient = AnimationGardenClient.Factory.create {
        proxy = currentAppSettings.proxy.toKtorProxy()
    },
    appDataSynchronizer = { dataScope ->
        val sync = currentAppSettings.sync
        AppDataSynchronizerImpl(
            dataScope.coroutineContext,
            remoteSynchronizerFactory = { applyMutation ->
                sync.createRemoteSynchronizer(
                    httpClient = createHttpClient({
                        if (currentAppSettings.sync.remoteSync.useProxy) {
                            proxy = currentAppSettings.proxy.toKtorProxy()
                        }
                    }),
                    localRef = createFileDelegatedMutableProperty(
                        File(
                            projectDirectories.dataDir,
                            "commit"
                        ).also {
                            it.parentFile.mkdirs()
                            logger.trace { "Commit file: ${it.absolutePath}" }
                        }
                    ).map(
                        get = { CommitRef(it) },
                        set = { it.toString() },
                    ),
                    promptConflict = { onConflict(dialogHost) },
                    applyMutation = applyMutation,
                    parentCoroutineContext = dataScope.coroutineContext
                )
            },
            backingStorage = sync.createLocalStorage(
                File(projectDirectories.dataDir, "app.dat").also {
                    it.parentFile.mkdirs()
                    logger.trace { "Data file: ${it.absolutePath}" }
                }
            ),
            localSyncSettingsFlow = localSyncSettingsFlow,
            promptSwitchToOffline = { e, optional ->
                onSwitchToOffline(
                    dialogHost, dataScope, snackbarState, currentBundle,
                    e, optional
                )
            },
            promptDataCorrupted = { onDataCorrupted(dataScope, snackbarState, currentBundle, it) },
        )
    }
)

private suspend fun onConflict(dialogHost: DialogHost): ConflictAction {
    val result = dialogHost.showConfirmationDialog(
        title = { LocalI18n.current.getString("sync.conflict.dialog.title") },
        confirmButtonText = {
            Text(LocalI18n.current.getString("sync.conflict.dialog.useServer"))
        },
        cancelButtonText = {
            Text(LocalI18n.current.getString("sync.conflict.dialog.useLocal"))
        },
    ) {
        Text(LocalI18n.current.getString("sync.conflict.dialog.content"))
    }
    return when (result) {
        DialogResult.CANCELED -> ConflictAction.StayOffline
        DialogResult.DISMISSED -> ConflictAction.AcceptClient
        DialogResult.CONFIRMED -> ConflictAction.AcceptServer
    }
}

private fun onDataCorrupted(
    uiScope: CoroutineScope,
    snackbarState: SnackbarHostState,
    currentBundle: ResourceBundle,
    exception: Exception,
) {
    uiScope.launch(Dispatchers.Main) {
        snackbarState.showSnackbar(
            String.format(
                currentBundle.getString("sync.data.corrupted"),
                exception.message ?: exception.toString()
            ),
            duration = SnackbarDuration.Long
        )
    }
}

private suspend fun onSwitchToOffline(
    dialogHost: DialogHost,
    uiScope: CoroutineScope,
    snackbarState: SnackbarHostState,
    currentBundle: ResourceBundle,
    exception: Exception,
    optional: Boolean,
): Boolean {
    logger.warn(exception) { "Switching to local mode" }

    return if (optional) {
        val result = dialogHost.showConfirmationDialog(
            title = { LocalI18n.current.getString("sync.failed.title") },
            confirmButtonText = {
                Text(LocalI18n.current.getString("sync.failed.switch.to.offline"))
            },
            cancelButtonText = {
                Text(LocalI18n.current.getString("sync.failed.revoke"))
            }
        ) {
            Text(
                String.format(
                    LocalI18n.current.getString("sync.failed.content"),
                    exception.message ?: exception.toString()
                )
            )
        }
        when (result) {
            DialogResult.CANCELED,
            DialogResult.DISMISSED -> {
                uiScope.launch(Dispatchers.Main) {
                    snackbarState.showSnackbar(
                        currentBundle.getString("sync.failed.revoked"),
                        duration = SnackbarDuration.Long
                    )
                }
                false
            }
            DialogResult.CONFIRMED -> {
                uiScope.launch(Dispatchers.Main) {
                    snackbarState.showSnackbar(
                        currentBundle.getString("sync.failed.switched.to.offline"),
                        duration = SnackbarDuration.Long
                    )
                }
                true
            }
        }
    } else {
        uiScope.launch(Dispatchers.Main) {
            snackbarState.showSnackbar(
                String.format(
                    currentBundle.getString("sync.failed.switched.to.offline.due.to"),
                    exception.message ?: exception.toString()
                ),
                duration = SnackbarDuration.Long
            )
        }
        true
    }
}


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
                MainPage(app, innerPadding = paddingByWindowSize, onClickProxySettings = onClickProxySettings)
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
            appDataSynchronizer = { createTestAppDataSynchronizer(it) }
        )
    }
    ProvideCompositionLocalsForPreview {
        MainWindowContent(hostIsMacOs = false, windowImmersed = false, app, onClickProxySettings = {})
    }
}
