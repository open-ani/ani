package me.him188.ani.datasources.api.title

import me.him188.ani.datasources.api.topic.titles.ParsedTopicTitle
import me.him188.ani.datasources.api.topic.titles.RawTitleParser
import me.him188.ani.datasources.api.topic.titles.parse

abstract class PatternBasedTitleParserTestSuite {
    private val parser = RawTitleParser.getDefault()

    fun parse(text: String): ParsedTopicTitle = parser.parse(text, allianceName = null)
}
