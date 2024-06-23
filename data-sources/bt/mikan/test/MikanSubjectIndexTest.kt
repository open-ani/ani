package me.him188.ani.datasources.mikan

import me.him188.ani.datasources.api.topic.TopicCriteria
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MikanSubjectIndexTest {

    @Test
    fun `can parse subject index`() {
        val ids = AbstractMikanMediaSource.parseMikanSubjectIdsFromSearch(
            Jsoup.parse(
                this::class.java.getResource("/mikan-search-无职转生.txt")!!.readText(),
            ),
        )
        assertEquals(listOf(3060, 2353, 2549, 3344).map { it.toString() }, ids)
    }

    @Test
    fun `can parse bangumi subject id`() {
        val id = AbstractMikanMediaSource.parseBangumiSubjectIdFromMikanSubjectDetails(
            Jsoup.parse(
                this::class.java.getResource("/mikan-bangumi-无职转生.txt")!!.readText(),
            ),
        )
        assertEquals("373247", id)
    }

    @Test
    fun `can parse subject rss`() {
        val list = AbstractMikanMediaSource.parseRssTopicList(
            Jsoup.parse(
                this::class.java.getResource("/mikan-subject-rss-无职转生.txt")!!.readText(),
            ),
            TopicCriteria.ANY,
            allowEpMatch = false, // doesn't matter
        )
        assertEquals(318, list.size)
        assertTrue {
            list.all { it.originalLink.isNotBlank() }
        }
        assertTrue {
            list.all { it.topicId.isNotBlank() }
        }
        assertTrue {
            list.all { it.downloadLink.uri.isNotBlank() }
        }
    }
}