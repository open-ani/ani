package me.him188.animationgarden.api.impl.impl.protocol

import me.him188.animationgarden.api.impl.AbstractTest
import me.him188.animationgarden.api.impl.getResourceAsString
import me.him188.animationgarden.api.impl.model.CacheImpl
import me.him188.animationgarden.api.impl.protocol.ListParser
import org.jsoup.Jsoup
import kotlin.test.Test

internal class ParseListTest : AbstractTest() {

    @Test
    fun canParseList() {
        val context = CacheImpl()
        val result = ListParser.parseList(
            context,
            Jsoup.parse(getResourceAsString("list.html"))
        )
        println(context)
        for (topic in result) {
            println(topic)
        }
    }
}