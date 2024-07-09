package me.him188.ani.app.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState


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
//    val formatter = remember(format) { DateTimeFormatter.ofPattern(format) }
//    return remember(timestamp) {
//        formatter.format(java.time.Instant.ofEpochMilli(timestamp))
//    }
}


//@Composable
//fun formatDateTime(
//    timestamp: OffsetDateTime, // millis
//    format: String = "yyyy-MM-dd HH:mm:ss",
//): String {
//    val formatter = DateTimeFormatter.ofPattern(format)
//    timestamp.atZoneSameInstant(java.time.ZoneId.systemDefault())
//    return formatter.format(java.time.Instant.ofEpochMilli(timestamp))
//}