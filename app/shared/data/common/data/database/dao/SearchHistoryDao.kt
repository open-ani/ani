package me.him188.ani.app.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.database.eneity.SearchHistoryEntity

@Dao
interface SearchHistoryDao {
    @Upsert
    suspend fun insert(item: SearchHistoryEntity)

    @Query("delete from `search_history` where `sequence`=:sequence")
    suspend fun deleteBySequence(sequence: Int)

    @Query("select * from `search_history` order by sequence desc")
    fun getFlow(): Flow<List<SearchHistoryEntity>>
}