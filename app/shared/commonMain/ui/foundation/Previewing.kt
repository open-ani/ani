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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.getCommonKoinModule
import me.him188.ani.app.torrent.TorrentDownloader
import me.him188.ani.app.torrent.TorrentDownloaderFactory
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.io.path.createTempDirectory

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun ProvideCompositionLocalsForPreview(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    runCatching { stopKoin() }
    startKoin {
        modules(getCommonKoinModule({ context }, GlobalScope))
        modules(module {
            single<TorrentDownloaderFactory> {
                TorrentDownloaderFactory {
                    TorrentDownloader(
                        cacheDirectory = createTempDirectory("ani-temp").toFile(),
                    )
                }
            }
        })
    }
    MaterialTheme {
        PlatformPreviewCompositionLocalProvider {
            AniApp {
                content()
            }
        }
    }
}

@Composable
expect fun PlatformPreviewCompositionLocalProvider(content: @Composable () -> Unit)