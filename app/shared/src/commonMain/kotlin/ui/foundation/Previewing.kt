/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.io.files.Path
import me.him188.ani.app.data.repository.DanmakuRegexFilterRepository
import me.him188.ani.app.data.repository.DanmakuRegexFilterRepositoryImpl
import me.him188.ani.app.data.repository.PreferencesRepositoryImpl
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.domain.media.resolver.HttpStreamingVideoSourceResolver
import me.him188.ani.app.domain.media.resolver.LocalFileVideoSourceResolver
import me.him188.ani.app.domain.media.resolver.TorrentVideoSourceResolver
import me.him188.ani.app.domain.media.resolver.VideoSourceResolver
import me.him188.ani.app.domain.session.PreviewSessionManager
import me.him188.ani.app.domain.session.SessionManager
import me.him188.ani.app.domain.torrent.DefaultTorrentManager
import me.him188.ani.app.domain.torrent.TorrentManager
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.BrowserNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.navigation.NoopBrowserNavigator
import me.him188.ani.app.platform.GrantedPermissionManager
import me.him188.ani.app.platform.PermissionManager
import me.him188.ani.app.tools.ldc.MemoryDataStore
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.Toaster
import me.him188.ani.app.ui.main.AniApp
import me.him188.ani.app.videoplayer.ui.state.DummyPlayerState
import me.him188.ani.app.videoplayer.ui.state.PlayerStateFactory
import me.him188.ani.utils.io.inSystem
import me.him188.ani.utils.platform.annotations.TestOnly
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * 应当优先使用 [me.him188.ani.app.ui.foundation.ProvideFoundationCompositionLocalsForPreview].
 * 仅当 foundation 的不满足需求时才使用此方法.
 */
@OptIn(TestOnly::class)
@Composable
fun ProvideCompositionLocalsForPreview(
    module: Module.() -> Unit = {},
    colorScheme: ColorScheme? = null,
    content: @Composable () -> Unit,
) {
    ProvideFoundationCompositionLocalsForPreview {
        val coroutineScope = rememberCoroutineScope()
        runCatching { stopKoin() }
        startKoin {
//            modules(getCommonKoinModule({ context }, coroutineScope))
            modules(
                module {
                    single<PlayerStateFactory> {
                        PlayerStateFactory { _, _ ->
                            DummyPlayerState(coroutineScope.coroutineContext)
                        }
                    }
                    single<SessionManager> { PreviewSessionManager }
                    factory<VideoSourceResolver> {
                        VideoSourceResolver.from(
                            get<TorrentManager>().engines
                                .map { TorrentVideoSourceResolver(it) }
                                .plus(LocalFileVideoSourceResolver())
                                .plus(HttpStreamingVideoSourceResolver()),
                        )
                    }
                    single<TorrentManager> {
                        DefaultTorrentManager.create(
                            coroutineScope.coroutineContext,
                            get(),
                            baseSaveDir = { Path("preview-cache").inSystem },
                        )
                    }
                    single<PermissionManager> { GrantedPermissionManager }
                    single<BrowserNavigator> { NoopBrowserNavigator }
                    single<SettingsRepository> { PreferencesRepositoryImpl(MemoryDataStore(mutablePreferencesOf())) }
                    single<DanmakuRegexFilterRepository> {
                        DanmakuRegexFilterRepositoryImpl(
                            MemoryDataStore(listOf()),
                        )
                    }
                    module()
                },
            )
        }
        val aniNavigator = remember { AniNavigator() }

        CompositionLocalProvider(
            LocalIsPreviewing provides true,
            LocalNavigator provides aniNavigator,
            LocalImageViewerHandler provides rememberImageViewerHandler(),
            LocalToaster provides remember {
                object : Toaster {
                    override fun toast(text: String) {
                    }
                }
            },
            LocalLifecycleOwner provides remember {
                object : LifecycleOwner {
                    override val lifecycle: Lifecycle get() = TestGlobalLifecycle
                }
            },
        ) {
            val navController = rememberNavController()
            SideEffect {
                aniNavigator.setNavController(navController)
            }
            NavHost(navController, startDestination = "test") { // provide ViewModelStoreOwner
                composable("test") {
                    AniApp(overrideColorTheme = colorScheme) {
                        content()
                    }
                }
            }
        }
    }
}
