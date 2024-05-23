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

import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FrameRate
import me.him188.ani.datasources.api.topic.MediaOrigin
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.SubtitleLanguage

/**
 * 只解析剧集, 分辨率等必要信息, 不解析标题. 拥有更高正确率
 */
class LabelFirstRawTitleParser : RawTitleParser() {
    override fun parse(
        text: String,
        allianceName: String?,
        builder: ParsedTopicTitle.Builder,
    ) {
        Session(builder).run {
            val words = mutableListOf<String>()
            for (s in text.splitWords()) {
                if (s.isBlank()) continue
                if (newAnime.matches(s)) {
                    builder.tags.add(s)
                    continue
                }
                val word = s.remove("招募").remove("招新").trim()
                words.add(word)
            }

            // 第一遍, 解析剧集, 分辨率, 字幕等
            for (word in words) {
                parseWord(word)
            }

            // 第二遍, 如果没有解析到剧集, 找是不是有 "BDRip", 判定为季度全集
            if (builder.episodeRange == null) {
                words.forEach { word ->
                    if (word.contains("BD", ignoreCase = true)
                        || word.contains("Blu-Ray", ignoreCase = true)
                    ) {
                        builder.episodeRange = EpisodeRange.unknownSeason()
                    }
                }
            }
        }
        return
    }

    class Session(
        private val builder: ParsedTopicTitle.Builder,
    ) {
        fun parseWord(word: String): Boolean {
            var anyMatched = false
            anyMatched = anyMatched or word.parseSubtitleLanguages()
            anyMatched = anyMatched or word.parseResolution()
            anyMatched = anyMatched or word.parseFrameRate()
            anyMatched = anyMatched or word.parseEpisode()
            anyMatched = anyMatched or word.parseMediaOrigin()

            return anyMatched
        }


        private fun String.parseSubtitleLanguages(): Boolean {
            var any = false
            for (entry in SubtitleLanguage.matchableEntries) {
                if (entry.matches(this)) {
                    builder.subtitleLanguages.add(entry)
                    any = true
                }
            }
            return any
        }

        private fun String.parseResolution(): Boolean {
            return Resolution.tryParse(this)?.let {
                builder.resolution = it
            } != null
        }

        private fun String.parseFrameRate(): Boolean {
            return FrameRate.tryParse(this)?.let {
                builder.frameRate = it
            } != null
        }

        private fun String.parseMediaOrigin(): Boolean {
            return MediaOrigin.tryParse(this)?.let {
                builder.mediaOrigin = it
            } != null
        }

        private fun String.parseEpisode(): Boolean {
            if (this.contains("x264", ignoreCase = true)
                || this.contains("x265", ignoreCase = true)
            ) return false

            val str = this
                .remove("第")
                .remove("_完")
                .remove("完")
                .remove("话")
                .remove("END")
                .remove("集")
                .remove("話")
                .remove("版") // 第06話V2版
                .remove("v1")
                .remove("v2")
                .remove("v3")
            str.toFloatOrNull()?.let {
                builder.episodeRange = EpisodeRange.single(str)
                return true
            }
            collectionPattern.find(str)?.let { result ->
                if (result.groupValues.size < 4) {
                    return@let
                }
                val (start, end) = result.destructured
                if (end.startsWith("0") && !start.startsWith("0")) {
                    // "Hibike! Euphonium 3 - 02"
                    return@let
                }

                if (result.groupValues.size >= 5 && result.groupValues[4].isNotBlank()) {
                    builder.episodeRange = EpisodeRange.combined(
                        EpisodeRange.range(start, end),
                        EpisodeRange.single(EpisodeSort(result.groupValues[4].removePrefix("+")))
                    )
                } else {
                    builder.episodeRange = EpisodeRange.range(start, end)
                }
                return true
            }
            if (str.contains("SP", ignoreCase = true) // 包括 "Special"
                || str.contains("OVA", ignoreCase = true)
                || str.contains("小剧场")
                || str.contains("特别篇")
            ) {
                builder.episodeRange = EpisodeRange.single(this)
                return true
            }
            return false
        }

    }
}

private val newAnime = Regex("(?:★?|★(.*)?)([0-9]|[一二三四五六七八九十]{0,4}) ?[月年] ?(?:新番|日剧)★?")
private val brackets = Regex("""[\[【(](.*?)[]】)]""")

private val collectionPattern = Regex(
    """(\d{1,4})\s?-{1,2}\s?(\d{1,4})(TV|BDrip|BD)?(\+.+)?""",
    RegexOption.IGNORE_CASE
)

private fun String.remove(str: String) = replace(str, "", ignoreCase = true)

private fun String.splitWords(): Sequence<String> {
    val text = this
    return sequence {
        var index = 0
        for (result in brackets.findAll(text)) {
            if (index < result.range.first) {
                yieldAll(
                    text.substring(index until result.range.first)
                        .splitToSequence('/', '\\', '|', ' ')
                )
            }
            index = result.range.last + 1

            val tag = result.groups[1]!!.value // can be "WebRip 1080p HEVC-10bit AAC" or "简繁内封字幕"
            yield(tag)
        }
        if (index < text.length) {
            yieldAll(
                text.substring(index until text.length)
                    .splitToSequence('/', '\\', '|', ' ')
            )
        }
    }
}
