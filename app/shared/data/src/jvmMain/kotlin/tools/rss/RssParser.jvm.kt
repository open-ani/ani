package me.him188.ani.app.tools.rss

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.RFC_1123_DATE_TIME

@Suppress("FunctionName")
internal actual fun RssParser_parseTime(text: String): LocalDateTime? {
    return runCatching { ZonedDateTime.parse(text, formatter) }.getOrNull()
        ?.toLocalDateTime()?.toKotlinLocalDateTime()
}
