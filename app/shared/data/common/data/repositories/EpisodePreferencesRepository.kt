package me.him188.ani.app.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent

interface EpisodePreferencesRepository : KoinComponent {
    /**
     * Returns the user's saved media preference for the given subject.
     * @see PreferencesRepository.defaultMediaPreference
     */
    fun mediaPreferenceFlow(subjectId: Int): Flow<MediaPreference>
    suspend fun setMediaPreference(subjectId: Int, mediaPreference: MediaPreference)
}

internal class EpisodePreferencesRepositoryImpl(
    private val store: DataStore<Preferences>,
) : EpisodePreferencesRepository {
    private val logger = logger(this::class)
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun mediaPreferenceFlow(subjectId: Int): Flow<MediaPreference> {
        return store.data.map {
            it[stringPreferencesKey(subjectId.toString())]
        }.map {
            if (it.isNullOrBlank()) {
                return@map MediaPreference.Empty
            }
            val res = kotlin.runCatching {
                json.decodeFromString(MediaPreference.serializer(), it)
            }.getOrNull() ?: MediaPreference.Empty
            logger.info { "Loaded user MediaPreference for subject $subjectId: $res" }
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