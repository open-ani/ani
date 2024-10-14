/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.rss.test

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import me.him188.ani.app.ui.foundation.OutlinedTag

@Immutable
class MatchTag(
    val value: String,
    /**
     * 该标签表示一个缺失的项目. 例如缺失 EP.
     */
    val isMissing: Boolean = false,
    /**
     * 该标签是否匹配了用户的搜索条件.
     * - `true`: 满足了一个条件. UI 显示为紫色的 check
     * - `false`: 不满足条件. UI 显示为红色的 close
     * - `null`: 这不是一个搜索条件. UI 不会特别高亮此标签.
     */
    val isMatch: Boolean? = null,
)

class MatchTagsBuilder
@PublishedApi
internal constructor() {
    private val list = mutableListOf<MatchTag>()

    fun emit(value: String, isMissing: Boolean = false, isMatch: Boolean? = null) {
        list.add(MatchTag(value, isMissing, isMatch))
    }

    @PublishedApi
    internal fun build(): List<MatchTag> = list
}

inline fun buildMatchTags(builder: MatchTagsBuilder.() -> Unit): List<MatchTag> =
    MatchTagsBuilder().apply(builder).build()

@Composable
fun OutlinedMatchTag(
    tag: MatchTag,
    modifier: Modifier = Modifier
) {
    when {
        tag.isMatch == true -> {
            OutlinedTag(
                modifier.width(IntrinsicSize.Max),
                leadingIcon = { Icon(Icons.Rounded.Check, "符合匹配") },
                contentColor = MaterialTheme.colorScheme.primary,
            ) { Text(tag.value, softWrap = false) }
        }

        tag.isMatch == false -> {
            OutlinedTag(
                modifier.width(IntrinsicSize.Max),
                leadingIcon = { Icon(Icons.Rounded.Close, "不符合匹配") },
                contentColor = MaterialTheme.colorScheme.tertiary,
            ) { Text(tag.value, softWrap = false) }
        }

        tag.isMissing -> {
            OutlinedTag(
                modifier.width(IntrinsicSize.Max),
                leadingIcon = { Icon(Icons.Rounded.QuestionMark, "缺失") },
                contentColor = MaterialTheme.colorScheme.error,
            ) { Text(tag.value, softWrap = false) }
        }

        else -> {
            OutlinedTag(
                modifier.width(IntrinsicSize.Max),
            ) { Text(tag.value, softWrap = false) }
        }
    }
}
