package me.him188.ani.app.data

import kotlinx.coroutines.flow.Flow
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.EpType
import org.openapitools.client.models.Episode
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.PatchUserSubjectEpisodeCollectionRequest
import org.openapitools.client.models.UserEpisodeCollection

interface EpisodeRepository : Repository {
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
}

internal class EpisodeRepositoryImpl : EpisodeRepository, KoinComponent {
    private val client by inject<BangumiClient>()
    private val logger = logger(EpisodeRepositoryImpl::class)

    override suspend fun getEpisodesBySubjectId(subjectId: Int, type: EpType): Flow<Episode> {
        val episodes = PageBasedPagedSource { page ->
            runCatching {
                client.api.getEpisodes(subjectId, type, offset = page * 100, limit = 100).run {
                    Paged(this.total ?: 0, !this.data.isNullOrEmpty(), this.data.orEmpty())
                }
            }.getOrNull()
        }
        return episodes.results
    }

    override suspend fun getSubjectEpisodeCollection(subjectId: Int, type: EpType): Flow<UserEpisodeCollection> {
        val episodes = PageBasedPagedSource { page ->
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
}