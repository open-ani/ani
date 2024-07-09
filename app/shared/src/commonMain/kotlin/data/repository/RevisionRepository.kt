package me.him188.ani.app.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.flow.Flow
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.paging.processPagedResponse
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.models.BangumiRevision
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed interface RevisionRepository {
    fun getCommentsByEpisodeId(episodeId: Int): Flow<Comment>
}

interface EpisodeRevisionRepository : RevisionRepository
interface SubjectRevisionRepository : RevisionRepository

class Comment(
    val id: String,
    val type: Int,
    val summary: String,
    val createdAt: Long, // timestamp millis
    val authorUsername: String?,
)

class EpisodeRevisionRepositoryImpl : EpisodeRevisionRepository, KoinComponent {
    private val client by inject<BangumiClient>()
    private val logger = logger(EpisodeRepositoryImpl::class)

    override fun getCommentsByEpisodeId(episodeId: Int): Flow<Comment> {
        return PageBasedPagedSource { page ->
            try {
                val pageSize = 30
                client.api.getEpisodeRevisions(
                    offset = page * pageSize, limit = pageSize,
                    episodeId = episodeId,
                ).body().run {
                    Paged.processPagedResponse(
                        total, pageSize,
                        data?.map {
                            it.toComment()
                        },
                    )
                }
            } catch (e: ResponseException) {
                logger.warn("Exception in getCollections", e)
                null
            }
        }.results
    }
}

private fun BangumiRevision.toComment(): Comment {
    return Comment(
        id = id.toString(),
        type = type,
        summary = summary,
        createdAt = createdAt.toEpochMilliseconds() * 1000,
        authorUsername = creator?.username,
    )
}
