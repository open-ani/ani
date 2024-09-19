/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.source.media.source.web.format

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import me.him188.ani.app.data.source.media.source.web.WebSearchSubjectInfo
import me.him188.ani.utils.xml.Element
import me.him188.ani.utils.xml.QueryParser
import me.him188.ani.utils.xml.parseSelectorOrNull

/**
 * 决定如何匹配条目
 */
sealed class SelectorSubjectFormat<in Config : SelectorFormatConfig>(override val id: SelectorFormatId) :
    SelectorFormat { // 方便改名
    abstract fun select(
        document: Element,
        baseUrl: String,
        config: Config,
    ): List<WebSearchSubjectInfo>

    companion object {
        val entries by lazy { // 必须 lazy, 否则可能获取到 null
            listOf(SelectorSubjectFormatA)
        }

        fun findById(id: SelectorFormatId): SelectorSubjectFormat<*>? {
            // reflection is not supported in Kotlin/Native
            return entries.find { it.id == id }
        }
    }
}

/**
 * Select 出一些 `<a>`, text 作为 name, `href` 作为 url
 */
data object SelectorSubjectFormatA : SelectorSubjectFormat<SelectorSubjectFormatA.Config>(SelectorFormatId("a")) {
    @Immutable
    @Serializable
    data class Config(
        val selectLists: String = "",
    ) : SelectorFormatConfig {
        override fun isValid(): Boolean {
            return selectLists.isNotBlank()
        }
    }

    override fun select(
        document: Element,
        baseUrl: String,
        config: Config,
    ): List<WebSearchSubjectInfo> {
        val selectLists = QueryParser.parseSelectorOrNull(config.selectLists) ?: return emptyList()
        return document.select(selectLists).map { a ->
            val name = a.attr("title").takeIf { it.isNotBlank() } ?: a.text()
            val href = a.attr("href")
            val id = href.substringBeforeLast(".html").substringAfterLast("/")
            WebSearchSubjectInfo(
                internalId = id,
                name = name,
                subjectDetailsPageUrl = SelectorHelpers.computeAbsoluteUrl(baseUrl, href),
                origin = a,
            )
        }
    }
}
