package me.him188.animationgarden.api.impl.tags

import me.him188.animationgarden.api.impl.AbstractTest
import me.him188.animationgarden.api.model.TopicDetails
import me.him188.animationgarden.api.tags.RawTitleParserImpl
import me.him188.animationgarden.api.tags.parse
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

internal class RawTitleParserTest : AbstractTest() {


    val data = mapOf(
        "Lilith-Raws [Lilith-Raws] Overlord IV - 05 [Baha][WEB-DL][1080p][AVC AAC][CHT][MP4]" to TopicDetails(
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
        val parser = RawTitleParserImpl()
        return dataA.map { data ->
            val allianceName = data.substringBefore(' ').trim()
            DynamicTest.dynamicTest(allianceName) {
                val builder = TopicDetails.Builder()
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