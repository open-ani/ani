/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
abstract class AbstractMediaSourceTestState {
    var searchKeywordPlaceholder = SampleKeywords.random()
    var searchKeyword: String by mutableStateOf(searchKeywordPlaceholder)

    var sort: String by mutableStateOf("1")

    fun randomKeyword() {
        val newRandom = SampleKeywords.random()
        searchKeywordPlaceholder = newRandom
        searchKeyword = newRandom
    }
}

@Stable
private val SampleKeywords
    get() = listOf(
        "败犬女主太多了！",
        "白箱",
        "CLANNAD",
        "轻音少女",
        "战姬绝唱",
        "凉宫春日的忧郁",
        "樱 Trick",
        "命运石之门",
    )
