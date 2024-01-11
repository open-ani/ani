package me.him188.ani.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.him188.ani.datasources.api.PageBasedSearchSession
import me.him188.ani.datasources.api.Paged
import me.him188.ani.datasources.bangumi.BangumiClient
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.models.SubjectCollectionType
import org.openapitools.client.models.SubjectType
import org.openapitools.client.models.UserSubjectCollection

interface CollectionRepository {
    suspend fun getCollections(
        username: String,
        subjectType: SubjectType? = null,
        subjectCollectionType: SubjectCollectionType? = null,
    ): Flow<UserSubjectCollection>
}

class CollectionRepositoryImpl : CollectionRepository, KoinComponent {
    private val client: BangumiClient by inject()
    private val logger = logger(CollectionRepositoryImpl::class)

    override suspend fun getCollections(
        username: String,
        subjectType: SubjectType?,
        subjectCollectionType: SubjectCollectionType?,
    ): Flow<UserSubjectCollection> = withContext(Dispatchers.IO) {
        PageBasedSearchSession { page ->
            try {
                client.api.getUserCollectionsByUsername(
                    username,
                    offset = page * 30, limit = 30,
                    subjectType = subjectType,
                    type = subjectCollectionType,
                ).run {
                    Paged(this.total ?: this.data?.size ?: 0, !this.data.isNullOrEmpty(), this.data.orEmpty())
                }
            } catch (e: ClientException) {
                logger.warn("Exception in getCollections", e)
                null
            }
        }.results
    }
}