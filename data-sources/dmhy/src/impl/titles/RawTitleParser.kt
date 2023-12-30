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

package me.him188.animationgarden.datasources.dmhy.impl.titles

import me.him188.animationgarden.datasources.api.topic.*

/**
 * 解析字幕组发布的标题
 */
internal abstract class RawTitleParser {
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
        collectEpisode: (Episode) -> Unit, // may be 12.5 or SP1
        collectResolution: (Resolution) -> Unit,
        collectFrameRate: (FrameRate) -> Unit,
        collectMediaOrigin: (MediaOrigin) -> Unit, // WebRip BDRip Blu-ray
        collectSubtitleLanguage: (SubtitleLanguage) -> Unit, // may be called multiple times
    )


    companion object {
        fun getParserFor(): RawTitleParser {
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