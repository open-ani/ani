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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.text.intl.Locale
import me.him188.ani.app.app.AppSettings
import me.him188.ani.app.app.AppSettingsManager
import me.him188.ani.app.app.LocalAppSettingsManager
import me.him188.ani.app.i18n.LocalI18n
import me.him188.ani.app.i18n.loadResourceBundle
import me.him188.ani.app.platform.Context
import me.him188.ani.app.platform.LocalContext

@Composable
fun ProvideCompositionLocalsForPreview(content: @Composable () -> Unit) {
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
        LocalAppSettingsManager provides appSettingsManager
    ) {
        content()
    }
}
