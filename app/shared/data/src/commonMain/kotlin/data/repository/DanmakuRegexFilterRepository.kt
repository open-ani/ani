package me.him188.ani.app.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.models.danmaku.DanmakuRegexFilter

interface DanmakuRegexFilterRepository : Repository {

    val flow: Flow<List<DanmakuRegexFilter>>

    suspend fun update(id: String, new: DanmakuRegexFilter)
    suspend fun remove(filter: DanmakuRegexFilter)
    suspend fun add(new: DanmakuRegexFilter)

}

class DanmakuRegexFilterRepositoryImpl(
    val store: DataStore<List<DanmakuRegexFilter>>,
) : DanmakuRegexFilterRepository {

    override val flow: Flow<List<DanmakuRegexFilter>> = store.data

    override suspend fun update(id: String, new: DanmakuRegexFilter) {
        store.updateData { data ->
            data.map {
                if (it.id == id) new else it
            }
        }
    }

    override suspend fun remove(filter: DanmakuRegexFilter) {
        store.updateData { data ->
            data - filter
        }
    }

    override suspend fun add(new: DanmakuRegexFilter) {
        store.updateData { data ->
            data + new
        }
    }

    companion object {
        fun create(store: DataStore<List<DanmakuRegexFilter>>): DanmakuRegexFilterRepository {
            return DanmakuRegexFilterRepositoryImpl(store)
        }
    }

}