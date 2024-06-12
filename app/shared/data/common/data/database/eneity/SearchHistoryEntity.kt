package me.him188.ani.app.data.database.eneity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val sequence: Int = 0,
    val content: String
)