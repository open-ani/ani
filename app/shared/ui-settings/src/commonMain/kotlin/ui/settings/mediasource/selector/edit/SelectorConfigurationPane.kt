/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormatFlattened
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormatNoChannel
import me.him188.ani.app.data.source.media.source.web.format.SelectorFormatId
import me.him188.ani.app.ui.foundation.animation.StandardEasing
import me.him188.ani.app.ui.foundation.effects.moveFocusOnEnter
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.foundation.theme.EasingDurations
import me.him188.ani.app.ui.settings.mediasource.rss.edit.MediaSourceHeadline

@Composable
internal fun SelectorConfigurationPane(
    state: SelectorConfigState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalSpacing: Dp = SelectorConfigurationDefaults.verticalSpacing,
    textFieldShape: Shape = SelectorConfigurationDefaults.textFieldShape,
) {
    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
        // 大图标和标题
        MediaSourceHeadline(state.iconUrl, state.displayName)

        Column(
            Modifier
                .fillMaxHeight()
                .padding(vertical = 16.dp),
        ) {
            val listItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)

            Column(verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
                OutlinedTextField(
                    state.displayName, { state.displayName = it },
                    Modifier
                        .fillMaxWidth()
                        .moveFocusOnEnter(),
                    label = { Text("名称*") },
                    placeholder = { Text("设置显示在列表中的名称") },
                    isError = state.displayNameIsError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                )
                OutlinedTextField(
                    state.iconUrl, { state.iconUrl = it },
                    Modifier
                        .fillMaxWidth()
                        .moveFocusOnEnter(),
                    label = { Text("图标链接") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                )
            }

            Row(Modifier.padding(top = verticalSpacing, bottom = 12.dp)) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.titleMedium,
                    MaterialTheme.colorScheme.primary,
                ) {
                    Text(SelectorConfigurationDefaults.STEP_NAME_1)
                }
            }

            Column {
                OutlinedTextField(
                    state.searchUrl, { state.searchUrl = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("搜索链接*") },
                    placeholder = {
                        Text(
                            "示例：https://www.nyacg.net/search.html?wd={keyword}",
                            color = MaterialTheme.colorScheme.outline,
                        )
                    },
                    supportingText = {
                        Text(
                            """
                                    替换规则：
                                    {keyword} 替换为条目 (番剧) 名称
                                """.trimIndent(),
                        )
                    },
                    isError = state.searchUrlIsError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                )
                ListItem(
                    headlineContent = { Text("仅使用第一个词") },
                    Modifier
                        .padding(top = (verticalSpacing - 8.dp).coerceAtLeast(0.dp))
                        .clickable { state.searchUseOnlyFirstWord = !state.searchUseOnlyFirstWord },
                    supportingContent = { Text("以空格分割，仅使用第一个词搜索。适用于搜索兼容性差的情况") },
                    trailingContent = {
                        Switch(state.searchUseOnlyFirstWord, { state.searchUseOnlyFirstWord = it })
                    },
                    colors = listItemColors,
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

            Row(Modifier.padding(top = verticalSpacing, bottom = 12.dp)) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.titleMedium,
                    MaterialTheme.colorScheme.primary,
                ) {
                    Text(SelectorConfigurationDefaults.STEP_NAME_2)
                }
            }

            SelectorChannelSelectionButtonRow(
                state,
                Modifier.fillMaxWidth().padding(bottom = 4.dp),
            )

            AnimatedContent(
                state.channelFormatId,
                Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
                    .animateContentSize(tween(EasingDurations.standard, easing = StandardEasing)),
                transitionSpec = AniThemeDefaults.standardAnimatedContentTransition,
            ) { formatId ->
                SelectorChannelConfigurationColumn(formatId, state, Modifier.fillMaxWidth())
            }

            Row(Modifier.padding(top = verticalSpacing, bottom = 12.dp)) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.titleMedium,
                    MaterialTheme.colorScheme.primary,
                ) {
                    Text("过滤设置")
                }
            }

            Column(
                Modifier,
                verticalArrangement = Arrangement.spacedBy((verticalSpacing - 16.dp).coerceAtLeast(0.dp)),
            ) {
                ListItem(
                    headlineContent = { Text("使用条目名称过滤") },
                    Modifier.focusable(false).clickable { state.filterBySubjectName = !state.filterBySubjectName },
                    supportingContent = { Text("要求资源标题包含条目名称。适用于数据源可能搜到无关内容的情况。通常建议开启") },
                    trailingContent = { Switch(state.filterBySubjectName, { state.filterBySubjectName = it }) },
                    colors = listItemColors,
                )
                ListItem(
                    headlineContent = { Text("使用剧集序号过滤") },
                    Modifier.focusable(false).clickable { state.filterByEpisodeSort = !state.filterByEpisodeSort },
                    supportingContent = { Text("要求资源标题包含剧集序号。适用于数据源可能搜到无关内容的情况。通常建议开启") },
                    trailingContent = { Switch(state.filterByEpisodeSort, { state.filterByEpisodeSort = it }) },
                    colors = listItemColors,
                )
            }

            Row(Modifier.padding(top = verticalSpacing, bottom = 12.dp)) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.titleMedium,
                    MaterialTheme.colorScheme.primary,
                ) {
                    Text(SelectorConfigurationDefaults.STEP_NAME_3)
                }
            }

            SelectorConfigurationDefaults.MatchVideoSection(
                state,
                textFieldShape = textFieldShape,
                verticalSpacing = verticalSpacing,
            )

            Row(Modifier.padding(top = verticalSpacing, bottom = 12.dp)) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.titleMedium,
                    MaterialTheme.colorScheme.primary,
                ) {
                    Text("播放视频时")
                }
            }

            Column(Modifier, verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
                val conf = state.matchVideoConfig.videoHeaders
                OutlinedTextField(
                    conf.referer, { conf.referer = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("Referer") },
                    supportingText = { Text("播放视频时执行的 HTTP 请求的 Referer，可留空") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                )
                OutlinedTextField(
                    conf.userAgent, { conf.userAgent = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("User-Agent") },
                    supportingText = { Text("播放视频时执行的 HTTP 请求的 User-Agent") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                )
            }

            Row(Modifier.align(Alignment.End).padding(top = verticalSpacing, bottom = 12.dp)) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.labelMedium,
                    MaterialTheme.colorScheme.outline,
                ) {
                    Text("提示：修改自动保存")
                }
            }
        }

    }
}

@Composable
private fun SelectorChannelSelectionButtonRow(
    state: SelectorConfigState,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier) {
        @Composable
        fun Btn(
            id: SelectorFormatId, index: Int,
            enabled: Boolean = true,
            label: @Composable () -> Unit,
        ) {
            SegmentedButton(
                state.channelFormatId == id,
                { state.channelFormatId = id },
                SegmentedButtonDefaults.itemShape(index, state.allChannelFormats.size),
                icon = { SegmentedButtonDefaults.Icon(state.channelFormatId == id) },
                label = label,
                enabled = enabled,
            )
        }

        for ((index, selectorChannelFormat) in state.allChannelFormats.withIndex()) {
            Btn(
                selectorChannelFormat.id, index,
                enabled = selectorChannelFormat == SelectorChannelFormatNoChannel,
            ) {
                Text(
                    when (selectorChannelFormat) { // type-safe to handle all formats
                        SelectorChannelFormatNoChannel -> "不区分线路"
                        SelectorChannelFormatFlattened -> "(其他暂未支持)"
                    },
                    softWrap = false,
                )
            }
        }
    }
}
