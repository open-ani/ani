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
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormat
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormatFlattened
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormatNoChannel
import me.him188.ani.app.data.source.media.source.web.format.SelectorFormatId
import me.him188.ani.app.ui.foundation.effects.moveFocusOnEnter
import me.him188.ani.app.ui.foundation.layout.cardVerticalPadding
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.settings.mediasource.MediaSourceConfigurationDefaults

@Composable
internal fun SelectorChannelConfigurationColumn(
    formatId: SelectorFormatId,
    state: SelectorConfigurationState,
    modifier: Modifier = Modifier,
    textFieldShape: Shape = MediaSourceConfigurationDefaults.outlinedTextFieldShape,
) {
    Column(modifier) {
        when (SelectorChannelFormat.findById(formatId)) {
            SelectorChannelFormatFlattened -> Column(
                verticalArrangement = Arrangement.spacedBy(currentWindowAdaptiveInfo().windowSizeClass.cardVerticalPadding),
            ) {
                val conf = state.channelFormatIndexed
                OutlinedTextField(
                    conf.selectChannels, { conf.selectChannels = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("提取线路名称列表") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                    supportingText = { Text("CSS Selector 表达式。将会读取结果的 text") },
                    isError = conf.selectChannelsIsError,
                )
                OutlinedTextField(
                    conf.selectLists, { conf.selectLists = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("提取所有线路的剧集列表") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                    supportingText = { Text("CSS Selector 表达式。期望返回一些 <a>，每个对应一个剧集，将会读取其 href 属性和 text") },
                    isError = conf.selectListsIsError,
                )
                OutlinedTextField(
                    conf.matchEpisodeSortFromName, { conf.matchEpisodeSortFromName = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("从剧集名称中匹配序号") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                    supportingText = { Text("正则表达式查找。期望名为 ep 的分组") },
                    isError = conf.matchEpisodeSortFromNameIsError,
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
                    supportingText = { Text("CSS Selector 表达式。期望返回一些 <a>，每个对应一个剧集，将会读取其 href 属性和 text") },
                    isError = conf.selectEpisodesIsError,
                )
                OutlinedTextField(
                    conf.matchEpisodeSortFromName, { conf.matchEpisodeSortFromName = it },
                    Modifier.fillMaxWidth().moveFocusOnEnter(),
                    label = { Text("从剧集名称中匹配序号") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = textFieldShape,
                    supportingText = { Text("正则表达式查找。期望名为 ep 的分组") },
                    isError = conf.matchEpisodeSortFromNameIsError,
                )
            }

            null -> {
                Column(
                    Modifier.align(Alignment.CenterHorizontally),
                ) {
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
        }
    }
}

