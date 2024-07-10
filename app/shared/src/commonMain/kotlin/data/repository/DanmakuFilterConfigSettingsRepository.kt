package me.him188.ani.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import me.him188.ani.danmaku.ui.DanmakuFilterConfig
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent

interface DanmakuFilterConfigSettingsRepository {
    val danmakuFilterConfig: Settings<DanmakuFilterConfig>
}

class DanmakuFilterConfigSettingsRepositoryImpl(
    val store: DataStore<Preferences>,
) : DanmakuFilterConfigSettingsRepository, KoinComponent {

    private val format = Json {
        ignoreUnknownKeys = true
    }

    inner class SerializablePreference<T : Any>(
        val name: String,
        private val serializer: KSerializer<T>,
        default: () -> T,
    ) : Settings<T> {
        private val key = stringPreferencesKey(name)
        override val flow: Flow<T> = store.data
            .map { it[key] }
            .distinctUntilChanged()
            .map { string ->
                if (string == null) {
                    default()
                } else try {
                    format.decodeFromString(serializer, string)
                } catch (e: Exception) {
                    DanmakuFilterConfigSettingsRepositoryImpl.logger.error(e) { "Failed to decode preference '$name'. Using default. Failed json: $string" }
                    default()
                }
            }

        override suspend fun set(value: T) {
            DanmakuFilterConfigSettingsRepositoryImpl.logger.debug { "Updating preference '$key' with: $value" }
            store.edit {
                it[key] = format.encodeToString(serializer, value)
            }
        }
    }

    override val danmakuFilterConfig: Settings<DanmakuFilterConfig> = SerializablePreference(
        "danmakuFilterConfig",
        DanmakuFilterConfig.serializer(),
        default = { DanmakuFilterConfig.Default },
    )

    private companion object {
        private val logger = logger<DanmakuFilterConfigSettingsRepository>()
    }

}