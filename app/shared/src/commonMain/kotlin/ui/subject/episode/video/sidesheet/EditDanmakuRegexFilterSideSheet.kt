package me.him188.ani.app.ui.subject.episode.video.sidesheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.danmaku.DanmakuRegexFilter
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsSideSheet
import me.him188.ani.app.ui.subject.episode.video.settings.RegexFilterItem
import me.him188.ani.utils.platform.Uuid


@Composable
fun EditDanmakuRegexFilterSideSheet(
    danmakuRegexFilterList: List<DanmakuRegexFilter>,
    onRemove: (DanmakuRegexFilter) -> Unit,
    onSwitch: (DanmakuRegexFilter) -> Unit,
    onAdd: (filter: DanmakuRegexFilter) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EpisodeVideoSettingsSideSheet(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "正则弹幕过滤管理") },
        closeButton = {
            IconButton(onClick = onDismissRequest) {
                Icon(Icons.Rounded.Close, contentDescription = "关闭")
            }
        },
        modifier = modifier,
    ) {
        var regexTextFieldValue by rememberSaveable { mutableStateOf("") }
        var regexTextFieldOutlineTitleText by rememberSaveable { mutableStateOf("填写用于屏蔽的正则表达式，例如：‘.*签.*’ 会屏蔽所有含有文字‘签’的弹幕。") }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // 输入框
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                Column(Modifier.padding(0.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = regexTextFieldValue,
                        onValueChange = {
                            regexTextFieldValue = it
                            regexTextFieldOutlineTitleText =
                                "填写用于屏蔽的正则表达式，例如：‘.*签.*’ 会屏蔽所有含有文字‘签’的弹幕。"
                        },
                        label = {
                            Text(
                                text = regexTextFieldOutlineTitleText,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // 提交按钮
                    TextButton(
                        onClick = {
                            if (regexTextFieldValue.isNotBlank()) {
                                onAdd(
                                    DanmakuRegexFilter(
                                        id = Uuid.randomString(),
                                        name = "",
                                        regex = regexTextFieldValue,
                                        enabled = true,
                                    ),
                                )
                            } else {
                                regexTextFieldOutlineTitleText = "正则输入法不能为空"
                            }
                        },
                    ) {
                        Text(color = MaterialTheme.colorScheme.primary, text = "添加")
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                danmakuRegexFilterList.forEachIndexed { index, item ->
                    RegexFilterItem(
                        item,
                        onDelete = { onRemove(item) },
                        onDisable = { onSwitch(item) },
                    )
                }
            }
        }
    }
}