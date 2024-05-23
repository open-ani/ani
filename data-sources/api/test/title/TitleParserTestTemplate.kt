package me.him188.ani.datasources.api.title

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 基于数据的测试
 */
class TitleParserTestTemplate : PatternBasedTitleParserTestSuite() {
    @Test
    fun `樱Trick #13`() {
        parse("""""").run {
            assertEquals(null, episodeRange)
        }
    }
}