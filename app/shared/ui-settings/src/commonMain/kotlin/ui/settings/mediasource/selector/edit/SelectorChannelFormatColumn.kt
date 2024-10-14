/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.him188.ani.app.domain.mediasource.web.format.SelectorChannelFormat
import me.him188.ani.app.domain.mediasource.web.format.SelectorChannelFormatIndexGrouped
import me.him188.ani.app.domain.mediasource.web.format.SelectorChannelFormatNoChannel
import me.him188.ani.app.domain.mediasource.web.format.SelectorFormatId
import me.him188.ani.app.ui.foundation.effects.moveFocusOnEnter
import me.him188.ani.app.ui.foundation.layout.cardVerticalPadding
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.settings.mediasource.MediaSourceConfigurationDefaults

@Composable
internal fun SelectorChannelFormatColumn(
    formatId: SelectorFormatId,
    state: SelectorConfigState,
    modifier: Modifier = Modifier,
    textFieldShape: Shape = MediaSourceConfigurationDefaults.outlinedTextFieldShape,
) {
    Column(modifier) {
        when (SelectorChannelFormat.findById(formatId)) {
            SelectorChannelFormatIndexGrouped -> Column(
                verticalArrangement = Arrangement.spacedBy(currentWindowAdaptiveInfo().windowSizeClass.cardVerticalPadding),
            ) {
                Text(
                    "先提取线路名称列表，再提取剧集面板列表，按顺序对应后，再分别从每个剧集面板中提取剧集",
                    Modifier,
                    style = MaterialTheme.typography.labelLarge,
                )

                val conf = state.channelFormatIndexed
                OutlinedTextField(
                    conf.selectChannelNames, { conf.selectChannelNames = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("从页面中提取线路名称列表") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                    supportingText = { Text("CSS Selector 表达式。期望返回一些任意类型元素，每个对应一个线路，将会读取其 text 作为线路名称") },
                    isError = conf.selectChannelNamesIsError,
                    enabled = state.enableEdit,
                )
                OutlinedTextField(
                    conf.matchChannelName, { conf.matchChannelName = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("匹配线路名称 (可选)") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                    supportingText = { Text("正则表达式。从上面提取到的元素 text 中，匹配线路名称。期望名为 ch 的分组，留空则使用整个 text。") },
                    isError = conf.matchChannelNameIsError,
                    enabled = state.enableEdit,
                )

                OutlinedTextField(
                    conf.selectEpisodeLists, { conf.selectEpisodeLists = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter().padding(top = 8.dp),
                    label = { Text("从页面中提取剧集面板列表") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                    supportingText = { Text("CSS Selector 表达式。期望返回一些 <div>，每个对应一个剧集面板。剧集面板内通常包含 1-12 集按钮") },
                    isError = conf.selectEpisodeListsIsError,
                    enabled = state.enableEdit,
                )

                OutlinedTextField(
                    conf.selectEpisodesFromList, { conf.selectEpisodesFromList = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter().padding(top = 8.dp),
                    label = { Text("从每个剧集面板中提取剧集列表") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                    supportingText = {
                        Text(
                            """
                                CSS Selector 表达式。期望返回一些元素，将会提取每个元素的 text 作为剧集名称。
                                如果元素是 <a>，则还会默认读取 href 作为剧集的链接。也可以在下面的设置中用其他方式提取链接。
                                如果元素不为 <a>，即名称和链接不在相同元素的情况，则需配置下面的设置来提取链接
                            """.trimIndent(),
                        )
                    },
                    isError = conf.selectEpisodesFromListIsError,
                    enabled = state.enableEdit,
                )
                OutlinedTextField(
                    conf.selectEpisodeLinksFromList, { conf.selectEpisodeLinksFromList = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter().padding(top = 8.dp),
                    label = { Text("从剧集面板中提取剧集链接列表 (可选)") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                    supportingText = { Text("可选的 CSS Selector 表达式。如果上一个设置中提取到的剧集元素不为 <a>，则需要配置此项提取链接") },
                    enabled = state.enableEdit,
                )
                OutlinedTextField(
                    conf.matchEpisodeSortFromName, { conf.matchEpisodeSortFromName = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("从剧集名称中匹配序号") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                    supportingText = { Text("正则表达式查找。期望名为 ep 的分组，数字为佳") },
                    isError = conf.matchEpisodeSortFromNameIsError,
                    enabled = state.enableEdit,
                )
            }

            SelectorChannelFormatNoChannel -> Column(
                verticalArrangement = Arrangement.spacedBy(currentWindowAdaptiveInfo().windowSizeClass.cardVerticalPadding),
            ) {
                val conf = state.channelFormatNoChannel
                OutlinedTextField(
                    conf.selectEpisodes, { conf.selectEpisodes = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("提取剧集列表") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                    supportingText = {
                        Text(
                            "CSS Selector 表达式。期望返回一些元素，将会提取每个元素的 text 作为剧集名称。" +
                                    "如果元素是 <a>，则还会默认读取 href 作为剧集的链接。也可以在下面的设置中用其他方式提取链接。" +
                                    "如果元素不为 <a>，即名称和链接不在相同元素的情况，则需配置下面的设置来提取链接",
                        )
                    },
                    isError = conf.selectEpisodesIsError,
                    enabled = state.enableEdit,
                )
                OutlinedTextField(
                    conf.selectEpisodeLinks, { conf.selectEpisodeLinks = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("提取剧集链接列表 (可选)") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                    supportingText = { Text("可选的 CSS Selector 表达式。如果上一个设置中提取到的剧集元素不为 <a>，则需要配置此项提取链接") },
                    enabled = state.enableEdit,
                )
                OutlinedTextField(
                    conf.matchEpisodeSortFromName, { conf.matchEpisodeSortFromName = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("从剧集名称中匹配序号") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                    supportingText = { Text("正则表达式查找。期望名为 ep 的分组，数字为佳") },
                    isError = conf.matchEpisodeSortFromNameIsError,
                    enabled = state.enableEdit,
                )
            }

            null -> {
                UnsupportedFormatIdHint(formatId, Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
internal fun UnsupportedFormatIdHint(formatId: SelectorFormatId, modifier: Modifier = Modifier) {
    Column(modifier) {
        ProvideTextStyleContentColor(MaterialTheme.typography.bodyLarge, MaterialTheme.colorScheme.error) {
            Icon(
                Icons.Rounded.Error, null,
                Modifier.align(Alignment.CenterHorizontally).size(48.dp),
            )
            Text(
                "当前版本不支持该配置类型：${formatId.value}\n\n这可能是导入了一个在更高版本编辑的配置导致的\n可升级 Ani 或切换到其他配置类型",
                Modifier.padding(top = 24.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

