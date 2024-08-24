package me.him188.ani.app.ui.subject.collection.components

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import me.him188.ani.app.data.models.subject.SubjectAiringInfo
import me.him188.ani.app.data.models.subject.SubjectAiringKind
import me.him188.ani.app.data.models.subject.isOnAir
import me.him188.ani.app.data.models.toStringExcludingSameYear

/**
 * ```
 * 已完结 · 全 28 话
 * ```
 *
 * ```
 * 连载至第 28 话 · 全 34 话
 * ```
 *
 * @sample
 */
@Composable
fun AiringLabel(
    info: SubjectAiringInfo,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    statusColor: Color = if (info.isOnAir) MaterialTheme.colorScheme.primary else LocalContentColor.current,
) {
    ProvideTextStyle(style) {
        Row(modifier.width(IntrinsicSize.Max).height(IntrinsicSize.Min)) {
            Text(
                remember(info) {
                    when (info.kind) {
                        SubjectAiringKind.UPCOMING -> {
                            if (info.airDate.isInvalid) {
                                "未开播"
                            } else {
                                info.airDate.toStringExcludingSameYear() + " 开播"
                            }
                        }

                        SubjectAiringKind.ON_AIR -> {
                            if (info.latestSort == null) {
                                "连载中"
                            } else {
                                "连载至第 ${info.latestSort} 话"
                            }
                        }

                        SubjectAiringKind.COMPLETED -> "已完结"
                    }
                },
                color = statusColor,
                maxLines = 1,
            )
            if (info.kind == SubjectAiringKind.UPCOMING && info.episodeCount == 0) {
                // 剧集还未知
            } else {
                Text(
                    " · ",
                    maxLines = 1,
                )
                Text(
                    when (info.kind) {
                        SubjectAiringKind.ON_AIR,
                        SubjectAiringKind.COMPLETED -> "全 ${info.episodeCount} 话"

                        SubjectAiringKind.UPCOMING -> "预定全 ${info.episodeCount} 话"
                    },
                    maxLines = 1,
                )
            }
        }
    }
}
