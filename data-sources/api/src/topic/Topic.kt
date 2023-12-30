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

package me.him188.animationgarden.datasources.api.topic

/**
 * 可供下载的搜索出来的一个条目
 */
class Topic(
    /**
     * 数据源内部的 ID.
     */
    val id: String,

    /**
     * 发布时间戳
     */
    val publishedTime: Long,

    val category: TopicCategory,

    val rawTitle: String,
    val commentsCount: Int,
    val magnetLink: String,
    val size: FileSize,

    val alliance: Alliance?,
    val author: Author,

    /**
     * 解析后的标题信息
     */
    val details: TopicDetails?,
)

class TopicDetails(
    val tags: List<String>,
    val chineseTitle: String?,
    val otherTitles: List<String>,
    val episode: Episode?,
    val resolution: Resolution?,
    val frameRate: FrameRate?,
    val mediaOrigin: MediaOrigin?,
    val subtitleLanguages: List<SubtitleLanguage>,
)

enum class TopicCategory {
    ANIME,
}