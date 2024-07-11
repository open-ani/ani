package me.him188.ani.app.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import me.him188.ani.danmaku.ui.DanmakuFilterConfig
import me.him188.ani.danmaku.ui.DanmakuRegexFilter

interface DanmakuRegexFilterRepository : Repository {

    val flow: Flow<DanmakuFilterConfig>

    suspend fun update(id: String, new: DanmakuRegexFilter)
    suspend fun remove(filter: DanmakuRegexFilter)
    suspend fun add(new: DanmakuRegexFilter)

}

class DanmakuRegexFilterRepositoryImpl(
    val store: DataStore<DanmakuFilterConfig>,
) : DanmakuRegexFilterRepository {

    override val flow: Flow<DanmakuFilterConfig> = store.data

    override suspend fun update(id: String, new: DanmakuRegexFilter) {
        store.updateData { danmakuFilterConfig ->
            danmakuFilterConfig.copy(
                danmakuRegexFilterList = danmakuFilterConfig.danmakuRegexFilterList.map {
                    if (it.id == id) new else it
                },
            )
        }
    }

    override suspend fun remove(filter: DanmakuRegexFilter) {
        store.updateData {
            it.copy(danmakuRegexFilterList = it.danmakuRegexFilterList.filter { it.id != filter.id })
        }
    }

    override suspend fun add(new: DanmakuRegexFilter) {
        store.updateData {
            it.copy(danmakuRegexFilterList = it.danmakuRegexFilterList + new)
        }
    }

    companion object {
        fun create(store: DataStore<DanmakuFilterConfig>): DanmakuRegexFilterRepository {
            return DanmakuRegexFilterRepositoryImpl(store)
        }
    }

}