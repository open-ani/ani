/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.ApiFailure

/**
 * 标记接口, 表示一个通用的刷新结果.
 */
interface RefreshResult {
    /**
     * 刷新成功, 不会显示错误按钮.
     */
    interface Success : RefreshResult

    /**
     * 刷新失败, 会显示错误按钮.
     */
    sealed interface Failed : RefreshResult

    /**
     * 一个已知类型的 API 错误
     */
    interface ApiError : Failed {
        val reason: ApiFailure
    }

    /**
     * 一个任意类型异常. 这属于不期望遇到的错误 (bug).
     */
    interface UnknownError : Failed {
        val exception: Throwable
    }
}

/**
 * 包含一个 Text, 一个刷新按钮, 一个错误提示的 [Row].
 *
 * 刷新按钮一直显示. 错误提示只在 [result] 为 [RefreshResult.Failed] 时显示.
 *
 * @see result null 表示查询中
 */
@Composable
fun RefreshIndicatedHeadlineRow(
    headline: @Composable () -> Unit,
    onRefresh: () -> Unit,
    result: RefreshResult?,
    modifier: Modifier = Modifier,
    refreshIcon: @Composable () -> Unit = { RefreshIndicatedHeadlineRowDefaults.RefreshIconButton(onRefresh) },
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
            headline()
        }

        refreshIcon()

        AnimatedVisibility(result is RefreshResult.Failed) {
            if (result !is RefreshResult.Failed) return@AnimatedVisibility
            TextButton(
                onClick = onRefresh,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(Icons.Rounded.Error, null, Modifier.align(Alignment.CenterVertically))
                Text(
                    when (result) {
                        is RefreshResult.ApiError -> {
                            when (result.reason) {
                                ApiFailure.NetworkError -> "网络错误"
                                ApiFailure.ServiceUnavailable -> "服务器错误"
                                ApiFailure.Unauthorized -> "未授权"
                            }
                        }

                        is RefreshResult.UnknownError -> "未知错误: ${result.exception}"
                    },
                    Modifier.padding(start = 8.dp).align(Alignment.CenterVertically),
                )
            }
        }
    }
}

@Stable
object RefreshIndicatedHeadlineRowDefaults {
    @Composable
    fun RefreshIconButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        TextButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            Icon(Icons.Rounded.Refresh, "刷新")
        }
    }
}