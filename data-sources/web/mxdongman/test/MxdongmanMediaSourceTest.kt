package me.him188.ani.datasources.nyafun

import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.mxdongman.MxdongmanMediaSource
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private typealias Source = MxdongmanMediaSource

class MxdongmanMediaSourceTest {

    @Test
    fun `parse search result`() {
        val source = Source(MediaSourceConfig())
        val doc = Jsoup.parse(
            this::class.java.classLoader.getResource("sakura trick/search.html")!!.readText(),
            source.baseUrl,
        )
        val list = source.parseBangumiSearch(doc)
        assertEquals(1, list.size)
        list[0].run {
            assertEquals("1850", internalId)
            assertEquals("樱Trick", name)
            assertEquals("https://www.mxdm4.com/dongman/1850.html", url)
        }
    }

    @Test
    fun `parse bangumi result`() {
        val source = Source(MediaSourceConfig())
        val doc = Jsoup.parse(
            this::class.java.classLoader.getResource("sakura trick/bangumi.html")!!.readText(),
            source.baseUrl,
        )
        val list = source.parseEpisodeList(doc)
        assertEquals(12, list.size)
        list[0].run {
            assertEquals("第01集", name)
            assertEquals("https://www.mxdm4.com/dongmanplay/1850-1-1.html", url)
        }
    }
}
