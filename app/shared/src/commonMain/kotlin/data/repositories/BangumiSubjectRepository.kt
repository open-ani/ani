package me.him188.ani.app.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.subject.EpisodeCollection
import me.him188.ani.app.data.subject.RatingInfo
import me.him188.ani.app.data.subject.SubjectCollection
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.data.subject.createSubjectInfo
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.paging.processPagedResponse
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.datasources.bangumi.processing.airSeason
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
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
                runInterruptible(Dispatchers.IO) {
                    client.api.getUserCollection("-", subjectId)
                }.type.toCollectionType(),
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
            subjectId = subjectId,
            displayName = this.subject?.nameCNOrName() ?: "",
            image = "",
            rate = subject.rating.toRatingInfo(),
            date = this.subject?.airSeason,
            totalEps = episodes.size,
            episodes = episodes.map { it.toEpisodeCollection() },
            collectionType = type.toCollectionType(),
            info = subject.createSubjectInfo(),
        )
    }

    return SubjectCollection(
        subjectId = subjectId,
        displayName = subject.nameCNOrName(),
        image = subject.images.common,
        rate = subject.rating.toRatingInfo(),
        date = subject.airSeason ?: "",
        totalEps = episodes.size,
        episodes = episodes.map { it.toEpisodeCollection() },
        collectionType = type.toCollectionType(),
        info = subject.createSubjectInfo(),
    )
}

private fun Rating.toRatingInfo(): RatingInfo = RatingInfo(
    rank = rank,
    total = total,
    count = count.toMap(),
    score = score.toFloat(),
)

private fun Count.toMap(): Map<Int, Int> = buildMap(10) {
    put(1, _1 ?: 0)
    put(2, _2 ?: 0)
    put(3, _3 ?: 0)
    put(4, _4 ?: 0)
    put(5, _5 ?: 0)
    put(6, _6 ?: 0)
    put(7, _7 ?: 0)
    put(8, _8 ?: 0)
    put(9, _9 ?: 0)
    put(10, _10 ?: 0)
}

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
