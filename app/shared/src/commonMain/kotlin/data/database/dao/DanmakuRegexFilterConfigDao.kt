package me.him188.ani.app.data.database.dao

import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.database.eneity.DanmakuRegexFilterConfigEntity

interface DanmakuRegexFilterConfigDao {
    var enabled: Boolean

    fun toggleEnabled()

    @Query("select * from `danmaku_regex_filter_config`")
    fun getFlow(): Flow<List<DanmakuRegexFilterConfigEntity>>

    @Query("delete from `danmaku_regex_filter_config` where `id`=:id")
    fun deleteDanmakuRegexFilterConfigById(id: String)

    @Query("update `danmaku_regex_filter_config` set `enabled` = not `enabled` where `id`=:id")
    fun toggleDanmakuRegexFilterConfigById(id: String)

    @Upsert
    suspend fun insert(item: DanmakuRegexFilterConfigEntity)
}