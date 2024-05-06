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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import io.ktor.client.HttpClient
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.him188.ani.app.interaction.PlatformImplementations
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.navigation.DesktopBrowserNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.DesktopContext
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.createAppRootCoroutineScope
import me.him188.ani.app.platform.getAniUserAgent
import me.him188.ani.app.platform.getCommonKoinModule
import me.him188.ani.app.platform.startCommonKoinModule
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.tools.torrent.DefaultTorrentManager
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.torrent.qbittorrent.QBittorrentClientConfig
import me.him188.ani.app.torrent.qbittorrent.QBittorrentTorrentDownloader
import me.him188.ani.app.ui.foundation.AniApp
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.main.AniAppContent
import me.him188.ani.app.ui.theme.AppTheme
import me.him188.ani.app.ui.theme.aniColorScheme
import me.him188.ani.app.videoplayer.ui.VlcjVideoPlayerState
import me.him188.ani.app.videoplayer.ui.state.PlayerStateFactory
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
    init {
        // 如果要在视频上面显示弹幕或者播放按钮需要在启动的时候设置 system's blending 并且使用1.6.1之后的 Compose 版本
        // system's blending 在windows 上还是有问题，使用 EmbeddedMediaPlayerComponent 还是不会显示视频，但是在Windows 系统上使用 CallbackMediaPlayerComponent 就没问题。
        // See https://github.com/open-ani/ani/issues/115#issuecomment-2092567727
        System.setProperty("compose.interop.blending", "true")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("dataDir: ${projectDirectories.dataDir}")
        println("cacheDir: ${projectDirectories.cacheDir}")
        val context = DesktopContext(
            File(projectDirectories.dataDir),
            File(projectDirectories.dataDir)
        )

        val coroutineScope = createAppRootCoroutineScope()

        val client = HttpClient {
            followRedirects = true
            install(UserAgent) {
                agent = getAniUserAgent()
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        val koin = startKoin {
            modules(getCommonKoinModule({ context }, coroutineScope))
            modules(module {
//                single<SubjectNavigator> { AndroidSubjectNavigator() }
//                single<AuthorizationNavigator> { AndroidAuthorizationNavigator() }
//                single<BrowserNavigator> { AndroidBrowserNavigator() }
                single<TorrentManager> {
                    DefaultTorrentManager(
                        coroutineScope.coroutineContext,
                        downloaderFactory = {
                            QBittorrentTorrentDownloader(
                                QBittorrentClientConfig(
                                    // TODO:
                                    baseUrl = "http://127.0.0.1:7212"
                                ),
                                saveDir = File(projectDirectories.dataDir).resolve("torrent"),
                                parentCoroutineContext = coroutineScope.coroutineContext,
                            )
//                            Libtorrent4jTorrentDownloader(
//                                cacheDirectory = File(projectDirectories.cacheDir).resolve("torrent"),
//                                downloadFile = { url ->
//                                    client.get(url).apply {
//                                        check(status.isSuccess()) {
//                                            "Failed to download torrent file, resp=${
//                                                bodyAsChannel().readRemaining().readBytes()
//                                            }"
//                                        }
//                                    }.bodyAsChannel().readRemaining().readBytes()
//                                },
//                                config = TorrentDownloaderConfig(
//                                    peerFingerprint = computeTorrentFingerprint(),
//                                    userAgent = computeTorrentUserAgent(),
//                                    isDebug = currentAniBuildConfig.isDebug,
//                                ),
//                                parentCoroutineContext = coroutineScope.coroutineContext,
//                            )
                        }
                    )
                }
                single<PlayerStateFactory> {
                    PlayerStateFactory { _, _ ->
                        VlcjVideoPlayerState()
                    }
                }
                single<BrowserNavigator> { DesktopBrowserNavigator() }
            })
        }.startCommonKoinModule(coroutineScope)

        val navigator = AniNavigator()

        val sessionManager by koin.koin.inject<SessionManager>()

        application(exitProcessOnExit = true) {
            val platform = remember { PlatformImplementations.current }
            val mainSnackbar = remember { SnackbarHostState() }
            CompositionLocalProvider(LocalContext provides context) {
                content(navigator)
            }

            LaunchedEffect(true) {
                coroutineScope.launch {
                    sessionManager.requireOnline(navigator)
                }
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
                height = 1920.dp / 2.2f,
                width = 1080.dp / 2.2f,
            ),
            resizable = true,
        ) {
            this.window
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
    AniApp(aniColorScheme()) {
        Box(
            Modifier.background(color = AppTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(top = if (hostIsMacOs && windowImmersed) 28.dp else 0.dp) // safe area for macOS if windowImmersed
                .fillMaxSize()
        ) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val paddingByWindowSize by animateDpAsState(
                    0.dp
//                    if (maxWidth > 400.dp) {
//                        16.dp
//                    } else {
//                        8.dp
//                    },
                )

                CompositionLocalProvider(LocalNavigator provides aniNavigator) {
                    Box(Modifier.padding(all = paddingByWindowSize)) {
                        AniAppContent(aniNavigator)
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
