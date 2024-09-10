package me.him188.ani.app.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant


/**
 * @see TimeFormatter
 * @see LocalTimeFormatter
 *
 * @param showTime 当距今比较远时, 是否展示时间
 */
@Composable
fun formatDateTime(
    timestamp: Long, // millis
    showTime: Boolean = true,
): String {
    val formatter by rememberUpdatedState(LocalTimeFormatter.current)
    return remember(timestamp, showTime) {
        if (timestamp == 0L) ""
        else formatter.format(timestamp, showTime)
    }
}

@Composable
fun formatDateTime(
    dateTime: LocalDateTime,
    showTime: Boolean = true,
): String {
    val formatter by rememberUpdatedState(LocalTimeFormatter.current)
    return remember(dateTime, showTime) {
        val instant = dateTime.toInstant(TimeZone.currentSystemDefault())
        if (instant.toEpochMilliseconds() == 0L) ""
        else formatter.format(instant, showTime)
    }
}

/**
 * @see WeekFormatter
 */
@Composable
fun formatDateAsWeek(
    timestamp: Long, // millis
    showTime: Boolean = true,
): String {
    return remember(timestamp, showTime) {
        if (timestamp == 0L) ""
        else WeekFormatter.System.format(Instant.fromEpochMilliseconds(timestamp))
    }
}
