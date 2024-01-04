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

package me.him188.animationgarden.datasources.dmhy

import me.him188.animationgarden.datasources.api.topic.Alliance
import me.him188.animationgarden.datasources.api.topic.Author
import me.him188.animationgarden.datasources.api.topic.Episode
import me.him188.animationgarden.datasources.api.topic.FileSize
import me.him188.animationgarden.datasources.api.topic.FrameRate
import me.him188.animationgarden.datasources.api.topic.MediaOrigin
import me.him188.animationgarden.datasources.api.topic.Resolution
import me.him188.animationgarden.datasources.api.topic.SubtitleLanguage
import me.him188.animationgarden.datasources.dmhy.impl.titles.RawTitleParser
import me.him188.animationgarden.datasources.dmhy.impl.titles.parse

class DmhyCategory(
    val id: String,
    val name: String,
) 

data class DmhyTopic(
    val id: String,
    val publishedTime: Long,
    val category: DmhyCategory,
    val alliance: Alliance?,
    val rawTitle: String,
    val commentsCount: Int,
    val magnetLink: String,
    val size: FileSize,
    val author: Author,
) {
    val details: DmhyTopicDetails? by lazy {
        DmhyTopicDetails.Builder().apply {
            RawTitleParser.getParserFor().parse(rawTitle, alliance?.name, this)
        }.build()
    }
}

data class DmhyTopicDetails(
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

        fun build(): DmhyTopicDetails {
            return DmhyTopicDetails(
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



private fun String.truncated(length: Int, truncated: String = "..."): String {
    return if (this.length > length) {
        this.take(length) + truncated
    } else {
        this
    }
}
