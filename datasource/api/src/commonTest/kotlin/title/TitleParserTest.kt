@file:Suppress("TestFunctionName")

package me.him188.ani.datasources.api.title

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 基于数据的测试
 */
class TitleParserTest : PatternBasedTitleParserTestSuite() {
    @Test
    fun `Baha as CHT`() {
        val r = parse("""[Up to 21℃] 怪人的沙拉碗 / Henjin no Salad Bowl - 10 (Baha 1920x1080 AVC AAC MP4)""")
        assertEquals("10..10", r.episodeRange.toString())
        assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
        assertEquals("1080P", r.resolution.toString())
    }

    @Test
    fun S01() {
        val r = parse("""[Up to 21℃] 怪人的沙拉碗 / Henjin no Salad Bowl - S01 (Baha 1920x1080 AVC AAC MP4)""")
        assertEquals("S1", r.episodeRange.toString())
        assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
        assertEquals("1080P", r.resolution.toString())
    }

    @Test
    fun S1() {
        val r = parse("""[Up to 21℃] 怪人的沙拉碗 / Henjin no Salad Bowl - S1 (Baha 1920x1080 AVC AAC MP4)""")
        assertEquals("S1", r.episodeRange.toString())
        assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
        assertEquals("1080P", r.resolution.toString())
    }

    @Test
    fun `S01E05 as ep 05`() {
        val r = parse("""[Up to 21℃] 怪人的沙拉碗 / Henjin no Salad Bowl - S01E05 (Baha 1920x1080 AVC AAC MP4)""")
        assertEquals("05..05", r.episodeRange.toString())
        assertEquals("CHT", r.subtitleLanguages.sortedBy { it.id }.joinToString { it.id })
        assertEquals("1080P", r.resolution.toString())
    }
}