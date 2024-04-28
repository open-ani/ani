package me.him188.ani.app.data.repositories

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.mikan.MikanIndexCacheProvider

interface MikanIndexCacheRepository : Repository, MikanIndexCacheProvider {
    override suspend fun getMikanSubjectId(bangumiSubjectId: String): String?
    override suspend fun setMikanSubjectId(bangumiSubjectId: String, mikanSubjectId: String)
}

@Serializable
data class MikanIndexes(
    val data: Map<String, String> = emptyMap()
) {
    companion object {
        val Empty = MikanIndexes()
    }
}

class MikanIndexCacheRepositoryImpl(
    private val store: DataStore<MikanIndexes>,
) : MikanIndexCacheRepository {
    override suspend fun getMikanSubjectId(bangumiSubjectId: String): String? {
        return store.data.map {
            it.data[bangumiSubjectId]
        }.firstOrNull()
    }

    override suspend fun setMikanSubjectId(bangumiSubjectId: String, mikanSubjectId: String) {
        store.updateData {
            it.copy(data = it.data + (bangumiSubjectId to mikanSubjectId))
        }
    }
}