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

package me.him188.ani.app.desktop

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import dev.dirs.ProjectDirectories
import kotlinx.coroutines.launch
import me.him188.ani.app.data.media.resolver.DesktopWebVideoSourceResolver
import me.him188.ani.app.data.media.resolver.HttpStreamingVideoSourceResolver
import me.him188.ani.app.data.media.resolver.LocalFileVideoSourceResolver
import me.him188.ani.app.data.media.resolver.TorrentVideoSourceResolver
import me.him188.ani.app.data.media.resolver.VideoSourceResolver
import me.him188.ani.app.data.update.UpdateManager
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.navigation.DesktopBrowserNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.DesktopContext
import me.him188.ani.app.platform.ExtraWindowProperties
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.createAppRootCoroutineScope
import me.him188.ani.app.platform.getCommonKoinModule
import me.him188.ani.app.platform.notification.NoopNotifManager
import me.him188.ani.app.platform.notification.NotifManager
import me.him188.ani.app.platform.startCommonKoinModule
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.tools.torrent.DefaultTorrentManager
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.tools.update.DesktopUpdateInstaller
import me.him188.ani.app.tools.update.UpdateInstaller
import me.him188.ani.app.ui.foundation.LocalWindowState
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.interaction.PlatformImplementations
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.foundation.theme.AppTheme
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.Toast
import me.him188.ani.app.ui.foundation.widgets.ToastViewModel
import me.him188.ani.app.ui.foundation.widgets.Toaster
import me.him188.ani.app.ui.main.AniApp
import me.him188.ani.app.ui.main.AniAppContent
import me.him188.ani.app.videoplayer.ui.VlcjVideoPlayerState
import me.him188.ani.app.videoplayer.ui.state.PlayerStateFactory
import me.him188.ani.desktop.generated.resources.Res
import me.him188.ani.desktop.generated.resources.a_round
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File

private val logger = logger("Ani")


val projectDirectories: ProjectDirectories by lazy {
    ProjectDirectories.from(
        "me",
        "Him188",
        "Ani",
    )
}

object AniDesktop {
    init {
        // 如果要在视频上面显示弹幕或者播放按钮需要在启动的时候设置 system's blending 并且使用1.6.1之后的 Compose 版本
        // system's blending 在windows 上还是有问题，使用 EmbeddedMediaPlayerComponent 还是不会显示视频，但是在Windows 系统上使用 CallbackMediaPlayerComponent 就没问题。
        // See https://github.com/open-ani/ani/issues/115#issuecomment-2092567727
//        System.setProperty("compose.interop.blending", "true")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("dataDir: file://${projectDirectories.dataDir.replace(" ", "%20")}")
        println("cacheDir: file://${projectDirectories.cacheDir.replace(" ", "%20")}")
        val logsDir = File(projectDirectories.dataDir).resolve("logs").apply { mkdirs() }
        println("logsDir: file://${logsDir.absolutePath.replace(" ", "%20")}")

        Log4j2Config.configureLogging(logsDir)

        val windowState = WindowState(
            size = DpSize(800.dp * 1.3f, 800.dp),
            position = WindowPosition.Aligned(Alignment.Center),
        )
        val context = DesktopContext(
            windowState,
            File(projectDirectories.dataDir),
            File(projectDirectories.dataDir),
            logsDir,
            ExtraWindowProperties(initialUndecorated = false),
        )

        val coroutineScope = createAppRootCoroutineScope()

        val koin = startKoin {
            modules(getCommonKoinModule({ context }, coroutineScope))
            modules(
                module {
//                single<SubjectNavigator> { AndroidSubjectNavigator() }
//                single<AuthorizationNavigator> { AndroidAuthorizationNavigator() }
//                single<BrowserNavigator> { AndroidBrowserNavigator() }
                    single<TorrentManager> {
                        DefaultTorrentManager(
                            coroutineScope.coroutineContext,
                            saveDir = { File(projectDirectories.cacheDir).resolve("torrent") },
                        )
                    }
                    single<PlayerStateFactory> {
                        PlayerStateFactory { _, ctx ->
                            VlcjVideoPlayerState(ctx)
                        }
                    }
                    single<BrowserNavigator> { DesktopBrowserNavigator() }
                    factory<VideoSourceResolver> {
                        VideoSourceResolver.from(
                            get<TorrentManager>().engines
                                .map { TorrentVideoSourceResolver(it) }
                                .plus(LocalFileVideoSourceResolver())
                                .plus(HttpStreamingVideoSourceResolver())
                                .plus(DesktopWebVideoSourceResolver()),
                        )
                    }
                    single<UpdateInstaller> { DesktopUpdateInstaller.currentOS() }
                    single<NotifManager> { NoopNotifManager }
                },
            )
        }.startCommonKoinModule(coroutineScope)

        kotlin.runCatching {
            koin.koin.get<UpdateManager>().deleteInstalledFiles()
        }.onFailure {
            logger.error(it) { "Failed to delete installed files" }
        }

        val navigator = AniNavigator()

        coroutineScope.launch {
            val sessionManager by koin.koin.inject<SessionManager>()
            logger.info { "[AutoLogin] Waiting for awaitNavigator" }
            navigator.awaitNavigator()
            logger.info { "[AutoLogin] Got navigator, start requireOnline" }
            sessionManager.requireOnline(navigator)
        }

        application {
            Window(
                onCloseRequest = { exitApplication() },
                state = windowState,
                title = "Ani",
                icon = painterResource(Res.drawable.a_round),
                undecorated = context.extraWindowProperties.undecorated,
            ) {
                SideEffect {
                    logger.info {
                        "renderApi: " + this.window.renderApi
                    }
                }
                CompositionLocalProvider(
                    LocalContext provides context,
                    LocalWindowState provides windowState,
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
                        navigator,
                    )
                }
            }

        }
        // unreachable here
    }

}


@Composable
private fun MainWindowContent(
    hostIsMacOs: Boolean,
    windowImmersed: Boolean,
    aniNavigator: AniNavigator,
) {
    AniApp {
        val window by rememberUpdatedState(LocalWindowState.current)
        val isFullscreen by remember {
            derivedStateOf {
                window.placement != WindowPlacement.Fullscreen
            }
        }
        Box(
            Modifier.background(color = AppTheme.colorScheme.background)
                .ifThen(!isFullscreen) {
                    statusBarsPadding()
                }
                .padding(top = if (hostIsMacOs && windowImmersed && isFullscreen) 28.dp else 0.dp) // safe area for macOS if windowImmersed
                .fillMaxSize(),
        ) {
            Box(Modifier.fillMaxSize()) {
                val paddingByWindowSize by animateDpAsState(0.dp)

                val vm = rememberViewModel { ToastViewModel() }

                val showing by vm.showing.collectAsStateWithLifecycle()
                val content by vm.content.collectAsStateWithLifecycle()

                CompositionLocalProvider(
                    LocalNavigator provides aniNavigator,
                    LocalToaster provides object : Toaster {
                        override fun toast(text: String) {
                            vm.show(text)
                        }
                    },
                ) {
                    Box(Modifier.padding(all = paddingByWindowSize)) {
                        AniAppContent(aniNavigator)
                        Toast(showing, content)
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
