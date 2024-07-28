package me.him188.ani.utils.xml

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.ported.BufferReader
import kotlinx.io.Source

actual object Xml {
    actual fun parse(string: String, baseUrl: String): Document {
        return Ksoup.parse(string, baseUrl)
    }

    actual fun parse(source: Source, baseUrl: String): Document {
        return Ksoup.parse(BufferReader(source), baseUrl, null)
    }

    actual fun parse(string: String): Document {
        return Ksoup.parse(string)
    }

    actual fun parse(source: Source): Document {
        return Ksoup.parse(BufferReader(source), "", null)
    }
}
