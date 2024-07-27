/*
 * Ani
 * Copyright (C) 2022-2024 Him188
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

package me.him188.ani.datasources.api.topic

import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.source.DownloadSearchQuery
import me.him188.ani.datasources.api.source.MediaSource

/**
 * An item search from a [MediaSource]
 */
@Serializable
class Topic(
    /**
     * 数据源内部的 ID.
     */
    val topicId: String,

    /**
     * 发布时间戳
     */
    val publishedTimeMillis: Long?,

    val category: TopicCategory,

    val rawTitle: String,
    val commentsCount: Int,
    val downloadLink: ResourceLocation,
    val size: FileSize,

    val alliance: String,
    val author: Author?,

    /**
     * 解析后的标题信息
     */
    val details: TopicDetails?,

    val originalLink: String,
) {
    override fun toString(): String {
        return "Topic(id='$topicId', publishedTimeMillis=$publishedTimeMillis, category=$category, rawTitle='$rawTitle', commentsCount=$commentsCount, downloadLink='$downloadLink', size=$size, alliance='$alliance', author=$author, details=$details, originalLink='$originalLink')"
    }
}

@Serializable
class TopicDetails(
    val tags: List<String>,
    val chineseTitle: String?,
    val otherTitles: List<String>,
    val episodeRange: EpisodeRange?,
    val resolution: Resolution?,
    val frameRate: FrameRate?,
    val mediaOrigin: MediaOrigin?,
    val subtitleLanguages: List<SubtitleLanguage>,
)

fun DownloadSearchQuery.matches(topic: Topic, allowEpMatch: Boolean): Boolean {
    return TopicCriteria(
        episodeSort = episodeSort,
        episodeEp = episodeEp,
        fallback = allowAny,
    ).matches(topic, allowEpMatch)
}

@Serializable
enum class TopicCategory {
    ANIME,
}