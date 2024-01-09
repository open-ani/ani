package me.him188.ani.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.withContext
import me.him188.ani.datasources.api.PageBasedSearchSession
import me.him188.ani.datasources.api.Paged
import me.him188.ani.datasources.bangumi.BangumiClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.UserSubjectCollection

interface CollectionRepository {
    suspend fun getCollections(
        username: String,
    ): Flow<List<UserSubjectCollection>>
}

class CollectionRepositoryImpl : CollectionRepository, KoinComponent {
    private val client: BangumiClient by inject()

    override suspend fun getCollections(
        username: String,
    ): Flow<List<UserSubjectCollection>> = withContext(Dispatchers.IO) {
        PageBasedSearchSession { page ->
            client.api.getUserCollectionsByUsername(username, offset = page * 30, limit = 30).run {
                Paged(this.total ?: 0, !this.data.isNullOrEmpty(), this.data.orEmpty())
            }
        }.results.runningFold(mutableListOf()) { acc, list ->
            acc.apply { add(list) }
        }
    }
}