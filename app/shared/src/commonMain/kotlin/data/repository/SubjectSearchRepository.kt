package me.him188.ani.app.data.repository

import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.persistent.database.dao.SearchHistoryDao
import me.him188.ani.app.data.persistent.database.dao.SearchTagDao
import me.him188.ani.app.data.persistent.database.eneity.SearchHistoryEntity
import me.him188.ani.app.data.persistent.database.eneity.SearchTagEntity
import org.koin.core.component.KoinComponent

interface SubjectSearchRepository {
    suspend fun addHistory(history: SearchHistoryEntity)
    fun getHistoryFlow(): Flow<List<SearchHistoryEntity>>
    suspend fun deleteHistoryBySeq(seq: Int)

    suspend fun addTag(tag: SearchTagEntity)
    fun getTagFlow(): Flow<List<SearchTagEntity>>
    suspend fun deleteTagByName(content: String)
    suspend fun increaseCountByName(content: String)
    suspend fun deleteTagById(id: Int)
    suspend fun increaseCountById(id: Int)
}

class SubjectSearchRepositoryImpl(
    private val searchHistory: SearchHistoryDao,
    private val searchTag: SearchTagDao,
) : SubjectSearchRepository, KoinComponent {
    override suspend fun addHistory(history: SearchHistoryEntity) {
        searchHistory.insert(history)
    }

    override fun getHistoryFlow(): Flow<List<SearchHistoryEntity>> {
        return searchHistory.getFlow()
    }

    override suspend fun deleteHistoryBySeq(seq: Int) {
        searchHistory.deleteBySequence(seq)
    }

    override suspend fun addTag(tag: SearchTagEntity) {
        searchTag.insert(tag)
    }

    override fun getTagFlow(): Flow<List<SearchTagEntity>> {
        return searchTag.getFlow()
    }

    override suspend fun deleteTagByName(content: String) {
        searchTag.deleteByName(content)
    }

    override suspend fun increaseCountByName(content: String) {
        searchTag.increaseCountByName(content)
    }

    override suspend fun deleteTagById(id: Int) {
        searchTag.deleteById(id)
    }

    override suspend fun increaseCountById(id: Int) {
        searchTag.increaseCountById(id)
    }
}