package me.him188.ani.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.database.eneity.SearchHistoryEntity

@Dao
interface SearchHistoryDao {
    @Insert
    fun insert(item: SearchHistoryEntity)

    @Query("delete from search_history where `sequence`=:sequence")
    fun deleteBySequence(sequence: Int)

    @Query("select * from search_history")
    fun getFlow(): Flow<List<SearchHistoryEntity>>
}