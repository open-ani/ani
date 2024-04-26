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

package me.him188.ani.app.ui.foundation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.getCommonKoinModule
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.session.TestSessionManagers
import me.him188.ani.app.torrent.api.TorrentDownloaderFactory
import me.him188.ani.app.torrent.torrent4j.Libtorrent4jTorrentDownloader
import me.him188.ani.app.ui.theme.aniColorScheme
import me.him188.ani.app.videoplayer.ui.state.DummyPlayerState
import me.him188.ani.app.videoplayer.ui.state.PlayerStateFactory
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.io.path.createTempDirectory

val LocalIsPreviewing = compositionLocalOf {
    false
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun ProvideCompositionLocalsForPreview(
    isDark: Boolean = isSystemInDarkTheme(),
    playerStateFactory: PlayerStateFactory = PlayerStateFactory { _, _ ->
        DummyPlayerState()
    },
    module: Module.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    MaterialTheme {
        PlatformPreviewCompositionLocalProvider {
            val context = LocalContext.current
            runCatching { stopKoin() }
            val scope = GlobalScope
            startKoin {
                modules(getCommonKoinModule({ context }, scope))
                modules(module {
                    single<TorrentDownloaderFactory> {
                        TorrentDownloaderFactory {
                            Libtorrent4jTorrentDownloader(
                                cacheDirectory = createTempDirectory("ani-temp").toFile(),
                                downloadFile = { _ -> byteArrayOf() },
                            )
                        }
                    }
                    single<PlayerStateFactory> {
                        playerStateFactory
                    }
                    single<SessionManager> { TestSessionManagers.Online }
                    module()
                })
            }
            val aniNavigator = remember { AniNavigator() }
            CompositionLocalProvider(
                LocalIsPreviewing provides true,
                LocalNavigator provides aniNavigator,
            ) {
                AniApp(aniColorScheme(isDark)) {
                    content()
                }
            }
        }
    }
}

@Composable
expect fun PlatformPreviewCompositionLocalProvider(content: @Composable () -> Unit)