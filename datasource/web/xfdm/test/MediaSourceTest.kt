import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.ntdm.XfdmMediaSource
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private typealias Source = XfdmMediaSource

class MediaSourceTest {
    @Test
    fun `parse search result`() {
        val source = Source(MediaSourceConfig())
        val doc = Jsoup.parse(
            this::class.java.classLoader.getResource("tensurai/search.html")!!.readText(),
            source.baseUrl,
        )
        val list = source.parseBangumiSearch(doc)
        assertEquals(6, list.size)
        list[0].run {
            assertEquals("2161", internalId)
            assertEquals("关于我转生变成史莱姆这档事 第三季", name)
            assertEquals("https://dm1.xfdm.pro/bangumi/2161.html", url)
        }
    }

    @Test
    fun `parse bangumi result`() {
        val source = Source(MediaSourceConfig())
        val doc = Jsoup.parse(
            this::class.java.classLoader.getResource("tensurai/bangumi.html")!!.readText(),
            source.baseUrl,
        )
        val list = source.parseEpisodeList(doc)
        assertEquals(57, list.size)
        list[0].run {
            assertEquals("第01集", name)
            assertEquals("https://dm1.xfdm.pro/watch/2161/1/1.html", url)
            assertEquals("稀饭新番主线-", channel)
        }
        list[11].run {
            assertEquals("第12集", name)
            assertEquals("https://dm1.xfdm.pro/watch/2161/1/12.html", url)
            assertEquals("稀饭新番主线-", channel)
        }
    }
}