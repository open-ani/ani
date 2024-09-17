/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.utils.xml

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parser.Parser
import com.fleeksoft.ksoup.select.QueryParser
import kotlinx.io.Source
import kotlinx.io.readString

actual object Xml {
    actual fun parse(string: String, baseUrl: String): Document {
        return Ksoup.parse(string, baseUrl, Parser.xmlParser())
    }

    actual fun parse(source: Source, baseUrl: String): Document {
        // TODO: Optimize Xml performance on iOS 
        return Ksoup.parse(source.readString(), baseUri = baseUrl, Parser.xmlParser())
    }

    actual fun parse(string: String): Document {
        return Ksoup.parse(string, Parser.xmlParser())
    }

    actual fun parse(source: Source): Document {
        // TODO: Optimize Xml performance on iOS 
        return Ksoup.parse(source.readString(), baseUri = "", Parser.xmlParser())
    }
}

actual object QueryParser {
    @Throws(IllegalStateException::class)
    actual fun parseSelector(selector: String): Evaluator = QueryParser.parse(selector)
}