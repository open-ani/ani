package me.him188.ani.app.ui.subject.episode.video.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.danmaku.ui.DanmakuFilterConfig
import me.him188.ani.danmaku.ui.DanmakuRegexFilter
import java.util.UUID
import java.util.regex.PatternSyntaxException

internal fun isValidRegex(pattern: String): Boolean {
    return try {
        Regex(pattern)
        true
    } catch (e: PatternSyntaxException) {
        false
    }
}

@Composable
internal fun SettingsScope.DanmakuRegexFilterGroup(
    danmakuFilterConfig: DanmakuFilterConfig,
    onAdd: (filter: DanmakuRegexFilter) -> Unit,
    onEdit: (id: String, new: DanmakuRegexFilter) -> Unit,
    onRemove: (filter: DanmakuRegexFilter) -> Unit,
    onSwitch: (fiter: DanmakuRegexFilter) -> Unit,
    isLoadingState: Boolean
) {
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    if (showAdd) {
        AddRegexFilterDialog(
            onDismissRequest = { showAdd = false },
            onConfirm = { name, regex ->
                if (regex.isBlank()) {
                    showAdd = false
                } else {
                    if (!isValidRegex(regex)){
                        errorMessage = "正则输入法不正确"
                    } else {
                        onAdd(
                            DanmakuRegexFilter(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                regex = regex,
                            ),
                        )
                        showAdd = false
                    }
                }
            },
            title = { Text("添加正则过滤器") },
            description = {
                Text("请正确添加正则表达式，例如：第一个字符为数字：'^[1-9]{1}\$'.\n匹配该正常表达式的弹幕将不会显示")
            },
            
        )
    }

    if (errorMessage.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { errorMessage = "" },
            confirmButton = {
                TextButton(onClick = { errorMessage = "" }) {
                    Text("确认")
                }
            },
            title = { Text("名字和正则不能为空") },
            text = { Text(errorMessage) }
        )
    }
    
    Group(
        title = { Text("正则过滤器管理") },
        actions = {
            Row {
                IconButton({
                    showAdd = true
                }) {
                    Icon(Icons.Rounded.Add, contentDescription = "添加正则")
                }
            }
        }
    ) {
        FlowRow (
            Modifier.placeholder(isLoadingState).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            danmakuFilterConfig.danmakuRegexFilterList.forEachIndexed { index, item ->
                RegexFilterItem(
                    item,
                    onDelete = { onRemove(item) },
                    onDisable = { onSwitch(item) },
                )
            }
        }
    }
}

private const val DISABLED_ALPHA = 0.38f
@Composable
internal fun RegexFilterItem(
    item: DanmakuRegexFilter,
    isEnabled: Boolean = item.enabled,
    onDisable: () -> Unit,
    onDelete: () -> Unit
) {
    var showConfirmDelete by remember { mutableStateOf(false) }
    ElevatedFilterChip(
        selected = item.enabled,
        onClick = onDisable,
        label = {
            Text(
                item.regex, Modifier.ifThen(!isEnabled) { alpha(DISABLED_ALPHA) },
                maxLines = 1,
                overflow = Ellipsis,
                textAlign = TextAlign.Center,
            )
        },
        trailingIcon = {
            Icon(
                Icons.Rounded.Close,
                contentDescription = null,
                Modifier.clickable(onClick = { showConfirmDelete = true }),
            )
        },
    )

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            icon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("删除正则") },
            text = { Text("确认删除 \"${item.regex}\"？") },
            confirmButton = {
                TextButton({ onDelete(); showConfirmDelete = false }) {
                    Text(
                        "删除",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = { TextButton({ showConfirmDelete = false }) { Text("取消") } },
        )
    }
}


@Composable
fun AddRegexFilterDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (name: String, regex: String) -> Unit, // onConfirm now accepts the text field value
    title: @Composable () -> Unit,
    confirmEnabled: Boolean = true,
    description: @Composable (() -> Unit)? = null
) {
    var nameTextFieldValue by rememberSaveable { mutableStateOf("") }
    var regexTextFieldValue by rememberSaveable { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                        title()
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    OutlinedTextField(
                        value = nameTextFieldValue,
                        onValueChange = { nameTextFieldValue = it },
                        label = { Text("输入正则表达式标题") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    OutlinedTextField(
                        value = regexTextFieldValue,
                        onValueChange = { regexTextFieldValue = it },
                        label = { Text("输入正则表达式") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                description?.let {
                    ProvideTextStyleContentColor(
                        MaterialTheme.typography.labelMedium,
                        LocalContentColor.current.copy(0.8f)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            it()
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().align(Alignment.End),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("取消")
                    }

                    Button(
                        onClick = { onConfirm(nameTextFieldValue, regexTextFieldValue) }, // Pass the text field value to onConfirm
                        enabled = confirmEnabled,
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }
}


