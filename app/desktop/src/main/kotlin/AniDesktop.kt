/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.desktop

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.LocalPlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import me.him188.ani.app.data.models.preference.configIfEnabledOrNull
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.domain.media.fetch.MediaSourceManager
import me.him188.ani.app.domain.media.resolver.DesktopWebVideoSourceResolver
import me.him188.ani.app.domain.media.resolver.HttpStreamingVideoSourceResolver
import me.him188.ani.app.domain.media.resolver.LocalFileVideoSourceResolver
import me.him188.ani.app.domain.media.resolver.TorrentVideoSourceResolver
import me.him188.ani.app.domain.media.resolver.VideoSourceResolver
import me.him188.ani.app.domain.session.SessionManager
import me.him188.ani.app.domain.torrent.DefaultTorrentManager
import me.him188.ani.app.domain.torrent.TorrentManager
import me.him188.ani.app.domain.update.UpdateManager
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.navigation.DesktopBrowserNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.AniBuildConfigDesktop
import me.him188.ani.app.platform.AniCefApp
import me.him188.ani.app.platform.AppStartupTasks
import me.him188.ani.app.platform.DesktopContext
import me.him188.ani.app.platform.ExtraWindowProperties
import me.him188.ani.app.platform.GrantedPermissionManager
import me.him188.ani.app.platform.JvmLogHelper
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.PermissionManager
import me.him188.ani.app.platform.PlatformWindow
import me.him188.ani.app.platform.createAppRootCoroutineScope
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.platform.getCommonKoinModule
import me.him188.ani.app.platform.notification.NoopNotifManager
import me.him188.ani.app.platform.notification.NotifManager
import me.him188.ani.app.platform.startCommonKoinModule
import me.him188.ani.app.platform.window.setTitleBarColor
import me.him188.ani.app.tools.update.DesktopUpdateInstaller
import me.him188.ani.app.tools.update.UpdateInstaller
import me.him188.ani.app.ui.foundation.LocalImageLoader
import me.him188.ani.app.ui.foundation.LocalWindowState
import me.him188.ani.app.ui.foundation.getDefaultImageLoader
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.layout.LocalPlatformWindow
import me.him188.ani.app.ui.foundation.layout.isSystemInFullscreen
import me.him188.ani.app.ui.foundation.navigation.LocalOnBackPressedDispatcherOwner
import me.him188.ani.app.ui.foundation.navigation.SkikoOnBackPressedDispatcherOwner
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
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
import me.him188.ani.utils.io.inSystem
import me.him188.ani.utils.io.toKtPath
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.platform.currentPlatform
import me.him188.ani.utils.platform.currentPlatformDesktop
import me.him188.ani.utils.platform.isMacOS
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import java.io.File
import kotlin.time.measureTime


private val logger by lazy { logger("Ani") }
private inline val toplevelLogger get() = logger

object AniDesktop {
//    init {
    // 如果要在视频上面显示弹幕或者播放按钮需要在启动的时候设置 system's blending 并且使用1.6.1之后的 Compose 版本
    // system's blending 在windows 上还是有问题，使用 EmbeddedMediaPlayerComponent 还是不会显示视频，但是在Windows 系统上使用 CallbackMediaPlayerComponent 就没问题。
    // See https://github.com/open-ani/ani/issues/115#issuecomment-2092567727
//        System.setProperty("compose.interop.blending", "true")
//    }


    private fun calculateWindowSize(
        desiredWidth: Dp,
        desiredHeight: Dp,
        screenSize: DpSize = ScreenUtils.getScreenSize()
    ): DpSize {
        return DpSize(
            width = if (desiredWidth > screenSize.width) screenSize.width else desiredWidth,
            height = if (desiredHeight > screenSize.height) screenSize.height else desiredHeight,
        )
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("dataDir: file://${projectDirectories.dataDir.replace(" ", "%20")}")
        println("cacheDir: file://${projectDirectories.cacheDir.replace(" ", "%20")}")
        val logsDir = File(projectDirectories.dataDir).resolve("logs").apply { mkdirs() }
        println("logsDir: file://${logsDir.absolutePath.replace(" ", "%20")}")

        Log4j2Config.configureLogging(logsDir)
        kotlin.runCatching {
            JvmLogHelper.deleteOldLogs(logsDir.toPath())
        }.onFailure {
            logger.error(it) { "Failed to delete old logs" }
        }

        if (AniBuildConfigDesktop.isDebug) {
            logger.info { "Debug mode enabled" }
        }
        logger.info { "Ani platform: ${currentPlatform().name}, version: ${currentAniBuildConfig.versionName}" }

        val defaultSize = DpSize(1301.dp, 855.dp)
        // Get the screen size as a Dimension object
        val windowState = WindowState(
            size = kotlin.runCatching {
                calculateWindowSize(defaultSize.width, defaultSize.height)
            }.onFailure {
                logger.error(it) { "Failed to calculate window size" }
            }.getOrElse {
                defaultSize
            },
            position = WindowPosition.Aligned(Alignment.Center),
        )
        val context = DesktopContext(
            windowState,
            File(projectDirectories.dataDir),
            File(projectDirectories.dataDir),
            logsDir,
            ExtraWindowProperties(),
        )

        val time = measureTime {
            SingleInstanceChecker.instance.ensureSingleInstance()
        }
        logger.info { "Single instance check took $time" }

        val coroutineScope = createAppRootCoroutineScope()

        coroutineScope.launch(Dispatchers.IO) {
            // since 3.4.0, anitorrent 增加后不兼容 QB 数据
            File(projectDirectories.cacheDir).resolve("torrent").let {
                if (it.exists()) {
                    it.deleteRecursively()
                }
            }
        }

        val koin = startKoin {
            modules(getCommonKoinModule({ context }, coroutineScope))
            modules(
                module {
//                single<SubjectNavigator> { AndroidSubjectNavigator() }
//                single<AuthorizationNavigator> { AndroidAuthorizationNavigator() }
//                single<BrowserNavigator> { AndroidBrowserNavigator() }
                    single<TorrentManager> {
                        DefaultTorrentManager.create(
                            coroutineScope.coroutineContext,
                            get(),
                            baseSaveDir = {
                                val saveDir = runBlocking {
                                    get<SettingsRepository>().mediaCacheSettings.flow.first().saveDir
                                        ?.let(::Path)
                                } ?: projectDirectories.torrentCacheDir.toKtPath()
                                toplevelLogger.info { "TorrentManager saveDir: $saveDir" }
                                saveDir.inSystem
                            },
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
                                .plus(
                                    DesktopWebVideoSourceResolver(
                                        context, 
                                        get<MediaSourceManager>().webVideoMatcherLoader
                                    )
                                ),
                        )
                    }
                    single<UpdateInstaller> { DesktopUpdateInstaller.currentOS() }
                    single<PermissionManager> { GrantedPermissionManager }
                    single<NotifManager> { NoopNotifManager }
                },
            )
        }.startCommonKoinModule(coroutineScope)

        // Initialize CEF application.
        coroutineScope.launch { 
            val proxySettings = koin.koin.get<SettingsRepository>()
                .proxySettings.flow
                .firstOrNull()
                ?.default
                ?.configIfEnabledOrNull
            
            AniCefApp.initialize(
                logDir = File(projectDirectories.dataDir).resolve("logs"),
                cacheDir = File(projectDirectories.cacheDir).resolve("jcef-cache"),
                proxyServer = proxySettings?.url,
                proxyAuthUsername = proxySettings?.authorization?.username,
                proxyAuthPassword = proxySettings?.authorization?.password
            )
        }
        
        // 预先加载 VLC, https://github.com/open-ani/ani/issues/618
        coroutineScope.launch {
            kotlin.runCatching {
                VlcjVideoPlayerState.prepareLibraries()
            }.onFailure {
                logger.error(it) { "Failed to prepare VLC" }
            }
        }

        kotlin.runCatching {
            val desktopUpdateInstaller = koin.koin.get<UpdateInstaller>() as DesktopUpdateInstaller
            desktopUpdateInstaller.deleteOldUpdater()
        }.onFailure {
            logger.error(it) { "Failed to delete update installer" }
        }

        kotlin.runCatching {
            koin.koin.get<UpdateManager>().deleteInstalledFiles()
        }.onFailure {
            logger.error(it) { "Failed to delete installed files" }
        }

        val navigator = AniNavigator()

        coroutineScope.launch {
            navigator.awaitNavController()
            val sessionManager by koin.koin.inject<SessionManager>()
            AppStartupTasks.verifySession(sessionManager, navigator)
        }

        application {
            Window(
                onCloseRequest = { exitApplication() },
                state = windowState,
                title = "Ani",
                icon = painterResource(Res.drawable.a_round),
            ) {
                val lifecycleOwner = LocalLifecycleOwner.current
                val backPressedDispatcherOwner = remember {
                    SkikoOnBackPressedDispatcherOwner(navigator, lifecycleOwner)
                }

                SideEffect {
                    // 防止闪眼
                    window.background = java.awt.Color.BLACK
                    window.contentPane.background = java.awt.Color.BLACK

                    logger.info {
                        "renderApi: " + this.window.renderApi
                    }
                }
                CompositionLocalProvider(
                    LocalContext provides context,
                    LocalWindowState provides windowState,
                    LocalPlatformWindow provides remember(window.windowHandle) {
                        PlatformWindow(
                            windowHandle = window.windowHandle,
                            windowScope = this,
                        )
                    },
                    LocalOnBackPressedDispatcherOwner provides backPressedDispatcherOwner,
                ) {
                    // This actually runs only once since app is never changed.
                    val windowImmersed = true

                    SideEffect {
                        // https://www.formdev.com/flatlaf/macos/
                        if (currentPlatformDesktop().isMacOS()) {
                            window.rootPane.putClientProperty("apple.awt.application.appearance", "system")
                            window.rootPane.putClientProperty("apple.awt.fullscreenable", true)
                            if (windowImmersed) {
                                window.rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
                                window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                                window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                            } else {
                                window.rootPane.putClientProperty("apple.awt.fullWindowContent", false)
                                window.rootPane.putClientProperty("apple.awt.transparentTitleBar", false)
                            }
                        }
                    }

                    MainWindowContent(navigator)
                }
            }

        }
        // unreachable here
    }

}


@Composable
private fun FrameWindowScope.MainWindowContent(
    aniNavigator: AniNavigator,
) {
    AniApp {
        val proxyConfig = KoinPlatform.getKoin().get<SettingsRepository>().proxySettings.flow.map {
            it.default.configIfEnabledOrNull
        }
        val proxy by proxyConfig.collectAsStateWithLifecycle(null)

        val coilContext = LocalPlatformContext.current
        val imageLoader by remember(coilContext) {
            derivedStateOf {
                getDefaultImageLoader(coilContext, proxyConfig = proxy)
            }
        }

        window.setTitleBarColor(AniThemeDefaults.navigationContainerColor)

        Box(
            Modifier
                .ifThen(!isSystemInFullscreen()) {
                    statusBarsPadding() // Windows 有, macOS 没有
                }
                .fillMaxSize(),
        ) {
            Box(Modifier.fillMaxSize()) {
                val paddingByWindowSize by animateDpAsState(0.dp)

                val vm = viewModel { ToastViewModel() }

                val showing by vm.showing.collectAsStateWithLifecycle()
                val content by vm.content.collectAsStateWithLifecycle()

                CompositionLocalProvider(
                    LocalNavigator provides aniNavigator,
                    LocalToaster provides object : Toaster {
                        override fun toast(text: String) {
                            vm.show(text)
                        }
                    },
                    LocalImageLoader provides imageLoader,
                ) {
                    Box(Modifier.padding(all = paddingByWindowSize)) {
                        AniAppContent(aniNavigator)
                        Toast({ showing }, { Text(content) })
                    }
                }
            }
        }
    }
}
