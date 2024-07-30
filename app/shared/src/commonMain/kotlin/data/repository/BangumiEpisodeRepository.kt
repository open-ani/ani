package me.him188.ani.app.data.repository

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.models.PackedDate
import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.data.models.episode.EpisodeType
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.paging.processPagedResponse
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.models.BangumiEpType
import me.him188.ani.datasources.bangumi.models.BangumiEpisode
import me.him188.ani.datasources.bangumi.models.BangumiEpisodeCollectionType
import me.him188.ani.datasources.bangumi.models.BangumiEpisodeDetail
import me.him188.ani.datasources.bangumi.models.BangumiPatchUserSubjectEpisodeCollectionRequest
import me.him188.ani.datasources.bangumi.models.BangumiUserEpisodeCollection
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.serialization.BigNum
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 执行网络请求查询.
 * 建议优先使用 [SubjectManager], 可以使用缓存.
 */
interface BangumiEpisodeRepository : Repository {
    suspend fun getEpisodeById(episodeId: Int): BangumiEpisodeDetail?

    /**
     * 获取条目下的所有剧集.
     */
    suspend fun getEpisodesBySubjectId(subjectId: Int, type: BangumiEpType): Flow<BangumiEpisode>

    /**
     * 获取用户在这个条目下的所有剧集的收藏状态. 当用户没有收藏此条目时返回 `null`
     */
    suspend fun getSubjectEpisodeCollections(subjectId: Int, type: BangumiEpType): Flow<BangumiUserEpisodeCollection>?

    /**
     * 获取用户在这个条目下的所有剧集的收藏状态.
     */
    suspend fun getEpisodeCollection(episodeId: Int): BangumiUserEpisodeCollection?

    /**
     * 设置多个剧集的收藏状态.
     */
    suspend fun setEpisodeCollection(subjectId: Int, episodeId: List<Int>, type: BangumiEpisodeCollectionType)
}

internal class EpisodeRepositoryImpl : BangumiEpisodeRepository, KoinComponent {
    private val client by inject<BangumiClient>()
    private val logger = logger(EpisodeRepositoryImpl::class)
    override suspend fun getEpisodeById(episodeId: Int): BangumiEpisodeDetail? {
        return try {
            client.getApi().getEpisodeById(episodeId).body()
        } catch (e: Exception) {
            logger.warn("Exception in getEpisodeById", e)
            null
        }
    }

    override suspend fun getEpisodesBySubjectId(subjectId: Int, type: BangumiEpType): Flow<BangumiEpisode> {
        val episodes = PageBasedPagedSource { page ->
            runCatching {
                withContext(Dispatchers.IO) {
                    client.getApi().getEpisodes(subjectId, type, offset = page * 100, limit = 100).body().run {
                        Paged(this.total ?: 0, !this.data.isNullOrEmpty(), this.data.orEmpty())
                    }
                }
            }.getOrNull()
        }
        return episodes.results
    }

    override suspend fun getSubjectEpisodeCollections(
        subjectId: Int,
        type: BangumiEpType
    ): Flow<BangumiUserEpisodeCollection>? {
        val firstPage = try {
            client.getApi().getUserSubjectEpisodeCollection(
                subjectId,
                episodeType = type,
                offset = 0,
                limit = 100,
            ).body()
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound
                || e.response.status == HttpStatusCode.Unauthorized
            ) {
                return null
            }
            throw e
        }

        val episodes = PageBasedPagedSource { page ->
            val resp = if (page == 0) {
                firstPage
            } else {
                client.getApi().getUserSubjectEpisodeCollection(
                    subjectId,
                    episodeType = type,
                    offset = page * 100,
                    limit = 100,
                ).body()
            }
            Paged.processPagedResponse(resp.total, 100, resp.data)
        }
        return episodes.results.retry(1)
    }

    override suspend fun getEpisodeCollection(episodeId: Int): BangumiUserEpisodeCollection? {
        try {
            val collection = client.getApi().getUserEpisodeCollection(episodeId)
            return collection.body()
        } catch (e: Exception) {
            logger.warn("Exception in getEpisodeCollection", e)
            return null
        }
    }

    override suspend fun setEpisodeCollection(
        subjectId: Int,
        episodeId: List<Int>,
        type: BangumiEpisodeCollectionType
    ) {
        try {
            client.getApi().patchUserSubjectEpisodeCollection(
                subjectId,
                BangumiPatchUserSubjectEpisodeCollectionRequest(
                    episodeId,
                    type,
                ),
            )
        } catch (e: Exception) {
            logger.warn("Exception in setEpisodeCollection", e)
        }
    }
}

fun BangumiEpisode.toEpisodeInfo(): EpisodeInfo {
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
        ep = EpisodeSort(this.ep ?: BigNum.ONE),
//        durationSeconds = this.durationSeconds
    )
}

fun BangumiEpisodeDetail.toEpisodeInfo(): EpisodeInfo {
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
        ep = EpisodeSort(this.ep ?: BigNum.ONE),
    )
}
