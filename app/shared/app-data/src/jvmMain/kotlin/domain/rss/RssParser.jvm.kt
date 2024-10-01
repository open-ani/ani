/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.rss

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
