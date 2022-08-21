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

package me.him188.animationgarden.api.model

import kotlinx.serialization.Serializable
import me.him188.animationgarden.api.tags.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class TopicDetails(
    val tags: List<String> = listOf(),
    val chineseTitle: String? = null,
    val otherTitles: List<String> = listOf(),
    val episode: Episode? = null,
    val resolution: Resolution? = null,
    val frameRate: FrameRate? = null,
    val mediaOrigin: MediaOrigin? = null,
    val subtitleLanguages: List<SubtitleLanguage> = listOf(),
) {
    class Builder {
        var tags = mutableSetOf<String>()
        var chineseTitle: String? = null
        var otherTitles = mutableSetOf<String>()
        var episode: Episode? = null
        var resolution: Resolution? = null
        var frameRate: FrameRate? = null
        var mediaOrigin: MediaOrigin? = null
        var subtitleLanguages = mutableSetOf<SubtitleLanguage>()

        fun build(): TopicDetails {
            return TopicDetails(
                tags = tags.toList(),
                chineseTitle = chineseTitle?.trim(),
                otherTitles = otherTitles.mapNotNull { title -> title.trim().takeIf { it.isNotEmpty() } },
                episode = episode,
                resolution = resolution,
                frameRate = frameRate,
                mediaOrigin = mediaOrigin,
                subtitleLanguages = subtitleLanguages.toList()
            )
        }
    }
}

class Topic(
    val id: String,
    val date: LocalDateTime,
    val category: TopicCategory,
    val alliance: Alliance?,
    val rawTitle: String,
    val commentsCount: Int,
    val magnetLink: MagnetLink,
    val size: FileSize,
    val author: User
) {
    val details: TopicDetails? by lazy {
        val builder = TopicDetails.Builder()
        RawTitleParser.getParserFor().parse(rawTitle, alliance?.name, builder)
        builder.build()
    }

    override fun toString(): String {
        return "$id " +
                "| ${date.format(DATE_FORMAT)} " +
                "| ${category.name} " +
                "| ${alliance?.name} " +
                "| $rawTitle " +
                "| $commentsCount " +
                "| ${magnetLink.value.truncated(10)} " +
                "| $size " +
                "| $author"
    }
}

@JvmInline
value class MagnetLink(
    val value: String
)

data class TopicCategory(
    val id: String,
    val name: String
)

@Serializable
data class Alliance(
    val id: String,
    val name: String
)

private fun String.truncated(length: Int, truncated: String = "..."): String {
    return if (this.length > length) {
        this.take(length) + truncated
    } else {
        this
    }
}

val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
