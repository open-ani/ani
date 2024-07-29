package me.him188.ani.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.mp.KoinPlatform

interface EpisodePreferencesRepository : KoinComponent {
    /**
     * 获取用户对这个条目的设置, 当不存在时返回全局默认设置 [SettingsRepository.defaultMediaPreference]
     * @see SettingsRepository.defaultMediaPreference
     */
    fun mediaPreferenceFlow(subjectId: Int): Flow<MediaPreference>
    suspend fun setMediaPreference(subjectId: Int, mediaPreference: MediaPreference)
}

internal class EpisodePreferencesRepositoryImpl(
    private val store: DataStore<Preferences>,
    private val defaultMediaPreference: Flow<MediaPreference> = KoinPlatform.getKoin()
        .get<SettingsRepository>().defaultMediaPreference.flow
) : EpisodePreferencesRepository, KoinComponent {
    private val logger = logger(this::class)
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun mediaPreferenceFlow(subjectId: Int): Flow<MediaPreference> {
        return store.data.map {
            it[stringPreferencesKey(subjectId.toString())]
        }.map {
            if (it.isNullOrBlank()) {
//                logger.info { "Loaded user MediaPreference for subject $subjectId: null, use default" }
                return@map defaultMediaPreference.first()
            }
            val res = kotlin.runCatching {
                json.decodeFromString(MediaPreference.serializer(), it)
            }.getOrNull() ?: defaultMediaPreference.first()
//            logger.info { "Loaded user MediaPreference for subject $subjectId: $res" }
            res
        }
    }

    override suspend fun setMediaPreference(subjectId: Int, mediaPreference: MediaPreference) {
        logger.info { "Saved user MediaPreference for subject $subjectId: $mediaPreference" }
        store.edit {
            it[stringPreferencesKey(subjectId.toString())] =
                json.encodeToString(MediaPreference.serializer(), mediaPreference)
        }
    }
}