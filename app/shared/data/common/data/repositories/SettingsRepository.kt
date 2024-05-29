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
import me.him188.ani.app.data.models.MediaSelectorSettings
import me.him188.ani.app.data.models.ProxySettings
import me.him188.ani.app.data.models.UISettings
import me.him188.ani.app.data.serializers.DanmakuConfigSerializer
import me.him188.ani.app.tools.torrent.engines.Libtorrent4jConfig
import me.him188.ani.app.tools.torrent.engines.QBittorrentConfig
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger

/**
 * 所有设置
 */
interface SettingsRepository {
    val danmakuEnabled: Settings<Boolean>
    val danmakuConfig: Settings<DanmakuConfig>

    val mediaSelectorSettings: Settings<MediaSelectorSettings>

    /**
     * 全局默认选择资源的偏好设置
     *
     * @see EpisodePreferencesRepository
     */
    val defaultMediaPreference: Settings<MediaPreference>

    val proxySettings: Settings<ProxySettings>
    val mediaCacheSettings: Settings<MediaCacheSettings>
    val danmakuSettings: Settings<DanmakuSettings>
    val uiSettings: Settings<UISettings>

    val libtorrent4jConfig: Settings<Libtorrent4jConfig>
    val qBittorrentConfig: Settings<QBittorrentConfig>
}

@Stable
interface Settings<T> {
    val flow: Flow<T>
    suspend fun set(value: T)
}

class PreferencesRepositoryImpl(
    private val preferences: DataStore<Preferences>,
) : SettingsRepository {
    private val format = Json {
        ignoreUnknownKeys = true
    }

    inner class BooleanPreference(
        val name: String,
    ) : Settings<Boolean> {
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
    ) : Settings<T> {
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

    override val danmakuEnabled: Settings<Boolean> = BooleanPreference("danmaku_enabled")
    override val danmakuConfig: Settings<DanmakuConfig> =
        SerializablePreference("danmaku_config", DanmakuConfigSerializer, default = { DanmakuConfig.Default })
    override val mediaSelectorSettings: Settings<MediaSelectorSettings> = SerializablePreference(
        "mediaSelectorSettings",
        MediaSelectorSettings.serializer(),
        default = { MediaSelectorSettings.Default }
    )
    override val defaultMediaPreference: Settings<MediaPreference> =
        SerializablePreference(
            "defaultMediaPreference",
            MediaPreference.serializer(),
            default = { MediaPreference.PlatformDefault }
        )
    override val proxySettings: Settings<ProxySettings> = SerializablePreference(
        "proxyPreferences",
        ProxySettings.serializer(),
        default = { ProxySettings.Default }
    )
    override val mediaCacheSettings: Settings<MediaCacheSettings> = SerializablePreference(
        "cachePreferences",
        MediaCacheSettings.serializer(),
        default = { MediaCacheSettings.Default }
    )
    override val danmakuSettings: Settings<DanmakuSettings> = SerializablePreference(
        "danmakuSettings",
        DanmakuSettings.serializer(),
        default = { DanmakuSettings.Default }
    )
    override val uiSettings: Settings<UISettings> = SerializablePreference(
        "uiSettings",
        UISettings.serializer(),
        default = { UISettings.Default },
    )
    override val libtorrent4jConfig: Settings<Libtorrent4jConfig> = SerializablePreference(
        "libtorrent4jConfig",
        Libtorrent4jConfig.serializer(),
        default = { Libtorrent4jConfig.Default }
    )
    override val qBittorrentConfig: Settings<QBittorrentConfig> = SerializablePreference(
        "qBittorrentConfig",
        QBittorrentConfig.serializer(),
        default = { QBittorrentConfig.Default }
    )

    private companion object {
        private val logger = logger<SettingsRepository>()
    }
}
