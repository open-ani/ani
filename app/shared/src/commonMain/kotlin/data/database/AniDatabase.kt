package me.him188.ani.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import me.him188.ani.app.data.database.dao.SearchHistoryDao
import me.him188.ani.app.data.database.dao.SearchTagDao
import me.him188.ani.app.data.database.eneity.SearchHistoryEntity
import me.him188.ani.app.data.database.eneity.SearchTagEntity

@Database(
    entities = [
        SearchHistoryEntity::class,
        SearchTagEntity::class,
    ],
    version = 1,
)
abstract class AniDatabase : RoomDatabase() {
    abstract fun searchHistory(): SearchHistoryDao
    abstract fun searchTag(): SearchTagDao
}