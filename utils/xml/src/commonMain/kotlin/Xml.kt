package me.him188.ani.utils.xml

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.ported.BufferReader
import kotlinx.io.Source

//expect object Xml {
//    fun parse(string: String, baseUrl: String): Document
//    fun parse(source: Source, baseUrl: String): Document
//}

@Deprecated(
    "For migration. Use Ksoup instead",
    ReplaceWith("Ksoup", imports = ["com.fleeksoft.ksoup.Ksoupl"]),
    level = DeprecationLevel.ERROR,
)
typealias Jsoup = Ksoup

fun Ksoup.parse(source: Source, encoding: String, baseUrl: String): com.fleeksoft.ksoup.nodes.Document {
    return parse(BufferReader(source), encoding, baseUrl)
}

//typealias Document = com.fleeksoft.ksoup.nodes.Document