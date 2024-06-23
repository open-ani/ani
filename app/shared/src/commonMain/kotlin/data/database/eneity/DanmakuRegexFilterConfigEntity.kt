package me.him188.ani.app.data.database.eneity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "danmaku_regex_filter_config")
data class DanmakuRegexFilterConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val enabled: Boolean = true,
    val name: String,
    val regex: String,
)