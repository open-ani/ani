package me.him188.ani.utils.xml

import kotlinx.io.Source
import kotlinx.io.asInputStream
import org.jsoup.Jsoup

actual object Xml {
    actual fun parse(string: String, baseUrl: String): org.jsoup.nodes.Document = Jsoup.parse(string, baseUrl)

    actual fun parse(source: Source, baseUrl: String): Document =
        Jsoup.parse(source.asInputStream(), "UTF-8", baseUrl)

    actual fun parse(string: String): Document {
        return Jsoup.parse(string)
    }

    actual fun parse(source: Source): Document {
        return Jsoup.parse(source.asInputStream(), "UTF-8", "")
    }
}
