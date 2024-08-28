package me.him188.ani.utils.xml

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parser.Parser
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
