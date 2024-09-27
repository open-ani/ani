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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import me.him188.ani.app.data.source.media.source.codec.FactoryNotFoundException
import me.him188.ani.app.data.source.media.source.codec.MediaSourceArguments
import me.him188.ani.app.data.source.media.source.codec.MediaSourceCodecManager
import me.him188.ani.app.data.source.media.source.codec.MediaSourceDecodeException
import me.him188.ani.app.data.source.media.source.codec.UnsupportedVersionException
import me.him188.ani.app.data.source.media.source.codec.decodeFromStringOrNull
import me.him188.ani.app.data.source.media.source.codec.serializeToString
import me.him188.ani.app.ui.foundation.isInDebugMode
import me.him188.ani.app.ui.foundation.widgets.LocalToaster

@Stable
object MediaSourceConfigurationDefaults {
    val outlinedTextFieldShape
        @Composable
        get() = MaterialTheme.shapes.medium
}

class ImportMediaSourceState<T : MediaSourceArguments>(
    private val codecManager: MediaSourceCodecManager,
    private val onImport: (T) -> Unit,
) {
    internal var parseResult by mutableStateOf<ParseResult?>(null)
        private set

    internal val error by derivedStateOf {
        parseResult as? ParseResult.Error
    }
    internal val showOverrideDialog by derivedStateOf {
        parseResult is ParseResult.Success<*>
    }

    fun parseContent(string: String?) {
        if (string.isNullOrBlank()) {
            parseResult = ParseResult.EmptyContent
            return
        }
        val list = codecManager.decodeFromStringOrNull(string)
        if (list == null) {
            parseResult = ParseResult.InvalidContent
            return
        }
        if (list.mediaSources.isEmpty()) {
            parseResult = ParseResult.EmptyContent
            return
        }
        if (list.mediaSources.size > 1) {
            parseResult = ParseResult.HasMoreThanOneArgument
            return
        }
        val data = list.mediaSources.single()

        val argument = try {
            codecManager.decode(data)
        } catch (e: MediaSourceDecodeException) {
            parseResult = when (e) {
                is UnsupportedVersionException -> ParseResult.UnsupportedVersion
                is FactoryNotFoundException -> ParseResult.UnsupportedFactory
            }
            return
        }
        parseResult = ParseResult.Success(argument)
    }

    fun cancelOverride() {
        parseResult = null
    }

    fun dismissError() {
        parseResult = null
    }

    fun confirmImport() {
        (parseResult as? ParseResult.Success<*>)?.let {
            @Suppress("UNCHECKED_CAST")
            onImport(it.argument as T)
        }
        parseResult = null
    }
}

@Immutable
internal sealed class ParseResult {
    @Immutable
    sealed class Error : ParseResult()

    @Immutable
    data object EmptyContent : Error()

    @Immutable
    data object InvalidContent : Error()

    @Immutable
    data object HasMoreThanOneArgument : Error()

    @Immutable
    data object UnsupportedFactory : Error()

    @Immutable
    data object UnsupportedVersion : Error()

    @Immutable
    data class Success<T>(
        val argument: T,
    ) : ParseResult()
}

/**
 * 点击后从剪贴板导入配置
 *
 * 配置有效时, 将会弹出一个对话框让用户确认覆盖现有配置.
 * 配置无效时, 将会显示一个错误提示.
 */
@Suppress("UnusedReceiverParameter")
@Composable
fun <T : MediaSourceArguments> MediaSourceConfigurationDefaults.DropdownMenuImport(
    state: ImportMediaSourceState<T>,
    onImported: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val clipboard = LocalClipboardManager.current
    DropdownMenuItem(
        text = { Text("从剪贴板导入配置") },
        onClick = { state.parseContent(clipboard.getText()?.text) },
        modifier,
        leadingIcon = { Icon(Icons.Rounded.ContentPaste, null) },
        enabled = enabled,
    )
    if (state.showOverrideDialog) {
        val toaster = LocalToaster.current
        AlertDialog(
            onDismissRequest = { state.cancelOverride() },
            icon = { Icon(Icons.Rounded.ContentPaste, null) },
            title = { Text("导入配置") },
            text = { Text("将会覆盖现有配置，且不能撤销") },
            confirmButton = {
                TextButton(
                    {
                        state.confirmImport()
                        toaster.toast("已导入配置")
                        onImported()
                    },
                ) {
                    Text("覆盖", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton({ state.cancelOverride() }) {
                    Text("取消")
                }
            },
        )
    }
    val error = state.error
    if (error != null) {
        AlertDialog(
            { state.dismissError() },
            icon = { Icon(Icons.Rounded.Error, null) },
            title = {
                when (error) {
                    ParseResult.EmptyContent -> Text("剪贴板内容为空")
                    ParseResult.HasMoreThanOneArgument -> Text("剪贴板内容包含多个数据源配置，当前导入功能只支持单个配置")
                    ParseResult.InvalidContent -> Text("剪贴板内容无效")
                    ParseResult.UnsupportedFactory -> Text("数据源类型不受支持，请先升级软件")
                    ParseResult.UnsupportedVersion -> Text("数据源版本不受支持，请先升级软件")
                }
            },
            confirmButton = {
                TextButton({ state.dismissError() }) {
                    Text("关闭")
                }
            },
        )
    }
}

class ExportMediaSourceState(
    private val codecManager: MediaSourceCodecManager,
    private val onExport: () -> MediaSourceArguments?,
) {
    fun serializeToString(): String? {
        return onExport()?.let {
            codecManager.serializeToString(listOf(it))
        }
    }

    fun serializeSingleToString(): String? {
        return onExport()?.let {
            codecManager.serializeSingleToString(it)
        }
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
fun MediaSourceConfigurationDefaults.DropdownMenuExport(
    state: ExportMediaSourceState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val clipboard = LocalClipboardManager.current
    val toaster = LocalToaster.current
    DropdownMenuItem(
        text = { Text("导出配置") },
        onClick = {
            state.serializeToString()?.let {
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
    if (isInDebugMode()) {
        DropdownMenuItem(
            text = { Text("导出单个配置 (仅限开发者)") },
            onClick = {
                state.serializeSingleToString()?.let {
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
}
