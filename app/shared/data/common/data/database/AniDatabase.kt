package me.him188.ani.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import me.him188.ani.app.data.database.dao.SearchHistoryDao
import me.him188.ani.app.data.database.eneity.SearchHistoryEntity

@Database(
    entities = [
        SearchHistoryEntity::class
    ], version = 1
)
abstract class AniDatabase : RoomDatabase() {
    abstract fun searchHistory(): SearchHistoryDao
}