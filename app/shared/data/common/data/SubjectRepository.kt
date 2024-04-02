package me.him188.ani.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import me.him188.ani.datasources.api.PageBasedPagedSource
import me.him188.ani.datasources.api.Paged
import me.him188.ani.datasources.api.PagedSource
import me.him188.ani.datasources.api.processPagedResponse
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.models.Subject
import org.openapitools.client.models.SubjectCollectionType
import org.openapitools.client.models.SubjectType
import org.openapitools.client.models.UserSubjectCollection
import org.openapitools.client.models.UserSubjectCollectionModifyPayload

interface SubjectRepository : Repository {
    suspend fun getSubject(id: Int): Subject?


    fun getSubjectCollections(
        username: String,
        subjectType: SubjectType? = null,
        subjectCollectionType: SubjectCollectionType? = null,
    ): PagedSource<UserSubjectCollection>

    suspend fun patchSubjectCollection(subjectId: Int, payload: UserSubjectCollectionModifyPayload)
    suspend fun deleteSubjectCollection(subjectId: Int)
}

suspend fun SubjectRepository.setSubjectCollectionTypeOrDelete(subjectId: Int, type: SubjectCollectionType?) {
    return if (type == null) {
        deleteSubjectCollection(subjectId)
    } else {
        patchSubjectCollection(subjectId, UserSubjectCollectionModifyPayload(type))
    }
}

class SubjectRepositoryImpl : SubjectRepository, KoinComponent {
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
                        Paged.processPagedResponse(total, pageSize, data)
                    }
                }
            } catch (e: ClientException) {
                logger.warn("Exception in getCollections", e)
                null
            }
        }
    }
}

private class LruCache<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>(maxSize + 1, 1f, true) {
    override fun removeEldestEntry(eldest: Map.Entry<K, V>): Boolean {
        return size > maxSize
    }
}
