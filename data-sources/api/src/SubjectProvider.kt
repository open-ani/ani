/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.animationgarden.datasources.api

/**
 * 提供番剧名称索引的数据源. 支持使用关键字搜索正式名称.
 */
interface SubjectProvider {
    /**
     * Unique ID. Can be name of the provider.
     */
    val id: String

    suspend fun startSearch(query: SubjectSearchQuery): SearchSession<DataSourceSubject>
}

class DataSourceSubject(
    /**
     * 条目官方原名称, 例如番剧为日文名称
     */
    val originalName: String,
    /**
     * 条目中文名称
     */
    val chineseName: String,
    val episodeCount: Int,
    /**
     * 平均评分
     */
    val ratingScore: Double,
    /**
     * 评分人数
     */
    val ratingCount: Int,
    val rank: Int,
    val sourceUrl: String, // 数据源
    val images: DataSourceSubjectImages,
)

interface DataSourceSubjectImages {
    /**
     * Get image URL for grid view.
     */
    fun forGrid(): String?

    fun forPoster(): String?
}

class SubjectSearchQuery(
    val keyword: String,
    val type: NameIndexSearchType = NameIndexSearchType.ANIME,
)

enum class NameIndexSearchType {
    ANIME,

    /*
    bangumi supports
            条目类型
            - `1` 为 书籍
            - `2` 为 动画
            - `3` 为 音乐
            - `4` 为 游戏
            - `6` 为 三次元
            
            没有 `5`
     */
}
