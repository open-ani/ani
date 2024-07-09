package me.him188.ani.app.data.persistent.database.eneity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_tag")
data class SearchTagEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    /**
     * 使用此 tag 搜索的次数，次数越高在搜索建议中排名越靠前
     */
    val useCount: Int = 1,
)