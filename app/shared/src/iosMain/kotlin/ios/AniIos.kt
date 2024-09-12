package me.him188.ani.app.ios

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.io.files.Path
import me.him188.ani.app.data.source.media.resolver.HttpStreamingVideoSourceResolver
import me.him188.ani.app.data.source.media.resolver.LocalFileVideoSourceResolver
import me.him188.ani.app.data.source.media.resolver.TorrentVideoSourceResolver
import me.him188.ani.app.data.source.media.resolver.VideoSourceResolver
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.navigation.NoopBrowserNavigator
import me.him188.ani.app.platform.GrantedPermissionManager
import me.him188.ani.app.platform.IosContext
import me.him188.ani.app.platform.IosContextFiles
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.PermissionManager
import me.him188.ani.app.platform.createAppRootCoroutineScope
import me.him188.ani.app.platform.getCommonKoinModule
import me.him188.ani.app.platform.isSystemInFullscreen
import me.him188.ani.app.platform.navigation.LocalOnBackPressedDispatcherOwner
import me.him188.ani.app.platform.navigation.OnBackPressedDispatcherOwner
import me.him188.ani.app.platform.notification.NoopNotifManager
import me.him188.ani.app.platform.notification.NotifManager
import me.him188.ani.app.platform.startCommonKoinModule
import me.him188.ani.app.platform.window.LocalPlatformWindow
import me.him188.ani.app.platform.window.PlatformWindow
import me.him188.ani.app.tools.torrent.DefaultTorrentManager
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.tools.update.IosUpdateInstaller
import me.him188.ani.app.tools.update.UpdateInstaller
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.Toast
import me.him188.ani.app.ui.foundation.widgets.ToastViewModel
import me.him188.ani.app.ui.foundation.widgets.Toaster
import me.him188.ani.app.ui.main.AniApp
import me.him188.ani.app.ui.main.AniAppContent
import me.him188.ani.app.videoplayer.ui.state.DummyPlayerState
import me.him188.ani.app.videoplayer.ui.state.PlayerStateFactory
import me.him188.ani.utils.io.SystemDocumentDir
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.inSystem
import me.him188.ani.utils.io.resolve
import org.koin.core.context.startKoin
import org.koin.dsl.module
import platform.UIKit.UIViewController

// Workaround for Kotlin bug: iosMain cannot resolve actualized skikoMain code
expect fun OnBackPressedDispatcherOwner(aniNavigator: AniNavigator): OnBackPressedDispatcherOwner

@Suppress("FunctionName", "unused") // used in Swift
fun MainViewController(): UIViewController {
    val scope = createAppRootCoroutineScope()

    val context = IosContext(
        IosContextFiles(
            cacheDir = Path(SystemDocumentDir).resolve("cache").inSystem,
            dataDir = Path(SystemDocumentDir).resolve("data").inSystem,
        ),
    ) // TODO IOS

    val koin = startKoin {
        modules(getCommonKoinModule({ context }, scope))
        modules(getIosModules(Path(SystemDocumentDir).resolve("torrent").inSystem, scope)) // TODO IOS
    }.startCommonKoinModule(scope).koin

    koin.get<TorrentManager>() // start sharing, connect to DHT now

    val aniNavigator = AniNavigator()
    val onBackPressedDispatcherOwner = OnBackPressedDispatcherOwner(aniNavigator)

    return ComposeUIViewController {
        AniApp {
            CompositionLocalProvider(
                LocalContext provides context,
                LocalPlatformWindow provides remember {
                    PlatformWindow()
                },
                LocalOnBackPressedDispatcherOwner provides onBackPressedDispatcherOwner,
            ) {
                Box(
                    Modifier.background(color = MaterialTheme.colorScheme.background)
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
                            LocalToaster provides remember {
                                object : Toaster {
                                    override fun toast(text: String) {
                                        vm.show(text)
                                    }
                                }
                            },
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
    }
}

fun getIosModules(
    defaultTorrentCacheDir: SystemPath,
    coroutineScope: CoroutineScope,
) = module {
    single<PermissionManager> {
        GrantedPermissionManager
    }
    single<NotifManager> {
        NoopNotifManager
    }
    single<BrowserNavigator> { NoopBrowserNavigator }
    single<TorrentManager> {
        DefaultTorrentManager.create(
            coroutineScope.coroutineContext,
            get(),
            baseSaveDir = { defaultTorrentCacheDir },
        )
    }
    single<PlayerStateFactory> {
        PlayerStateFactory { _, _ ->
            DummyPlayerState()
        }
    }


    factory<VideoSourceResolver> {
        VideoSourceResolver.from(
            get<TorrentManager>().engines
                .map { TorrentVideoSourceResolver(it) }
                .plus(LocalFileVideoSourceResolver())
                .plus(HttpStreamingVideoSourceResolver()),
        )
    }
    single<UpdateInstaller> { IosUpdateInstaller }
}
