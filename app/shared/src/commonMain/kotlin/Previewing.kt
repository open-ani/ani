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

package me.him188.ani.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import me.him188.ani.app.app.AppSettings
import me.him188.ani.app.app.AppSettingsManager
import me.him188.ani.app.platform.createBangumiClient
import me.him188.ani.app.ui.foundation.AniApp
import me.him188.ani.datasources.api.DownloadProvider
import me.him188.ani.datasources.api.SubjectProvider
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.BangumiSubjectProvider
import me.him188.ani.datasources.dmhy.DmhyClient
import me.him188.ani.datasources.dmhy.DmhyDownloadProvider
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@Composable
fun ProvideCompositionLocalsForPreview(
    content: @Composable () -> Unit,
) {
    runCatching { stopKoin() }
    startKoin {
        modules(module {
            single<DmhyClient> { DmhyClient.create { } }
            single<BangumiClient> { createBangumiClient() }
            single<SubjectProvider> { BangumiSubjectProvider(get<BangumiClient>()) }
            single<DownloadProvider> { DmhyDownloadProvider() }
        })
    }
    val appSettingsManager = remember {
        object : AppSettingsManager() {
            @Volatile
            private var dummyStorage: AppSettings = AppSettings()
            override fun loadImpl(): AppSettings {
                return dummyStorage
            }

            override fun saveImpl(instance: AppSettings) {
                dummyStorage = instance
            }
        }

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