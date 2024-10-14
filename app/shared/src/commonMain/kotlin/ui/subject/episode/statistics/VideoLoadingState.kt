/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.episode.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import me.him188.ani.app.domain.media.resolver.VideoSourceResolver
import me.him188.ani.app.domain.media.selector.MediaSelector
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.text.ProvideContentColor
import me.him188.ani.datasources.api.Media

@Immutable
sealed interface VideoLoadingState {
    sealed interface Progressing : VideoLoadingState

    /**
     * 等待选择 [Media] ([MediaSelector])
     */
    data object Initial : VideoLoadingState

    /**
     * 在解析磁力链/寻找文件
     */
    data object ResolvingSource : VideoLoadingState, Progressing

    /**
     * WEB: 已经成功解析到 m3u8 链接
     * BT: 要解析磁力链, 查询元数据
     */
    data class DecodingData(
        val isBt: Boolean,
    ) : VideoLoadingState, Progressing

    /**
     * 文件成功找到
     */
    data class Succeed(
        val isBt: Boolean,
    ) : VideoLoadingState, Progressing

    sealed class Failed : VideoLoadingState
    data object ResolutionTimedOut : Failed()
    data object NetworkError : Failed()
    data object Cancelled : Failed()

    /**
     * 不支持的媒体, 或者说是未启用支持该媒体的 [VideoSourceResolver]
     */
    data object UnsupportedMedia : Failed()
    data object NoMatchingFile : Failed()
    data class UnknownError(
        val cause: Throwable,
    ) : Failed()
}

@Composable
fun SimpleErrorDialog(
    text: () -> String,
    onDismissRequest: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val copy = {
        clipboard.setText(AnnotatedString(text()))
    }
    AlertDialog(
        onDismissRequest,
        confirmButton = {
            TextButton(copy) {
                Text("复制")
            }
        },
        dismissButton = {
            TextButton(onDismissRequest) {
                Text("关闭")
            }
        },
        title = { Text("错误详情") },
        text = {
            OutlinedTextField(
                value = text(),
                onValueChange = {},
                trailingIcon = {
                    IconButton(copy) {
                        Icon(Icons.Outlined.ContentCopy, "复制")
                    }
                },
                readOnly = true,
                maxLines = 4,
            )
        },
    )
}

@Composable
fun VideoLoadingSummary(
    state: VideoLoadingState,
    color: Color = MaterialTheme.colorScheme.error,
) {
    if (state is VideoLoadingState.Failed) {
        ProvideContentColor(color) {
            var showErrorDialog by rememberSaveable(state) { mutableStateOf(false) }
            if (showErrorDialog) {
                val text = remember(state) {
                    when (state) {
                        is VideoLoadingState.UnknownError -> state.cause.stackTraceToString()
                        else -> state.toString()
                    }
                }
                SimpleErrorDialog({ text }) { showErrorDialog = false }
            }
            Row(
                Modifier.ifThen(state is VideoLoadingState.UnknownError) {
                    clickable { showErrorDialog = true }
                },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.align(Alignment.Top)
                        .minimumInteractiveComponentSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.ErrorOutline, "错误",
                    )
                }

                when (state) {
                    VideoLoadingState.NoMatchingFile -> Text("未匹配到文件")
                    VideoLoadingState.ResolutionTimedOut -> Text("解析超时")
                    VideoLoadingState.UnsupportedMedia -> Text("不支持的视频类型")
                    is VideoLoadingState.UnknownError -> {
                        Text("未知错误，点击查看")
                    }

                    VideoLoadingState.Cancelled -> Text("已取消")
                    VideoLoadingState.NetworkError -> Text("网络错误，请检查网络连接状况")
                }
            }
        }
    }
}
