package me.him188.ani.app.data

import kotlinx.coroutines.flow.Flow
import me.him188.ani.datasources.api.PageBasedSearchSession
import me.him188.ani.datasources.api.Paged
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.EpType
import org.openapitools.client.models.Episode
import org.openapitools.client.models.UserEpisodeCollection

interface EpisodeRepository {
    suspend fun getEpisodesBySubjectId(subjectId: Int, type: EpType): Flow<Episode>
    suspend fun getSubjectEpisodeCollection(subjectId: Int, type: EpType): Flow<UserEpisodeCollection>
}

internal class EpisodeRepositoryImpl : EpisodeRepository, KoinComponent {
    private val client by inject<BangumiClient>()
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
}