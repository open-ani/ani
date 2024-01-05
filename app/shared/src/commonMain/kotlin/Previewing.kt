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

package me.him188.animationgarden.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.text.intl.Locale
import io.kamel.image.config.LocalKamelConfig
import me.him188.animationgarden.app.app.AppSettings
import me.him188.animationgarden.app.app.AppSettingsManager
import me.him188.animationgarden.app.app.LocalAppSettingsManager
import me.him188.animationgarden.app.i18n.LocalI18n
import me.him188.animationgarden.app.i18n.loadResourceBundle
import me.him188.animationgarden.app.platform.Context
import me.him188.animationgarden.app.platform.LocalContext
import me.him188.animationgarden.app.ui.foundation.DefaultKamelConfig
import me.him188.animationgarden.datasources.api.SubjectProvider
import me.him188.animationgarden.datasources.bangumi.BangumiClient
import me.him188.animationgarden.datasources.bangumi.BangumiSubjectProvider
import me.him188.animationgarden.datasources.dmhy.DmhyClient
import org.koin.core.context.startKoin
import org.koin.dsl.module

@Composable
fun ProvideCompositionLocalsForPreview(content: @Composable () -> Unit) {
    startKoin {
        modules(module {
            single<DmhyClient> { DmhyClient.create { } }
            single<BangumiClient> { BangumiClient.create() }
            single<SubjectProvider> { BangumiSubjectProvider() }
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
    val context: Context = LocalContext.current
    val currentBundle = remember(Locale.current.language) { loadResourceBundle(context) }
    CompositionLocalProvider(
        LocalI18n provides currentBundle,
        LocalAppSettingsManager provides appSettingsManager,
        LocalKamelConfig provides DefaultKamelConfig
    ) {
        content()
    }
}
