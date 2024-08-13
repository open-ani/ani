package me.him188.ani.app.data.repository

import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.data.models.episode.EpisodeComment
import me.him188.ani.app.data.models.episode.toEpisodeComment
import me.him188.ani.app.data.models.subject.SubjectComment
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.paging.processPagedResponse
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.next.models.BangumiNextGetSubjectEpisodeComments200ResponseInner
import me.him188.ani.datasources.bangumi.next.models.BangumiNextSubjectInterestCommentListInner
import me.him188.ani.utils.logging.logger

sealed interface CommentRepository {
    fun getSubjectEpisodeComments(episodeId: Int): PagedSource<EpisodeComment>
    fun getSubjectComments(subjectId: Int): PagedSource<SubjectComment>
}

class BangumiCommentRepositoryImpl(
    private val client: BangumiClient
) : CommentRepository {
    private val logger = logger(CommentRepository::class)

    override fun getSubjectComments(subjectId: Int): PagedSource<SubjectComment> {
        // 这个接口不支持按时间倒叙查询，所以先查询一条来获取评论总数，再从最后一页开始查询
        return PageBasedPagedSource { page ->
            try {
                // 第一页先获取所有评论数量，获取失败则继续使用时间正序查询，也不会提供总计大小
                if (page == 0) {
                    try {
                        val response = client.getNextApi().subjectComments(subjectId, 1, 1).body()
                        setTotalSize(response.total)
                    } catch (_: Exception) {
                    }
                }

                val total = totalSize
                val offset: Int = if (total == null) {
                    page * 16
                } else {
                    (total - (page + 1) * 16).coerceAtLeast(0)
                }

                val response = client.getNextApi()
                    .subjectComments(subjectId, 16, offset)
                    .body()

                val list = response.list.map(BangumiNextSubjectInterestCommentListInner::toSubjectComment)
                Paged.processPagedResponse(total = response.total, pageSize = 16, data = list)
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

private fun BangumiNextSubjectInterestCommentListInner.toSubjectComment() = SubjectComment(
    id = 31 * comment.hashCode() + 31 * updatedAt + 31 * (user?.id ?: UserInfo.EMPTY.id),
    content = comment,
    updatedAt = updatedAt,
    rating = rate,
    creator = user?.let { u ->
        UserInfo(
            id = u.id,
            nickname = u.nickname,
            username = u.id.toString(),
            avatarUrl = u.avatar.medium,
        ) // 没有username
    },
)