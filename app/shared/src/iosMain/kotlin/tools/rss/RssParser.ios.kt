package me.him188.ani.app.tools.rss

import kotlinx.datetime.LocalDateTime


@Suppress("FunctionName")
internal actual fun RssParser_parseTime(text: String): LocalDateTime? {
    return RssParser_parseTimeUsingKtx(text)
}
