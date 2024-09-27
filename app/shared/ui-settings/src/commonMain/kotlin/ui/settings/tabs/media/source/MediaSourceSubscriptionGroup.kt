/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.tabs.media.source

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.source.media.source.subscription.MediaSourceSubscription
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.utils.platform.Uuid
import kotlin.jvm.JvmName

@Stable
class MediaSourceSubscriptionGroupState(
    subscriptionsState: State<List<MediaSourceSubscription>>,
    private val onUpdateAll: suspend () -> Unit,
    private val onAdd: suspend (MediaSourceSubscription) -> Unit,
    private val onDelete: (MediaSourceSubscription) -> Unit,
    backgroundScope: CoroutineScope,
) {
    val subscriptions by subscriptionsState

    private val updateAllTasker = MonoTasker(backgroundScope)
    val isUpdateAllInProgress get() = updateAllTasker.isRunning
    fun updateAll() {
        updateAllTasker.launch {
            onUpdateAll()
        }
    }


    var editingUrl by mutableStateOf("")
        private set

    @JvmName("setEditingUrl1")
    fun setEditingUrl(url: String) {
        if (isAddInProgress) {
            return
        }
        editingUrl = url
    }

    val editingUrlIsError by derivedStateOf { editingUrl.isEmpty() }

    private val addTasker = MonoTasker(backgroundScope)
    val isAddInProgress get() = addTasker.isRunning
    fun addNew(string: String) {
        addTasker.launch {
            onAdd(
                MediaSourceSubscription(
                    subscriptionId = Uuid.randomString(),
                    url = string,
                ),
            )
        }
    }

    fun delete(subscription: MediaSourceSubscription) {
        onDelete(subscription)
    }
}

@Composable
internal fun SettingsScope.MediaSourceSubscriptionGroup(
    state: MediaSourceSubscriptionGroupState,
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    Group(
        title = { Text("数据源订阅") },
        description = { Text("可通过订阅添加多个数据源，自动定时更新订阅") },
        actions = {
            IconButton({ showAddDialog = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "添加")
            }

            AnimatedContent(
                state.isUpdateAllInProgress,
                transitionSpec = AniThemeDefaults.standardAnimatedContentTransition,
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (it) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    IconButton({ state.updateAll() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "刷新全部")
                    }
                }
            }
        },
    ) {
        for (subscription in state.subscriptions) {
            Item(
                headlineContent = {
                    SelectionContainer {
                        Text(subscription.url)
                    }
                },
                supportingContent = {
                    Text(
                        "每 ${subscription.updatePeriod} 自动更新，" + formatLastUpdated(subscription.lastUpdated),
                    )
                },
                trailingContent = {
                    var showDropdown by remember { mutableStateOf(false) }
                    IconButton({ showDropdown = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(showDropdown, { showDropdown = false }) {
//                        DropdownMenuItem(
//                            text = { Text("立即更新") },
//                            onClick = {},
//                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                            onClick = { state.delete(subscription) },
                        )
                    }
                },
            )
        }

        if (showAddDialog) {
            val textFieldFocus = remember { FocusRequester() }
            val confirmAdd = {
                showAddDialog = false
                state.addNew(state.editingUrl)
            }
            AlertDialog(
                { showAddDialog = false },
                confirmButton = {
                    AnimatedContent(
                        state.isAddInProgress,
                        transitionSpec = AniThemeDefaults.standardAnimatedContentTransition,
                        contentAlignment = Alignment.BottomEnd,
                    ) {
                        if (it) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                        } else {
                            TextButton(confirmAdd) {
                                Text("添加")
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton({ showAddDialog = false }) {
                        Text("取消")
                    }
                },
                title = {
                    Text("添加订阅")
                },
                text = {
                    OutlinedTextField(
                        value = state.editingUrl,
                        onValueChange = { state.setEditingUrl(it) },
                        Modifier.focusRequester(textFieldFocus),
                        isError = state.editingUrlIsError,
                        enabled = !(state.isAddInProgress),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { confirmAdd() }),
                        label = { Text("URL (HTTP)") },
                    )
                    SideEffect {
                        textFieldFocus.requestFocus()
                    }
                },
            )
        }
    }
}

@Composable
private fun formatLastUpdated(lastUpdated: MediaSourceSubscription.LastUpdated?): String {
    if (lastUpdated == null) return "还未更新"
    val mediaSourceCount = lastUpdated.mediaSourceCount
    return when {
        lastUpdated.error != null || mediaSourceCount == null -> {
            "${formatDateTime(lastUpdated.timeMillis)}更新失败：${lastUpdated.error?.message}"
        }

        else -> {
            "${formatDateTime(lastUpdated.timeMillis)}更新成功，包含 $mediaSourceCount 个数据源"
        }
    }
//    return when (lastUpdated.error) {
//        MediaSourceSubscription.UpdateError.NETWORK_ERROR -> "${formatDateTime(lastUpdated.timeMillis)}因网络错误更新失败"
//        MediaSourceSubscription.UpdateError.INVALID_CONFIG -> "${formatDateTime(lastUpdated.timeMillis)}因远程配置有误更新失败"
//        null -> "${formatDateTime(lastUpdated.timeMillis)}更新成功"
//    }
}
