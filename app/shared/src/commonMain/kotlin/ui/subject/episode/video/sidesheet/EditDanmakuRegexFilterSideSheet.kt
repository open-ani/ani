package me.him188.ani.app.ui.subject.episode.video.sidesheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.danmaku.DanmakuRegexFilter
import me.him188.ani.app.ui.subject.episode.video.settings.DanmakuRegexFilterState
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsSideSheet
import me.him188.ani.app.ui.subject.episode.video.settings.RegexFilterItem
import me.him188.ani.utils.platform.Uuid


@Composable
fun EditDanmakuRegexFilterSideSheet(
    danmakuRegexFilterState: DanmakuRegexFilterState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,  // Use the expanded parameter
) {
    val focusManager = LocalFocusManager.current
    var regexTextFieldValue by rememberSaveable { mutableStateOf("") }
    val isBlank by remember { derivedStateOf { regexTextFieldValue.isBlank() } }

    fun handleAdd(): Unit {
        if (!isBlank) {
            danmakuRegexFilterState.add(
                DanmakuRegexFilter(
                    id = Uuid.randomString(),
                    name = "",
                    regex = regexTextFieldValue,
                    enabled = true,
                ),
            )
            regexTextFieldValue = "" // Clear the text field after adding
            focusManager.clearFocus()
        }
    }

    EpisodeVideoSettingsSideSheet(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "正则弹幕过滤管理") },
        closeButton = {
            IconButton(onClick = onDismissRequest) {
                Icon(Icons.Rounded.Close, contentDescription = "关闭")
            }
        },
        modifier = modifier
            .clickable(onClick = { focusManager.clearFocus() }),
    ) {
        Surface {
            Column(
                modifier.padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState()),
            ) {
                // 输入框
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        value = regexTextFieldValue,
                        onValueChange = {
                            regexTextFieldValue = it
                        },
                        isError = isBlank,
                        supportingText = {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = if (isBlank) {
                                    "正则表达式不能为空"
                                } else if (expanded) {
                                    "填写用于屏蔽的正则表达式，例如：‘.*签.*’ 会屏蔽所有含有文字‘签’的弹幕。"
                                } else {
                                    "竖屏状态下无法编辑"
                                },
                                color = if (isBlank) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        },
                        label = {
                            Text(
                                text = "正则表达式",
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                            .onKeyEvent { event: KeyEvent ->
                                if (event.key == Key.Enter) {
                                    handleAdd()
                                    true // Consume the event
                                } else {
                                    false // Pass the event to other handlers
                                }
                            },
                        enabled = expanded,  // Disable the text field if expanded is false
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { handleAdd() },
                        ),
                    )

                    // 提交按钮
                    TextButton(
                        onClick = ::handleAdd,
                        enabled = expanded,  // Disable the button if expanded is false
                    ) {
                        Text(text = "添加")
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    danmakuRegexFilterState.list.forEach { item ->
                        RegexFilterItem(
                            item,
                            onDelete = { danmakuRegexFilterState.remove(item) },
                            onDisable = { danmakuRegexFilterState.switch(item) },
                        )
                    }
                }
            }
        }
    }
}
