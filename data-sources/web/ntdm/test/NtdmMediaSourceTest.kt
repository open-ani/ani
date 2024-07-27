import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.ntdm.NtdmMediaSource
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


private typealias Source = NtdmMediaSource

class NtdmMediaSourceTest {

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
            assertEquals("4621", internalId)
            assertEquals("别当欧尼酱了！", name)
            assertEquals("https://www.ntdm9.com/video/4621.html", url)
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
        assertEquals(22, list.size)
        list[0].run {
            assertEquals("第01集", name)
            assertEquals("https://www.ntdm9.com/play/4621-1-1.html", url)
            assertEquals("第一线路", channel)
        }
        list[13].run {
            assertEquals("第02集", name)
            assertEquals("https://www.ntdm9.com/play/4621-2-2.html", url)
            assertEquals("备用线路", channel)
        }
    }
}