package me.him188.ani.app.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import me.him188.ani.app.data.serializers.DanmakuConfigSerializer
import me.him188.ani.danmaku.ui.DanmakuConfig

interface PreferencesRepository {
    val danmakuEnabled: Preference<Boolean>
    val danmakuConfig: Preference<DanmakuConfig>

//    suspend fun setDanmakuEnabled(enabled: Boolean)
}

interface Preference<T> {
    val flow: Flow<T>
    suspend fun set(value: T)
}

class PreferencesRepositoryImpl(
    private val preferences: DataStore<Preferences>,
) : PreferencesRepository {
    private val format = Json {
        ignoreUnknownKeys = true
    }

    inner class BooleanPreference(
        val name: String,
    ) : Preference<Boolean> {
        private val key = booleanPreferencesKey(name)
        override val flow: Flow<Boolean> = preferences.data.map { it[key] ?: true }
        override suspend fun set(value: Boolean) {
            preferences.edit { it[key] = value }
        }
    }

    inner class SerializablePreference<T>(
        val name: String,
        private val serializer: KSerializer<T>,
        default: () -> T,
    ) : Preference<T> {
        private val key = stringPreferencesKey(name)
        override val flow: Flow<T> = preferences.data
            .map { it[key] }
            .map { string ->
                if (string == null) {
                    default()
                } else format.decodeFromString(serializer, string)
            }

        override suspend fun set(value: T) {
            preferences.edit { it[key] = format.encodeToString(serializer, value) }
        }
    }

    override val danmakuEnabled: Preference<Boolean> = BooleanPreference("danmaku_enabled")
    override val danmakuConfig: Preference<DanmakuConfig> =
        SerializablePreference("danmaku_config", DanmakuConfigSerializer, default = { DanmakuConfig.Default })

//    private companion object {
//        val DANMAKU_ENABLED = booleanPreferencesKey("danmaku_enabled")
//    }
//
//    override val danmakuEnabled: Flow<Boolean>
//        get() = preferences.data.map { it[DANMAKU_ENABLED] ?: true }
//
//    override suspend fun setDanmakuEnabled(enabled: Boolean) {
//        preferences.edit { it[DANMAKU_ENABLED] = enabled }
//    }

}
