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

import me.him188.ani.datasources.api.DownloadSearchQuery
import me.him188.ani.datasources.api.MediaSource
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * An item search from a [MediaSource]
 */
class Topic(
    /**
     * 数据源内部的 ID.
     */
    val id: String,

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
        return "Topic(id='$id', publishedTimeMillis=$publishedTimeMillis, category=$category, rawTitle='$rawTitle', commentsCount=$commentsCount, downloadLink='$downloadLink', size=$size, alliance='$alliance', author=$author, details=$details, originalLink='$originalLink')"
    }
}

private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

fun Topic.publishedTimeString(): String? {
    return publishedTimeMillis?.let {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(it),
            ZoneId.systemDefault(),
        ).format(DATE_FORMAT)
    }
}

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

fun DownloadSearchQuery.matches(topic: Topic): Boolean {
    val details = topic.details ?: return true
    episodeSort?.let { expected ->
        val ep = details.episode
        if (ep != null && ep.raw.removePrefix("0").removeSuffix(".0") !=
            expected.removePrefix("0").removeSuffix(".0")
        ) return false
    }
    return true
}

enum class TopicCategory {
    ANIME,
}