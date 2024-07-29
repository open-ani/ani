package me.him188.ani.utils.xml

import kotlinx.io.Source

// JVM 直接用 Jsoup, Native 用 Ksoup. 因为 Ksoup 性能远低于 Jsoup. 如果未来 Ksoup 能解决性能问题, 我们可以考虑更换.
expect object Xml {
    fun parse(string: String): Document
    fun parse(string: String, baseUrl: String): Document
    fun parse(source: Source): Document
    fun parse(source: Source, baseUrl: String): Document
}
