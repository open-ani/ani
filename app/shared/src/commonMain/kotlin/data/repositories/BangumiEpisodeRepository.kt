package me.him188.ani.app.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.subject.EpisodeInfo
import me.him188.ani.app.data.subject.EpisodeType
import me.him188.ani.app.data.subject.PackedDate
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.EpType
import org.openapitools.client.models.Episode
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.EpisodeDetail
import org.openapitools.client.models.PatchUserSubjectEpisodeCollectionRequest
import org.openapitools.client.models.UserEpisodeCollection
import java.math.BigDecimal

/**
 * 执行网络请求查询.
 * 建议优先使用 [SubjectManager], 可以使用缓存.
 */
interface BangumiEpisodeRepository : Repository {
    suspend fun getEpisodeById(episodeId: Int): EpisodeDetail?

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

internal class EpisodeRepositoryImpl : BangumiEpisodeRepository, KoinComponent {
    private val client by inject<BangumiClient>()
    private val logger = logger(EpisodeRepositoryImpl::class)
    override suspend fun getEpisodeById(episodeId: Int): EpisodeDetail? {
        return try {
            client.api.getEpisodeById(episodeId)
        } catch (e: Exception) {
            logger.warn("Exception in getEpisodeById", e)
            null
        }
    }

    override suspend fun getEpisodesBySubjectId(subjectId: Int, type: EpType): Flow<Episode> {
        val episodes = PageBasedPagedSource { page ->
            runCatching {
                withContext(Dispatchers.IO) {
                    client.api.getEpisodes(subjectId, type, offset = page * 100, limit = 100).run {
                        Paged(this.total ?: 0, !this.data.isNullOrEmpty(), this.data.orEmpty())
                    }
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

fun Episode.toEpisodeInfo(): EpisodeInfo {
    return EpisodeInfo(
        id = this.id,
        type = EpisodeType(this.type),
        name = this.name,
        nameCn = this.nameCn,
        airDate = PackedDate.parseFromDate(this.airdate),
        comment = this.comment,
        duration = this.duration,
        desc = this.desc,
        disc = this.disc,
        sort = EpisodeSort(this.sort),
        ep = EpisodeSort(this.ep ?: BigDecimal.ONE),
//        durationSeconds = this.durationSeconds
    )
}

fun EpisodeDetail.toEpisodeInfo(): EpisodeInfo {
    return EpisodeInfo(
        id = id,
        type = EpisodeType(this.type),
        name = name,
        nameCn = nameCn,
        sort = EpisodeSort(this.sort),
        airDate = PackedDate.parseFromDate(this.airdate),
        comment = comment,
        duration = duration,
        desc = desc,
        disc = disc,
        ep = EpisodeSort(this.ep ?: BigDecimal.ONE),
    )
}
