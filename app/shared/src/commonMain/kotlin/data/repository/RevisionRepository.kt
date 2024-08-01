package me.him188.ani.app.data.repository

import me.him188.ani.app.data.models.episode.EpisodeComment
import me.him188.ani.app.data.models.episode.toEpisodeComment
import me.him188.ani.app.data.models.subject.SubjectComment
import me.him188.ani.app.data.models.subject.toSubjectComment
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.paging.processPagedResponse
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.next.models.BangumiNextGetSubjectEpisodeComments200ResponseInner
import me.him188.ani.datasources.bangumi.next.models.BangumiNextSubjectInterestCommentListInner
import me.him188.ani.utils.logging.logger

sealed interface RevisionRepository {
    fun getSubjectEpisodeComments(episodeId: Int): PagedSource<EpisodeComment>
    fun getSubjectComments(subjectId: Int): PagedSource<SubjectComment>
}

class BangumiRevisionRepositoryImpl(
    private val client: BangumiClient
) : RevisionRepository {
    private val logger = logger(RevisionRepository::class)

    override fun getSubjectComments(subjectId: Int): PagedSource<SubjectComment> {
        return PageBasedPagedSource { page ->
            try {
                val response = client.getNextApi()
                    .subjectComments(subjectId, 16, page * 16)
                    .body()

                setTotalSize(response.total)
                val list = response.list.map(BangumiNextSubjectInterestCommentListInner::toSubjectComment)
                Paged.processPagedResponse(response.total, 16, list)
            } catch (e: Exception) {
                logger.warn("Exception in getSubjectComments", e)
                null
            }
        }
    }

    override fun getSubjectEpisodeComments(episodeId: Int): PagedSource<EpisodeComment> {
        // 未来这个接口将会支持分页属性
        return PageBasedPagedSource { page ->
            try {
                if (page == 0) {
                    val response = client.getNextApi()
                        .getSubjectEpisodeComments(episodeId)
                        .body()
                        .map(BangumiNextGetSubjectEpisodeComments200ResponseInner::toEpisodeComment)

                    setTotalSize(response.size)
                    Paged.processPagedResponse(response.size, response.size, response)
                } else null
            } catch (e: Exception) {
                logger.warn("Exception in getSubjectEpisodeComments", e)
                null
            }
        }
    }
}