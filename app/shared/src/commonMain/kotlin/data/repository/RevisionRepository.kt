package me.him188.ani.app.data.repository

import io.ktor.client.plugins.ResponseException
import me.him188.ani.app.data.models.episode.EpisodeComment
import me.him188.ani.app.data.models.episode.toEpisodeComment
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.paging.processPagedResponse
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.next.models.BangumiNextGetSubjectEpisodeComments200ResponseInner
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed interface RevisionRepository {
    fun getSubjectEpisodeComments(episodeId: Int): PagedSource<EpisodeComment>
}

interface EpisodeRevisionRepository : RevisionRepository
interface SubjectRevisionRepository : RevisionRepository

class EpisodeRevisionRepositoryImpl : EpisodeRevisionRepository, KoinComponent {
    private val client by inject<BangumiClient>()
    private val logger = logger(EpisodeRepositoryImpl::class)

    override fun getSubjectEpisodeComments(episodeId: Int): PagedSource<EpisodeComment> {
        // 未来这个接口将会支持分页属性
        return PageBasedPagedSource { page ->
            try {
                if (page == 0) {
                    val response = client.nextApi
                        .getSubjectEpisodeComments(episodeId)
                        .body()
                        .map(BangumiNextGetSubjectEpisodeComments200ResponseInner::toEpisodeComment)

                    Paged.processPagedResponse(response.size, response.size, response)
                } else null
            } catch (e: ResponseException) {
                logger.warn("Exception in getSubjectEpisodeComments", e)
                null
            }
        }
    }
}