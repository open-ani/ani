package me.him188.ani.datasources.api

import me.him188.ani.datasources.api.topic.EpisodeRange
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EpRangeTest {
    @Test
    fun testEmpty() {
        val empty = EpisodeRange.empty()
        assert(empty.sorts.toList().isEmpty())
    }

    @Test
    fun testSingle() {
        val single = EpisodeRange.single("1")
        assertEquals(1, single.sorts.toList().size)
    }

    @Test
    fun testRange() {
        val range = EpisodeRange.range("1", "3")
        assertEquals(3, range.sorts.toList().size)
        assertEquals("[01, 02, 03]", range.sorts.toList().toString())
    }

    @Test
    fun `partial range end`() {
        val range = EpisodeRange.range("1", "2.5")
        assertEquals(3, range.sorts.toList().size)
        assertEquals("[01, 02, 2.5]", range.sorts.toList().toString())
    }

    @Test
    fun `partial range start`() {
        val range = EpisodeRange.range("1.5", "3")
        assertEquals(3, range.sorts.toList().size)
        assertEquals("[1.5, 02, 03]", range.sorts.toList().toString())
    }

    @Test
    fun `partial range both`() {
        val range = EpisodeRange.range("1.5", "2.5")
        assertEquals(3, range.sorts.toList().size)
        assertEquals("[1.5, 02, 2.5]", range.sorts.toList().toString())
    }

    @Test
    fun `normal and special`() {
        val range = EpisodeRange.range(EpisodeSort("SP"), EpisodeSort("2.5"))
        assertEquals(2, range.sorts.toList().size)
        assertEquals("[SP, 2.5]", range.sorts.toList().toString())
    }

    @Test
    fun `special and special`() {
        val range = EpisodeRange.range(EpisodeSort("SP"), EpisodeSort("OP"))
        assertEquals(2, range.sorts.toList().size)
        assertEquals("[SP, OP]", range.sorts.toList().toString())
    }
}