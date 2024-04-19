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

package me.him188.ani.datasources.api.topic.titles

import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FrameRate
import me.him188.ani.datasources.api.topic.MediaOrigin
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.SubtitleLanguage
import me.him188.ani.datasources.api.topic.TopicDetails

/**
 * 解析字幕组发布的标题
 */
abstract class RawTitleParser {
    // 【极影字幕社】 ★7月新番 【来自深渊 烈日的黄金乡】【Made in Abyss - Retsujitsu no Ougonkyou】【04】GB MP4_1080P
    // [獸耳娘噠萌進化][第352期][500P]
    // [猎户不鸽发布组] 比赛开始,零比零 / 羽球青春 Love All Play [16-17] [1080p] [简中] [网盘] [2022年4月番]
    // [喵萌奶茶屋&LoliHouse] 继母的拖油瓶是我的前女友 / Mamahaha no Tsurego ga Motokano datta - 04 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]

    abstract fun parse(
        text: String,
        allianceName: String?, // used to filter out alliance tags.
        collectTag: (title: String) -> Unit,
        collectChineseTitle: (String) -> Unit,
        collectOtherTitle: (String) -> Unit,
        collectEpisode: (EpisodeRange) -> Unit, // may be 12.5 or SP1
        collectResolution: (Resolution) -> Unit,
        collectFrameRate: (FrameRate) -> Unit,
        collectMediaOrigin: (MediaOrigin) -> Unit, // WebRip BDRip Blu-ray
        collectSubtitleLanguage: (SubtitleLanguage) -> Unit, // may be called multiple times
    )


    companion object {
        fun getDefault(): RawTitleParser {
            return PatternBasedRawTitleParser()
//            when (allianceName) {
//                "LoliHouse",
//                "NC-Raws",
//                "Lilith-Raws",
//                "桜都字幕组",
//                "Skymoon-Raws",
//                "天月動漫&發佈組",
//                "天月搬運組",
//                -> {
//                    return RawTitleParserA()
//                }
//
//                else -> {}
//            }
//            return null
        }
    }
}

data class ParsedTopicTitle(
    val tags: List<String> = listOf(),
    val chineseTitle: String? = null,
    val otherTitles: List<String> = listOf(),
    val episodeRange: EpisodeRange? = null,
    val resolution: Resolution? = null,
    val frameRate: FrameRate? = null,
    val mediaOrigin: MediaOrigin? = null,
    val subtitleLanguages: List<SubtitleLanguage> = listOf(),
) {
    class Builder {
        var tags = mutableSetOf<String>()
        var chineseTitle: String? = null
        var otherTitles = mutableSetOf<String>()
        var episodeRange: EpisodeRange? = null
        var resolution: Resolution? = null
        var frameRate: FrameRate? = null
        var mediaOrigin: MediaOrigin? = null
        var subtitleLanguages = mutableSetOf<SubtitleLanguage>()

        fun build(): ParsedTopicTitle {
            return ParsedTopicTitle(
                tags = tags.toList(),
                chineseTitle = chineseTitle?.trim(),
                otherTitles = otherTitles.mapNotNull { title -> title.trim().takeIf { it.isNotEmpty() } },
                episodeRange = episodeRange,
                resolution = resolution,
                frameRate = frameRate,
                mediaOrigin = mediaOrigin,
                subtitleLanguages = subtitleLanguages.toList()
            )
        }
    }
}

fun RawTitleParser.parse(text: String, allianceName: String?, builder: ParsedTopicTitle.Builder) {
    return parse(
        text, allianceName,
        collectTag = { builder.tags.add(it) },
        collectChineseTitle = { builder.chineseTitle = it },
        collectOtherTitle = { builder.otherTitles.add(it) },
        collectEpisode = { builder.episodeRange = it },
        collectResolution = { builder.resolution = it },
        collectFrameRate = { builder.frameRate = it },
        collectMediaOrigin = { builder.mediaOrigin = it },
        collectSubtitleLanguage = { builder.subtitleLanguages.add(it) }
    )
}

fun RawTitleParser.parse(text: String, allianceName: String?): ParsedTopicTitle {
    return ParsedTopicTitle.Builder().run {
        parse(text, allianceName, this)
        build()
    }
}

fun ParsedTopicTitle.toTopicDetails(): TopicDetails {
    return TopicDetails(
        tags = tags,
        chineseTitle = chineseTitle,
        otherTitles = otherTitles,
        episodeRange = episodeRange,
        resolution = resolution,
        frameRate = frameRate,
        mediaOrigin = mediaOrigin,
        subtitleLanguages = subtitleLanguages,
    )
}