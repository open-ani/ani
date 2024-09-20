/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import me.him188.ani.app.ui.foundation.widgets.LocalToaster

@Stable
object MediaSourceConfigurationDefaults {
    val outlinedTextFieldShape
        @Composable
        get() = MaterialTheme.shapes.medium
}

/**
 * 点击后从剪贴板导入配置
 *
 * 将会调用 [parseContent] 检查配置是否有效. [parseContent] 返回 `true` 代表有效.
 * 配置有效时, 将会弹出一个对话框让用户确认覆盖现有配置.
 * 配置无效时, 将会显示一个错误提示.
 */
@Suppress("UnusedReceiverParameter")
@Composable
fun <T : Any> MediaSourceConfigurationDefaults.DropdownMenuImport(
    parseContent: (String) -> T?,
    onImport: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var currentContent by remember { mutableStateOf<T?>(null) }

    var showOverrideDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    DropdownMenuItem(
        text = { Text("从剪贴板导入配置") },
        onClick = {
            val parsed = clipboard.getText()?.let { parseContent(it.text) }
            if (parsed == null) {
                showErrorDialog = true
            } else {
                currentContent = parsed
                showOverrideDialog = true
            }
        },
        modifier,
        leadingIcon = { Icon(Icons.Rounded.ContentPaste, null) },
        enabled = enabled,
    )
    if (showOverrideDialog) {
        val toaster = LocalToaster.current
        AlertDialog(
            {
                showOverrideDialog = false
            },
            icon = { Icon(Icons.Rounded.ContentPaste, null) },
            title = { Text("导入配置") },
            text = { Text("将会覆盖现有配置，且不能撤销") },
            confirmButton = {
                TextButton(
                    {
                        showOverrideDialog = false
                        currentContent?.let { onImport(it) }
                        toaster.toast("已导入配置")
                    },
                ) {
                    Text("覆盖", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton({ showOverrideDialog = false }) {
                    Text("取消")
                }
            },
        )
    }
    if (showErrorDialog) {
        AlertDialog(
            { showErrorDialog = false },
            icon = { Icon(Icons.Rounded.Error, null) },
            title = { Text("剪贴板内容无效") },
            confirmButton = {
                TextButton({ showErrorDialog = false }) {
                    Text("关闭")
                }
            },
        )
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
fun MediaSourceConfigurationDefaults.DropdownMenuExport(
    encode: () -> String?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val clipboard = LocalClipboardManager.current
    val toaster = LocalToaster.current
    DropdownMenuItem(
        text = { Text("导出配置") },
        onClick = {
            encode()?.let {
                clipboard.setText(AnnotatedString(it))
                toaster.toast("已复制到剪贴板")
            } ?: kotlin.run {
                toaster.toast("目前无法导出，请稍后再试")
            }
            onDismissRequest()
        },
        modifier,
        leadingIcon = { Icon(Icons.Rounded.Share, null) },
        enabled = enabled,
    )
}
