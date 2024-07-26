import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.ntdm.GugufanMediaSource
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


private typealias Source = GugufanMediaSource

class GugufanMediaSourceTest {
    @Test
    fun `parse search result`() {
        val source = Source(MediaSourceConfig())
        val doc = Jsoup.parse(
            this::class.java.classLoader.getResource("onimai/search.html")!!.readText(),
            source.baseUrl,
        )
        val list = source.parseBangumiSearch(doc)
        assertEquals(1, list.size)
        list[0].run {
            assertEquals("2391", internalId)
            assertEquals("别当欧尼酱了！", name)
            assertEquals("https://www.gugufan.com/index.php/vod/detail/id/2391.html", url)
        }
    }

    @Test
    fun `parse bangumi result`() {
        val source = Source(MediaSourceConfig())
        val doc = Jsoup.parse(
            this::class.java.classLoader.getResource("onimai/bangumi.html")!!.readText(),
            source.baseUrl,
        )
        val list = source.parseEpisodeList(doc)
        assertEquals(12, list.size)
        list[0].run {
            assertEquals("第01集", name)
            assertEquals("https://www.gugufan.com/index.php/vod/play/id/2391/sid/1/nid/1.html", url)
            assertEquals("咕咕新线", channel)
        }
        list[11].run {
            assertEquals("第12集", name)
            assertEquals("https://www.gugufan.com/index.php/vod/play/id/2391/sid/1/nid/12.html", url)
            assertEquals("咕咕新线", channel)
        }
    }
}