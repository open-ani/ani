package me.him188.ani.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import me.him188.ani.datasources.api.PageBasedSearchSession
import me.him188.ani.datasources.api.Paged
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.EpType
import org.openapitools.client.models.Episode
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.PatchUserSubjectEpisodeCollectionRequest
import org.openapitools.client.models.UserEpisodeCollection

interface EpisodeRepository {
    /**
     * 获取条目下的所有剧集.
     */
    suspend fun getEpisodesBySubjectId(subjectId: Int, type: EpType): Flow<Episode>

    /**
     * 获取用户在这个条目下的所有剧集的收藏状态.
     */
    suspend fun getSubjectEpisodeCollection(subjectId: Int, type: EpType): Flow<UserEpisodeCollection>

    /**
     * 获取用户在这个条目下的所有剧集的收藏状态.
     */
    suspend fun getEpisodeCollection(episodeId: Int): UserEpisodeCollection?

    /**
     * 设置多个剧集的收藏状态.
     */
    suspend fun setEpisodeCollection(subjectId: Int, episodeId: List<Int>, type: EpisodeCollectionType)

    /**
     * 获取该剧集的播放进度
     *
     * @param key subjectId + '.' + episodeId
     */
    fun getEpisodePosition(key: String): Flow<Long>

    /**
     * 设置该剧集的播放进度
     *
     * @param key subjectId + '.' + episodeId
     */
    suspend fun setEpisodePosition(key: String, position: Long)
}

internal class EpisodeRepositoryImpl(
    private val store: DataStore<Preferences>
) : EpisodeRepository, KoinComponent {
    private val client by inject<BangumiClient>()
    private val settings by inject<AppSettingsRepository>()
    private val logger = logger(EpisodeRepositoryImpl::class)

    override suspend fun getEpisodesBySubjectId(subjectId: Int, type: EpType): Flow<Episode> {
        val episodes = PageBasedSearchSession { page ->
            runCatching {
                client.api.getEpisodes(subjectId, type, offset = page * 100, limit = 100).run {
                    Paged(this.total ?: 0, !this.data.isNullOrEmpty(), this.data.orEmpty())
                }
            }.getOrNull()
        }
        return episodes.results
    }

    override suspend fun getSubjectEpisodeCollection(subjectId: Int, type: EpType): Flow<UserEpisodeCollection> {
        val episodes = PageBasedSearchSession { page ->
            try {
                client.api.getUserSubjectEpisodeCollection(
                    subjectId,
                    episodeType = type,
                    offset = page * 100,
                    limit = 100
                ).run {
                    val data = this.data ?: return@run null
                    Paged(this.total, data.size == 100, data)
                }
            } catch (
                e: Exception
            ) {
                logger.warn("Exception in getSubjectEpisodeCollection", e)
                null
            }
        }
        return episodes.results
    }

    override suspend fun getEpisodeCollection(episodeId: Int): UserEpisodeCollection? {
        try {
            val collection = client.api.getUserEpisodeCollection(episodeId)
            return collection
        } catch (e: Exception) {
            logger.warn("Exception in getEpisodeCollection", e)
            return null
        }
    }

    override suspend fun setEpisodeCollection(subjectId: Int, episodeId: List<Int>, type: EpisodeCollectionType) {
        try {
            client.postEpisodeCollection(
                subjectId,
                PatchUserSubjectEpisodeCollectionRequest(
                    episodeId,
                    type,
                ),
            )
        } catch (e: Exception) {
            logger.warn("Exception in setEpisodeCollection", e)
        }
    }

    private val serializer by lazy { MapSerializer(String.serializer(), EpisodePosition.serializer()) }

    override fun getEpisodePosition(key: String): Flow<Long> {
        return store.data
            .map { it[stringPreferencesKey(key)] }
            .map { if (it == null) 0 else Json.decodeFromString(serializer, it)[key]?.position ?: 0 }
    }

    override suspend fun setEpisodePosition(key: String, position: Long) {
        logger.info { "Saved episode positions: $position" }
        val now = System.currentTimeMillis()
        val ep = EpisodePosition(position, now)
        val keep = settings.settings.first().keepPlayPosition.inWholeMilliseconds
        store.edit { preferences ->
            val map = preferences[stringPreferencesKey(key)]
                ?.let { Json.decodeFromString(serializer, it) }
                ?: mapOf()
            val positions = map.filter { now - it.value.time < keep } + (key to ep)
            preferences[stringPreferencesKey(key)] = Json.encodeToString(serializer, positions)
        }
    }
}

@Serializable
data class EpisodePosition(
    val position: Long,
    val time: Long,
)