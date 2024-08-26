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
