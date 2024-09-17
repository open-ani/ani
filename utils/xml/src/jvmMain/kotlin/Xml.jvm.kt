/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.utils.xml

import kotlinx.io.Source
import kotlinx.io.asInputStream
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

actual object Xml {
    actual fun parse(string: String, baseUrl: String): org.jsoup.nodes.Document =
        Jsoup.parse(string, baseUrl, Parser.xmlParser())

    actual fun parse(source: Source, baseUrl: String): Document =
        Jsoup.parse(source.asInputStream(), "UTF-8", baseUrl, Parser.xmlParser())

    actual fun parse(string: String): Document {
        return Jsoup.parse(string, Parser.xmlParser())
    }

    actual fun parse(source: Source): Document {
        return Jsoup.parse(source.asInputStream(), "UTF-8", "", Parser.xmlParser())
    }
}

actual object QueryParser {
    @Throws(IllegalStateException::class)
    actual fun parseSelector(selector: String): Evaluator {
        return org.jsoup.select.QueryParser.parse(selector)
    }
}
