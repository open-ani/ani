package me.him188.ani.app.data.repositories

import androidx.compose.runtime.Stable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import me.him188.ani.app.data.models.DanmakuSettings
import me.him188.ani.app.data.models.MediaCacheSettings
import me.him188.ani.app.data.models.ProxyPreferences
import me.him188.ani.app.data.serializers.DanmakuConfigSerializer
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger

interface PreferencesRepository {
    val danmakuEnabled: Preference<Boolean>
    val danmakuConfig: Preference<DanmakuConfig>

//    suspend fun setDanmakuEnabled(enabled: Boolean)

    /**
     * 全局默认设置
     *
     * @see EpisodePreferencesRepository
     */
    val defaultMediaPreference: Preference<MediaPreference>

    val proxyPreferences: Preference<ProxyPreferences>
    val mediaCacheSettings: Preference<MediaCacheSettings>
    val danmakuSettings: Preference<DanmakuSettings>
}

@Stable
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

    inner class SerializablePreference<T : Any>(
        val name: String,
        private val serializer: KSerializer<T>,
        default: () -> T,
    ) : Preference<T> {
        private val key = stringPreferencesKey(name)
        override val flow: Flow<T> = preferences.data
            .map { it[key] }
            .distinctUntilChanged()
            .map { string ->
                if (string == null) {
                    default()
                } else try {
                    format.decodeFromString(serializer, string)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to decode preference '$name'. Using default. Failed json: $string" }
                    default()
                }
            }

        override suspend fun set(value: T) {
            logger.debug { "Updating preference '$key' with: $value" }
            preferences.edit {
                it[key] = format.encodeToString(serializer, value)
            }
        }
    }

    override val danmakuEnabled: Preference<Boolean> = BooleanPreference("danmaku_enabled")
    override val danmakuConfig: Preference<DanmakuConfig> =
        SerializablePreference("danmaku_config", DanmakuConfigSerializer, default = { DanmakuConfig.Default })
    override val defaultMediaPreference: Preference<MediaPreference> =
        SerializablePreference(
            "defaultMediaPreference",
            MediaPreference.serializer(),
            default = { MediaPreference.Default }
        )
    override val proxyPreferences: Preference<ProxyPreferences> = SerializablePreference(
        "proxyPreferences",
        ProxyPreferences.serializer(),
        default = { ProxyPreferences.Default }
    )
    override val mediaCacheSettings: Preference<MediaCacheSettings> = SerializablePreference(
        "cachePreferences",
        MediaCacheSettings.serializer(),
        default = { MediaCacheSettings.Default }
    )
    override val danmakuSettings: Preference<DanmakuSettings> = SerializablePreference(
        "danmakuSettings",
        DanmakuSettings.serializer(),
        default = { DanmakuSettings.Default }
    )

    private companion object {
        private val logger = logger<PreferencesRepository>()
    }
}
