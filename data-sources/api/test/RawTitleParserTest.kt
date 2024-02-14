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

package me.him188.ani.datasources.api

import me.him188.ani.datasources.api.titles.ParsedTopicTitle
import me.him188.ani.datasources.api.titles.PatternBasedRawTitleParser
import me.him188.ani.datasources.api.titles.parse
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

internal class RawTitleParserTest {
    val data = mapOf(
        "Lilith-Raws [Lilith-Raws] Overlord IV - 05 [Baha][WEB-DL][1080p][AVC AAC][CHT][MP4]" to ParsedTopicTitle(
            listOf("Baha", "WEB-DL", "AVC", "AAC", "MP4")
        )
    )

    val dataA = """
            Lilith-Raws [Lilith-Raws] Overlord IV - 05 [Baha][WEB-DL][1080p][AVC AAC][CHT][MP4]
            NC-Raws [NC-Raws] OVERLORD IV / Overlord S4 - 05 (B-Global 3840x2160 HEVC AAC MKV)
            桜都字幕组 [桜都字幕组] RWBY 冰雪帝国 / RWBY Hyousetsu Teikoku [05][1080p][简繁内封]
            天月動漫&發佈組 [Skymoon-Raws] 新網球王子: U-17 WORLD CUP / Shin Tennis no Ouji-sama: U-17 World Cup - 05 [ViuTV][WEB-RIP][720p][HEVC AAC][CHT][MP4]
            天月動漫&發佈組 [天月搬運組] 異世界迷宮裡的後宮生活 / Isekai Meikyuu de Harem wo - 05 [1080P][簡繁日外掛]
            LoliHouse [喵萌奶茶屋&LoliHouse] 风都侦探 / Fuuto Tantei / FUUTO PI - 01 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]
            LoliHouse [LoliHouse] 邪神与厨二病少女X / Jashin-chan Dropkick X - 05 [WebRip 1080p HEVC-10bit AAC][简繁内封字幕]
            LoliHouse [喵萌Production&LoliHouse] LoveLive! 超级明星!! 2期 / Love Live! Superstar!! S2 - 03 [WebRip 1080p HEVC-10bit AAC][简繁日内封字幕]
    """.trimIndent().lines()

    @TestFactory
    fun testParserA(): List<DynamicTest> {
        val parser = PatternBasedRawTitleParser()
        return dataA.map { data ->
            val allianceName = data.substringBefore(' ').trim()
            DynamicTest.dynamicTest(allianceName) {
                val builder = ParsedTopicTitle.Builder()
                parser.parse(
                    data.substringAfter(' ').trim(),
                    allianceName,
                    builder,
                )
                builder.build().run {
                    // TODO: 2022/8/4 test TopicDetails
                    println(this)
//                    assertEquals("", chineseTitle)
                }
            }
        }
    }
}