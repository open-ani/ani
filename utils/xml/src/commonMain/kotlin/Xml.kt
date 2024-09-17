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

// JVM 直接用 Jsoup, Native 用 Ksoup. 因为 Ksoup 性能远低于 Jsoup. 如果未来 Ksoup 能解决性能问题, 我们可以考虑更换.
expect object Xml {
    fun parse(string: String): Document
    fun parse(string: String, baseUrl: String): Document
    fun parse(source: Source): Document
    fun parse(source: Source, baseUrl: String): Document
}

expect object QueryParser {
    @Throws(IllegalStateException::class)
    fun parseSelector(selector: String): Evaluator
}

fun QueryParser.parseSelectorOrNull(selector: String): Evaluator? {
    if (selector.isBlank()) return null
    return try {
        parseSelector(selector)
    } catch (e: IllegalStateException) {
        null
    }
}
