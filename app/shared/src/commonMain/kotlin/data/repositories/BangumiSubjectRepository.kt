package me.him188.ani.app.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.subject.EpisodeCollection
import me.him188.ani.app.data.subject.RatingCounts
import me.him188.ani.app.data.subject.RatingInfo
import me.him188.ani.app.data.subject.SubjectCollection
import me.him188.ani.app.data.subject.SubjectCollectionStats
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.data.subject.Tag
import me.him188.ani.app.data.subject.toInfoboxItem
import me.him188.ani.app.session.SessionManager
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.paging.processPagedResponse
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.processing.toCollectionType
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.models.Count
import org.openapitools.client.models.Rating
import org.openapitools.client.models.Subject
import org.openapitools.client.models.SubjectCollectionType
import org.openapitools.client.models.SubjectType
import org.openapitools.client.models.UserEpisodeCollection
import org.openapitools.client.models.UserSubjectCollection
import org.openapitools.client.models.UserSubjectCollectionModifyPayload

/**
 * Performs network requests.
 * Use [SubjectManager] instead.
 */
interface BangumiSubjectRepository : Repository {
    suspend fun getSubject(id: Int): Subject?

    fun getSubjectCollections(
        username: String,
        subjectType: SubjectType? = null,
        subjectCollectionType: SubjectCollectionType? = null,
    ): PagedSource<UserSubjectCollection>

    fun subjectCollectionTypeById(subjectId: Int): Flow<UnifiedCollectionType>

    suspend fun patchSubjectCollection(subjectId: Int, payload: UserSubjectCollectionModifyPayload)
    suspend fun deleteSubjectCollection(subjectId: Int)
}

suspend inline fun BangumiSubjectRepository.setSubjectCollectionTypeOrDelete(
    subjectId: Int,
    type: SubjectCollectionType?
) {
    return if (type == null) {
        deleteSubjectCollection(subjectId)
    } else {
        patchSubjectCollection(subjectId, UserSubjectCollectionModifyPayload(type))
    }
}

class RemoteBangumiSubjectRepository : BangumiSubjectRepository, KoinComponent {
    private val client: BangumiClient by inject()
    private val sessionManager: SessionManager by inject()
    private val logger = logger(this::class)

    override suspend fun getSubject(id: Int): Subject? {
        return runInterruptible(Dispatchers.IO) {
            runCatching {
                client.api.getSubjectById(id)
            }.getOrNull()
        }
    }

    override suspend fun patchSubjectCollection(subjectId: Int, payload: UserSubjectCollectionModifyPayload) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                client.postSubjectCollection(subjectId, payload)
            }
        }
    }

    override suspend fun deleteSubjectCollection(subjectId: Int) {
        runInterruptible(Dispatchers.IO) {
            kotlin.runCatching {
                client.api.uncollectIndexByIndexIdAndUserId(subjectId)
            }
        }
    }

    override fun getSubjectCollections(
        username: String,
        subjectType: SubjectType?,
        subjectCollectionType: SubjectCollectionType?,
    ): PagedSource<UserSubjectCollection> {
        return PageBasedPagedSource { page ->
            try {
                val pageSize = 10
                withContext(Dispatchers.IO) {
                    client.api.getUserCollectionsByUsername(
                        username,
                        offset = page * pageSize, limit = pageSize,
                        subjectType = subjectType,
                        type = subjectCollectionType,
                    ).run {
                        total?.let { setTotalSize(it) }
                        Paged.processPagedResponse(total, pageSize, data)
                    }
                }
            } catch (e: ClientException) {
                logger.warn("Exception in getCollections, page=$page", e)
                null
            }
        }
    }

    override fun subjectCollectionTypeById(subjectId: Int): Flow<UnifiedCollectionType> {
        return flow {
            emit(
                try {
                    val username = sessionManager.username.first() ?: "-"
                    runInterruptible(Dispatchers.IO) {
                        client.api.getUserCollection(username, subjectId)
                    }.type.toCollectionType()
                } catch (e: ClientException) {
                    if (e.statusCode == 404) {
                        UnifiedCollectionType.NOT_COLLECTED
                    } else {
                        throw e
                    }
                },
            )
        }
    }
}

fun UserSubjectCollection.toSubjectCollectionItem(
    subject: Subject,
    episodes: List<UserEpisodeCollection>,
): SubjectCollection {
    if (subject.type != SubjectType.Anime) {
        return SubjectCollection(
            info = subject.createSubjectInfo(),
            episodes = episodes.map { it.toEpisodeCollection() },
            collectionType = type.toCollectionType(),
        )
    }

    return SubjectCollection(
        info = subject.createSubjectInfo(),
        episodes = episodes.map { it.toEpisodeCollection() },
        collectionType = type.toCollectionType(),
    )
}

fun Subject.createSubjectInfo(): SubjectInfo {
    return SubjectInfo(
        id = id,
        name = name,
        nameCn = nameCn,
        summary = this.summary,
        nsfw = this.nsfw,
        locked = this.locked,
        platform = this.platform,
        volumes = this.volumes,
        eps = this.eps,
        totalEpisodes = this.totalEpisodes,
        date = this.date,
        tags = this.tags.map { Tag(it.name, it.count) },
        infobox = this.infobox?.map { it.toInfoboxItem() }.orEmpty(),
        imageCommon = this.images.common,
        collection = this.collection.run {
            SubjectCollectionStats(
                wish = wish,
                doing = doing,
                done = collect,
                onHold = onHold,
                dropped = dropped,
            )
        },
        ratingInfo = this.rating.toRatingInfo(),
    )
}


private fun Rating.toRatingInfo(): RatingInfo = RatingInfo(
    rank = rank,
    total = total,
    count = count.toRatingCounts(),
    score = score.toString(),
)

private fun Count.toRatingCounts() = RatingCounts(
    intArrayOf(
        _1 ?: 0,
        _2 ?: 0,
        _3 ?: 0,
        _4 ?: 0,
        _5 ?: 0,
        _6 ?: 0,
        _7 ?: 0,
        _8 ?: 0,
        _9 ?: 0,
        _10 ?: 0,
    ),
)

fun UserEpisodeCollection.toEpisodeCollection(): EpisodeCollection {
    return EpisodeCollection(
        episodeInfo = episode.toEpisodeInfo(),
        collectionType = type.toCollectionType(),
    )
}


private class LruCache<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>(maxSize + 1, 1f, true) {
    override fun removeEldestEntry(eldest: Map.Entry<K, V>): Boolean {
        return size > maxSize
    }
}
