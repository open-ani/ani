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

package me.him188.ani.desktop

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.dirs.ProjectDirectories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.him188.ani.app.interaction.PlatformImplementations
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.DesktopContext
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.getCommonKoinModule
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.torrent.TorrentDownloader
import me.him188.ani.app.torrent.TorrentDownloaderFactory
import me.him188.ani.app.ui.foundation.AniApp
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.main.MainScreen
import me.him188.ani.app.ui.theme.AppTheme
import me.him188.ani.utils.logging.logger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File

private val logger = logger("Ani")


val projectDirectories: ProjectDirectories by lazy {
    ProjectDirectories.from(
        "me",
        "Him188",
        "Ani"
    )
}

object AniDesktop {
    @JvmStatic
    fun main(args: Array<String>) {
        val context = DesktopContext(
            File(projectDirectories.dataDir),
            File(projectDirectories.dataDir)
        )

        val coroutineScope = CoroutineScope(SupervisorJob())

        val koin = startKoin {
            modules(getCommonKoinModule({ context }, coroutineScope))
            modules(module {
//                single<SubjectNavigator> { AndroidSubjectNavigator() }
//                single<AuthorizationNavigator> { AndroidAuthorizationNavigator() }
//                single<BrowserNavigator> { AndroidBrowserNavigator() }
                single<TorrentDownloaderFactory> {
                    TorrentDownloaderFactory {
                        TorrentDownloader(
                            cacheDirectory = File(projectDirectories.cacheDir).resolve("torrent"),
                        )
                    }
                }
            })
        }

        val navigator = AniNavigator()

        val sessionManager by koin.koin.inject<SessionManager>()

        coroutineScope.launch {
            sessionManager.requireAuthorization(navigator, optional = true)
        }

        application(exitProcessOnExit = true) {
            val platform = remember { PlatformImplementations.current }
            val mainSnackbar = remember { SnackbarHostState() }
            CompositionLocalProvider(LocalContext provides context) {
                content(navigator)
            }
        }
    }

    @Composable
    private fun ApplicationScope.content(
        aniNavigator: AniNavigator,
    ) {
        Window(
            title = "ani",
            onCloseRequest = {
                exitApplication()
            },
            state = rememberWindowState(
                height = 1920.dp / 2,
                width = 1080.dp / 2,
            ),
            resizable = false,
        ) {
            // This actually runs only once since app is never changed.
            val windowImmersed = true
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
                hostIsMacOs = PlatformImplementations.hostIsMacOs,
                windowImmersed = windowImmersed,
                aniNavigator
            )
        }
    }
}


@Composable
private fun MainWindowContent(
    hostIsMacOs: Boolean,
    windowImmersed: Boolean,
    aniNavigator: AniNavigator,
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

            CompositionLocalProvider(LocalNavigator provides aniNavigator) {
                Box(Modifier.padding(all = paddingByWindowSize)) {
                    AniApp {
                        MainScreen(aniNavigator)
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewMainWindowMacOS() {
    ProvideCompositionLocalsForPreview {
        MainWindowContent(
            hostIsMacOs = false,
            windowImmersed = false,
            aniNavigator = remember { AniNavigator() },
        )
    }
}
