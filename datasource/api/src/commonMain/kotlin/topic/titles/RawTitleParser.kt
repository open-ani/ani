/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.datasources.api.topic.titles

import me.him188.ani.datasources.api.SubtitleKind
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
        allianceName: String? = null, // used to filter out alliance tags.
        builder: ParsedTopicTitle.Builder,
    )

    // TODO: 2024/9/24 Optimize RawTitleParser.parseSubtitleLanguages for L1
    open fun parseSubtitleLanguages(word: String): List<SubtitleLanguage> = parse(word).subtitleLanguages

    companion object {
        private val default by lazy(LazyThreadSafetyMode.PUBLICATION) {
            LabelFirstRawTitleParser()
        }

        fun getDefault(): RawTitleParser = default
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
    val subtitleKind: SubtitleKind? = null,
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
        var subtitleKind: SubtitleKind? = null

        fun build(): ParsedTopicTitle {
            return ParsedTopicTitle(
                tags = tags.toList(),
                chineseTitle = chineseTitle?.trim(),
                otherTitles = otherTitles.mapNotNull { title -> title.trim().takeIf { it.isNotEmpty() } },
                episodeRange = episodeRange,
                resolution = resolution,
                frameRate = frameRate,
                mediaOrigin = mediaOrigin,
                subtitleLanguages = subtitleLanguages.toList(),
                subtitleKind = subtitleKind,
            )
        }
    }
}

fun RawTitleParser.parse(text: String, allianceName: String? = null): ParsedTopicTitle {
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
        subtitleKind = subtitleKind,
    )
}