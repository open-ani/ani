package me.him188.ani.datasources.nyafun

import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.ResourceLocation
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NyafunMediaSourceTest {
    @Test
    fun `parse search result`() {
        val doc = Jsoup.parse(
            this::class.java.classLoader.getResource("girls band cry/search.html")!!.readText(),
            NyafunMediaSource.BASE_URL
        )
        val list = NyafunMediaSource.parseBangumiSearch(doc)
        assertEquals(1, list.size)
        list[0].run {
            assertEquals("7168", id)
            assertEquals("GIRLS BAND CRY", name)
            assertEquals("https://www.nyafun.net/bangumi/7168.html", url)
        }
    }

    @Test
    fun `parse bangumi result`() {
        val doc = Jsoup.parse(
            this::class.java.classLoader.getResource("girls band cry/bangumi.html")!!.readText(),
            NyafunMediaSource.BASE_URL
        )
        val list = NyafunMediaSource.parseEpisodeList(doc)
        assertEquals(8, list.size)
        list[0].run {
            assertEquals("第01集", name)
            assertEquals("https://www.nyafun.net/play/7168-1-1.html", url)
        }
    }

    @Test
    fun `create media`() {
        assertEquals(
            MediaMatch(
                DefaultMedia(
                    mediaId = "nyafun.7168-01",
                    mediaSourceId = NyafunMediaSource.ID,
                    originalUrl = "https://www.nyafun.net/bangumi/7168.html",
                    download = ResourceLocation.WebVideo(
                        "https://www.nyafun.net/play/7168-1-1.html",
                    ),
                    originalTitle = "GIRLS BAND CRY 第01集",
                    publishedTime = 0L,
                    properties = MediaProperties(
                        listOf("CHS"),
                        resolution = "1080P",
                        NyafunMediaSource.ID,
                        size = FileSize.Unspecified,
                    ),
                    episodeRange = EpisodeRange.single("01"),
                ),
                MatchKind.FUZZY
            ).toString(),
            NyafunMediaSource.createMediaMatch(
                NyafunBangumi(
                    "7168",
                    "GIRLS BAND CRY",
                    "https://www.nyafun.net/bangumi/7168.html"
                ),
                NyafunEp("第01集", "https://www.nyafun.net/play/7168-1-1.html")
            ).toString()
        )
    }
}