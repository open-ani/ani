package me.him188.ani.app.tools

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * @see formatDateTime
 */
// TimeFormatterTest
class TimeFormatter(
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault()),
    private val getTimeNow: () -> Long = { System.currentTimeMillis() },
) {
    fun format(timestamp: Long): String {
        val now = getTimeNow()
        val differenceInSeconds = ChronoUnit.SECONDS.between(Instant.ofEpochMilli(timestamp), Instant.ofEpochMilli(now))

        // written by ChatGPT
        return when (differenceInSeconds) {
            in 0..1L -> "刚刚"
            in 0..59 -> "$differenceInSeconds 秒前"
            in -60..0 -> "${-differenceInSeconds} 秒后"
            in 60..<3600 -> "${differenceInSeconds / 60} 分钟前"
            in -3600..-60 -> "${-differenceInSeconds / 60} 分钟后"
            in 3600..<86400 -> "${differenceInSeconds / 3600} 小时前"
            in -86400..<-3600 -> "${-differenceInSeconds / 3600} 小时后"
            in 86400..<86400 * 2 -> "${differenceInSeconds / 86400} 天前"
            in -86400 * 2..<-86400 -> "${differenceInSeconds / 86400} 天后"
            else -> formatter.format(Instant.ofEpochMilli(timestamp))
        }
    }
}