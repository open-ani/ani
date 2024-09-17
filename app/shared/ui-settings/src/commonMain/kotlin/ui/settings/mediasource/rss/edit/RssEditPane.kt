/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.rss.edit

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.settings.SettingsViewModel
import me.him188.ani.app.ui.settings.mediasource.MediaSourceConfigurationDefaults
import me.him188.ani.app.ui.settings.mediasource.rss.EditRssMediaSourceState

@Composable
fun RssEditPane(
    state: EditRssMediaSourceState,
    onClickTest: () -> Unit,
    showTestButton: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    showDebugFields: Boolean = viewModel<SettingsViewModel> { SettingsViewModel() }.isInDebugMode,
    outlinedTextFieldShape: Shape = MediaSourceConfigurationDefaults.outlinedTextFieldShape
) {
    Column(
        modifier
            .padding(contentPadding),
    ) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
        ) {
            // 大图标和标题
            MediaSourceHeadline(state.displayIconUrl, state.displayName)

            Column(
                Modifier.focusGroup()
                    .fillMaxHeight()
                    .padding(vertical = 16.dp),
            ) {
                val listItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)

                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    if (showDebugFields) {
                        OutlinedTextField(
                            state.instanceId, { },
                            Modifier
                                .fillMaxWidth(),
                            label = { Text("[debug] instanceId") },
                            placeholder = { Text("设置显示在列表中的名称") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            readOnly = true,
                            shape = outlinedTextFieldShape,
                        )
                    }

                    OutlinedTextField(
                        state.displayName, { state.displayName = it },
                        Modifier
                            .fillMaxWidth(),
                        label = { Text("名称*") },
                        placeholder = { Text("设置显示在列表中的名称") },
                        isError = state.displayNameIsError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = outlinedTextFieldShape,
                    )
                    OutlinedTextField(
                        state.iconUrl, { state.iconUrl = it.trim() },
                        Modifier
                            .fillMaxWidth(),
                        label = { Text("图标链接") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = outlinedTextFieldShape,
                    )
                }

                Row(Modifier.padding(top = 20.dp, bottom = 12.dp)) {
                    ProvideTextStyleContentColor(
                        MaterialTheme.typography.titleMedium,
                        MaterialTheme.colorScheme.primary,
                    ) {
                        Text("查询设置")
                    }
                }

                Column(Modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    OutlinedTextField(
                        state.searchUrl, { state.searchUrl = it },
                        Modifier.fillMaxWidth(),
                        label = { Text("搜索链接*") },
                        placeholder = {
                            Text(
                                "示例：https://acg.rip/page/{page}.xml?term={keyword}",
                                color = MaterialTheme.colorScheme.outline,
                            )
                        },
                        supportingText = {
                            Text(
                                """
                                    替换规则：
                                    {keyword} 替换为条目 (番剧) 名称
                                    {page} 替换为页码, 如果不需要分页则忽略
                                """.trimIndent(),
                            )
                        },
                        isError = state.searchUrlIsError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = outlinedTextFieldShape,
                    )

                    ListItem(
                        headlineContent = { Text("使用剧集序号过滤") },
                        supportingContent = { Text("要求资源标题包含剧集序号。适用于数据源可能搜到无关内容的情况") },
                        trailingContent = { Switch(state.filterByEpisodeSort, { state.filterByEpisodeSort = it }) },
                        colors = listItemColors,
                    )

                    ListItem(
                        headlineContent = { Text("使用条目名称过滤") },
                        supportingContent = { Text("要求资源标题包含条目名称。适用于数据源可能搜到无关内容的情况") },
                        trailingContent = { Switch(state.filterBySubjectName, { state.filterBySubjectName = it }) },
                        colors = listItemColors,
                    )
                }

                Row(Modifier.align(Alignment.End).padding(top = 20.dp, bottom = 12.dp)) {
                    ProvideTextStyleContentColor(
                        MaterialTheme.typography.labelMedium,
                        MaterialTheme.colorScheme.outline,
                    ) {
                        Text("提示：修改自动保存")
                    }
                }
            }
        }

        if (showTestButton) {
            FilledTonalButton(
                onClick = onClickTest,
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text("测试")
            }
        }
    }
}
