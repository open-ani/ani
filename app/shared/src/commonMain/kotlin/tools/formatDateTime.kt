package me.him188.ani.app.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.datetime.Instant


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
