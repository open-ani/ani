package me.him188.ani.app.data.repositories

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.database.dao.SearchHistoryDao
import me.him188.ani.app.data.database.eneity.SearchHistoryEntity
import org.koin.core.component.KoinComponent

@Stable
interface SearchHistoryRepository {
    fun add(history: SearchHistoryEntity)
    fun getFlow(): Flow<List<SearchHistoryEntity>>
    fun deleteBySeq(seq: Int)
}

class SearchHistoryRepositoryImpl(
    private val searchHistory: SearchHistoryDao
) : SearchHistoryRepository, KoinComponent {
    override fun add(history: SearchHistoryEntity) {
        searchHistory.insert(history)
    }

    override fun getFlow(): Flow<List<SearchHistoryEntity>> {
        return searchHistory.getFlow()
    }

    override fun deleteBySeq(seq: Int) {
        searchHistory.deleteBySequence(seq)
    }
}