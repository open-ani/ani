/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.source.media.source.web.format.SelectorSubjectFormat
import me.him188.ani.app.data.source.media.source.web.format.SelectorSubjectFormatA
import me.him188.ani.app.data.source.media.source.web.format.SelectorSubjectFormatIndexed
import me.him188.ani.app.ui.foundation.effects.moveFocusOnEnter

@Composable
internal fun SelectorSubjectConfigurationColumn(
    format: SelectorSubjectFormat<*>?,
    state: SelectorConfigState,
    textFieldShape: Shape,
    verticalSpacing: Dp,
    listItemColors: ListItemColors,
    modifier: Modifier = Modifier,
) {
    when (format) {
        SelectorSubjectFormatA -> Column(modifier) {
            Text(
                "单个表达式，选取一些 <a>，根据其 title 属性或 text 确定名称，href 属性确定链接",
                Modifier,
                style = MaterialTheme.typography.bodyLarge,
            )

            val conf = state.subjectFormatA
            OutlinedTextField(
                conf.selectLists, { conf.selectLists = it },
                Modifier.fillMaxWidth().moveFocusOnEnter().padding(top = verticalSpacing),
                label = { Text("提取条目列表") },
                supportingText = { Text("CSS Selector 表达式。期望返回一些 <a>，每个对应一个条目，将会读取其 href 属性和 text") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = textFieldShape,
                isError = conf.selectListsIsError,
            )
            ListItem(
                headlineContent = { Text("选择最短标题") },
                Modifier
                    .padding(top = (verticalSpacing - 8.dp).coerceAtLeast(0.dp))
                    .clickable { conf.preferShorterName = !conf.preferShorterName },
                supportingContent = { Text("选择满足匹配的标题最短的条目。可避免为第一季匹配到第二季") },
                trailingContent = {
                    Switch(conf.preferShorterName, { conf.preferShorterName = it })
                },
                colors = listItemColors,
            )
        }

        SelectorSubjectFormatIndexed -> Column(modifier) {
            Text(
                "两个表达式，分别选取条目名称列表和链接列表，按顺序一一对应",
                Modifier,
                style = MaterialTheme.typography.bodyLarge,
            )
            val conf = state.subjectFormatIndex
            OutlinedTextField(
                conf.selectNames, { conf.selectNames = it },
                Modifier.fillMaxWidth().moveFocusOnEnter().padding(top = verticalSpacing),
                label = { Text("提取条目列表") },
                supportingText = { Text("CSS Selector 表达式。选取条目名称列表") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = textFieldShape,
                isError = conf.selectNamesIsError,
            )
            OutlinedTextField(
                conf.selectLinks, { conf.selectLinks = it },
                Modifier.fillMaxWidth().moveFocusOnEnter().padding(top = verticalSpacing),
                label = { Text("提取条目列表") },
                supportingText = { Text("CSS Selector 表达式。选取链接列表") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = textFieldShape,
                isError = conf.selectLinksIsError,
            )
            ListItem(
                headlineContent = { Text("选择最短标题") },
                Modifier
                    .padding(top = (verticalSpacing - 8.dp).coerceAtLeast(0.dp))
                    .clickable { conf.preferShorterName = !conf.preferShorterName },
                supportingContent = { Text("选择满足匹配的标题最短的条目。可避免为第一季匹配到第二季") },
                trailingContent = {
                    Switch(conf.preferShorterName, { conf.preferShorterName = it })
                },
                colors = listItemColors,
            )
        }

        null -> Column(modifier) {
            UnsupportedFormatIdHint(
                state.subjectFormatId,
                Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}
