package me.him188.ani.app.ui.subject.episode.video.sidesheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.danmaku.DanmakuRegexFilter
import me.him188.ani.app.ui.subject.episode.TAG_EPISODE_SELECTOR_SHEET
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
    var regexTextFieldOutlineTitleText by rememberSaveable { mutableStateOf("填写用于屏蔽的正则表达式，例如：‘.*签.*’ 会屏蔽所有含有文字‘签’的弹幕。") }
    var isError by rememberSaveable { mutableStateOf(false) }

    // Monitor the expanded state and update the title text accordingly
    LaunchedEffect(expanded) {
        regexTextFieldOutlineTitleText = if (expanded) {
            "填写用于屏蔽的正则表达式，例如：‘.*签.*’ 会屏蔽所有含有文字‘签’的弹幕。"
        } else {
            "竖屏状态下无法编辑"
        }
    }

    fun handleAdd(): Unit {
        if (regexTextFieldValue.isNotBlank()) {
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
            isError = false
        } else {
            isError = true
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
        modifier = modifier.testTag(TAG_EPISODE_SELECTOR_SHEET)
            .clickable(onClick = { focusManager.clearFocus() }),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { focusManager.clearFocus() },
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
                                if (expanded) {
                                    regexTextFieldOutlineTitleText =
                                        "填写用于屏蔽的正则表达式，例如：‘.*签.*’ 会屏蔽所有含有文字‘签’的弹幕。"
                                }
                            },
                            isError = isError,
                            supportingText = {
                                if (isError) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = "正则表达式不能为空",
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = regexTextFieldOutlineTitleText,
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
}
