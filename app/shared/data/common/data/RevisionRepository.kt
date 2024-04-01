package me.him188.ani.app.data

import kotlinx.coroutines.flow.Flow
import me.him188.ani.datasources.api.PageBasedPagedSource
import me.him188.ani.datasources.api.Paged
import me.him188.ani.datasources.api.processPagedResponse
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.models.Revision

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
                ).run {
                    Paged.processPagedResponse(total, pageSize, data?.map {
                        it.toComment()
                    })
                }
            } catch (e: ClientException) {
                logger.warn("Exception in getCollections", e)
                null
            }
        }.results
    }
//    override fun getCommentById(commentId: Int): Comment? {
//        return client.api.getEpisodeRevisionByRevisionId(
//            revisionId = commentId,
//        )
//    }
}

private fun Revision.toComment(): Comment {
    return Comment(
        id = id.toString(),
        type = type,
        summary = summary,
        createdAt = createdAt.toEpochSecond() * 1000,
        authorUsername = creator?.username,
    )
}
