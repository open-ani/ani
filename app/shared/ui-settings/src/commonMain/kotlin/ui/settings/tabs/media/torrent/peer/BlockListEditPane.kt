package me.him188.ani.app.ui.settings.tabs.media.torrent.peer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.comment.CommentEditorTextState
import me.him188.ani.app.ui.foundation.IconButton
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.text.ProvideContentColor
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.currentPlatform

private val IPV4_REGEX =
    Regex("^((25[0-5]|2[0-4][0-9]|[0-1]?[0-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]?[0-9])\$")
private val IPV6_REGEX = Regex("^(?:[\\da-fA-F]{4}:){7}[\\da-fA-F]{4}\$")

private fun validateIp(value: String): Boolean {
    return value
        .split('\n')
        .filter { it.trim().isNotEmpty() }
        .all { IPV4_REGEX.matches(it) || IPV6_REGEX.matches(it) }
}

@Composable
fun BlockListEditPane(
    blockedIpList: List<String>,
    newBlockedIpValue: CommentEditorTextState,
    contentPadding: PaddingValues,
    showTitle: Boolean,
    onAdd: (List<String>) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)

    var showAddBlockedIpDialog by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.padding(contentPadding).fillMaxSize()) {
        if (showTitle) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ProvideTextStyleContentColor(
                    MaterialTheme.typography.titleMedium,
                    MaterialTheme.colorScheme.primary,
                ) {
                    Text("黑名单")
                }
                ProvideTextStyleContentColor(MaterialTheme.typography.bodyMedium) {
                    Text("黑名单中的 Peer 总是被屏蔽，无论是否匹配过滤规则")
                }
            }
        }
        ListItem(
            headlineContent = {
                if (!showTitle) Text("添加黑名单 IP 地址")
            },
            trailingContent = {
                if (showTitle) {
                    OutlinedButton({ showAddBlockedIpDialog = true }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加黑名单 IP 地址")
                            Text("添加黑名单 IP 地址")
                        }
                    }
                } else {
                    IconButton(
                        {
                            showAddBlockedIpDialog = true
                        },
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "添加黑名单 IP 地址")
                    }
                }

            },
            colors = listItemColors,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp),
        ) {
            items(items = blockedIpList, key = { it }) { item ->
                ListItem(
                    headlineContent = { Text(item) },
                    trailingContent = {
                        IconButton({ onRemove(item) }) {
                            Icon(Icons.Default.Close, contentDescription = "移除此黑名单 IP")
                        }
                    },
                    colors = listItemColors,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }

    var isIpValueValid by rememberSaveable { mutableStateOf(true) }
    val dialogAddButtonEnabled by derivedStateOf { newBlockedIpValue.textField.text.isNotEmpty() }

    if (showAddBlockedIpDialog) {
        val focusRequester = remember { FocusRequester() }
        val isDesktop by rememberUpdatedState(currentPlatform() is Platform.Desktop)

        val dismiss = {
            showAddBlockedIpDialog = false
            newBlockedIpValue.override(TextFieldValue(AnnotatedString("")))
            isIpValueValid = true
        }

        val doAdd = {
            if (validateIp(newBlockedIpValue.textField.text)) {
                onAdd(
                    newBlockedIpValue.textField.text
                        .split('\n')
                        .filter { it.trim().isNotEmpty() },
                )
                dismiss()
            } else {
                isIpValueValid = false
            }
        }

        AlertDialog(
            onDismissRequest = dismiss,
            title = { Text("添加 IP 地址") },
            text = {
                Column {
                    Text("向 IP 地址黑名单添加新的 IP 地址")
                    Text("支持 IPv4 或 IPv6 地址，且 IPv6 地址必须为完整格式的地址")
                    Text("可输入多行，按 Enter 确认添加，Ctrl+Enter 换行")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        isError = !isIpValueValid,
                        value = newBlockedIpValue.textField,
                        onValueChange = {
                            newBlockedIpValue.override(it)
                            isIpValueValid = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .ifThen(isDesktop) {
                                onPreviewKeyEvent { event ->
                                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                                    if (event.key == Key.Enter) {
                                        if (event.isCtrlPressed) {
                                            newBlockedIpValue.insertTextAt("\n")
                                            return@onPreviewKeyEvent false
                                        }
                                        doAdd()
                                        return@onPreviewKeyEvent true
                                    }
                                    false
                                }
                            },
                    )
                    if (!isIpValueValid) {
                        Row(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ProvideContentColor(MaterialTheme.colorScheme.error) {
                                Icon(Icons.Default.Close, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("IP 地址格式错误")
                            }
                        }
                    }

                    SideEffect {
                        focusRequester.requestFocus()
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = dialogAddButtonEnabled,
                    onClick = doAdd,
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(dismiss) {
                    Text("取消")
                }
            },
        )
    }
}