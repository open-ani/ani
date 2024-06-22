package me.him188.ani.app.tools

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeFormatterTest {

    private val fixedTime = Instant.parse("2020-01-01T10:00:00Z").toEpochMilli()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))

    private val timeFormatter = TimeFormatter(
        formatterWithTime = formatter,
        getTimeNow = { fixedTime },
    )

    @Test
    fun testJustNow() {
        assertEquals("刚刚", timeFormatter.format(fixedTime))
    }

    @Test
    fun testSecondsAgo() {
        val timestamp = Instant.parse("2020-01-01T09:59:30Z").toEpochMilli()
        assertEquals("30 秒前", timeFormatter.format(timestamp))
    }

    @Test
    fun testSecondsLater() {
        val timestamp = Instant.parse("2020-01-01T10:00:30Z").toEpochMilli()
        assertEquals("30 秒后", timeFormatter.format(timestamp))
    }

    @Test
    fun testMinutesAgo() {
        val timestamp = Instant.parse("2020-01-01T09:58:00Z").toEpochMilli()
        assertEquals("2 分钟前", timeFormatter.format(timestamp))
    }

    @Test
    fun testMinutesLater() {
        val timestamp = Instant.parse("2020-01-01T10:02:00Z").toEpochMilli()
        assertEquals("2 分钟后", timeFormatter.format(timestamp))
    }

    @Test
    fun testHoursAgo() {
        val timestamp = Instant.parse("2020-01-01T08:00:00Z").toEpochMilli()
        assertEquals("2 小时前", timeFormatter.format(timestamp))
    }

    @Test
    fun testHoursLater() {
        val timestamp = Instant.parse("2020-01-01T12:00:00Z").toEpochMilli()
        assertEquals("2 小时后", timeFormatter.format(timestamp))
    }

    @Test
    fun testDaysAgo() {
        val timestamp = Instant.parse("2019-12-31T10:00:00Z").toEpochMilli()
        assertEquals("1 天前", timeFormatter.format(timestamp))
    }

    @Test
    fun testUsingFormatter() {
        val timestamp = Instant.parse("2019-12-30T10:00:00Z").toEpochMilli()
        assertEquals("2019-12-30 10:00:00", timeFormatter.format(timestamp))
    }
}
