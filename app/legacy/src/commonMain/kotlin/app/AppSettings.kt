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

package me.him188.ani.app.app

import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import me.him188.ani.app.app.settings.ProxySettings
import me.him188.ani.app.app.settings.SyncSettings
import net.mamoe.yamlkt.Yaml
import java.io.File

@Immutable
@Serializable
data class AppSettings(
    /**
     * macOS 窗口沉浸, 把背景画入标题栏
     */
    @Stable
    val windowImmersed: Boolean = true,

    @Stable
    val proxy: ProxySettings = ProxySettings(),

    @Stable
    val sync: SyncSettings = SyncSettings(),
)

@Stable
abstract class AppSettingsManager {
    @Stable
    val value: MutableStateFlow<AppSettings> by lazy { MutableStateFlow(loadImpl()) }

    inline fun mutate(block: AppSettings.() -> AppSettings) {
        value.value = value.value.let(block)
    }

    fun load() {
        value.value = loadImpl()
    }

    protected abstract fun loadImpl(): AppSettings

    fun save() {
        saveImpl(value.value)
    }

    protected abstract fun saveImpl(instance: AppSettings)

    @Composable
    fun attachAutoSave() {
        val instance by value.collectAsStateWithLifecycle()
        LaunchedEffect(instance) {
            withContext(Dispatchers.IO) {
                save()
            }
        }
    }
}

@Stable
class LocalAppSettingsManagerImpl(
    private val file: File
) : AppSettingsManager() {
    override fun loadImpl(): AppSettings {
        if (!file.exists()) return AppSettings().also { saveImpl(it) }
        return Yaml.decodeFromString(AppSettings.serializer(), file.readText())
    }

    override fun saveImpl(instance: AppSettings) {
        file.parentFile?.mkdir()
        file.writeText(Yaml.encodeToString(AppSettings.serializer(), instance))
    }
}


@Stable
val LocalAppSettingsManager: ProvidableCompositionLocal<AppSettingsManager> =
    staticCompositionLocalOf {
        error("No AppSettingsManager provided in current context")
    }

@Stable
object LocalAppSettings {
    @Stable
    val current
        @Composable
        get() = LocalAppSettingsManager.current.value.value
}