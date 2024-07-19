package me.him188.ani.app.ui.subject.episode.details.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Outbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.icons.PlayingIcon
import me.him188.ani.app.ui.foundation.text.ProvideContentColor
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.settings.rendering.MediaSourceIcons
import me.him188.ani.app.ui.settings.rendering.renderMediaSource
import me.him188.ani.datasources.api.Media

/**
 * 剧集详情页中的正在播放的剧集卡片
 *
 * 可交互内容:
 * - 标记看过
 * - 切换数据源
 * - 跳转缓存页
 */
@Composable
fun PlayingEpisodeItem(
    episodeSort: @Composable () -> Unit,
    title: @Composable () -> Unit,
    watchStatus: @Composable () -> Unit,
    mediaSelected: Boolean,
    mediaLabels: @Composable () -> Unit,
    filename: @Composable () -> Unit,
    mediaSource: @Composable RowScope.() -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp).padding(bottom = 16.dp),
//                .padding(top = 4.dp)
//                .padding(bottom = 4.dp),
        ) {
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Top) {
                FlowRow(
                    Modifier.padding(top = 12.dp).weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                        ProvideContentColor(MaterialTheme.colorScheme.primary) {
                            PlayingIcon()
                        }
                        episodeSort()
                        title()
                    }
                }
                Box(Modifier.padding(start = 12.dp)) {
                    watchStatus()
                }
            }
            Spacer(Modifier.height(12.dp))
            if (mediaSelected) {
                ProvideTextStyleContentColor(MaterialTheme.typography.labelLarge, MaterialTheme.colorScheme.secondary) {
                    Row {
                        mediaLabels()
                    }
                }
                Spacer(Modifier.height(24.dp))
                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.Description, null)
                        filename()
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(8.dp))
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                Row(
                    Modifier, // cancel out semantic paddings
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp), // 20.dp effectively
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        mediaSource()
                    }
                    Row(
                        Modifier.padding(start = 32.dp).offset(x = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp), // 20.dp effectively
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        actions()
                    }
                }
            }
        }
    }
}


object PlayingEpisodeItemDefaults {
    @Composable
    fun ActionCache(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        IconButton(onClick, modifier) {
            Icon(Icons.Rounded.Download, "缓存")
        }
    }

    @Composable
    fun ActionShare(
        playingMedia: Media?,
        modifier: Modifier = Modifier,
    ) {
        var showShareDropdown by rememberSaveable { mutableStateOf(false) }
        Box {
            IconButton({ showShareDropdown = true }, modifier) {
                Icon(Icons.Rounded.Outbox, "分享")
            }
            ShareEpisodeDropdown(
                showShareDropdown, { showShareDropdown = false },
                playingMedia = playingMedia,
            )
        }
    }

    @Composable
    fun MediaSource(
        media: Media?,
        isLoading: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (media != null) {
                OutlinedButton(
                    onClick = onClick,
                    Modifier,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LocalContentColor.current),
                ) {
                    Icon(MediaSourceIcons.location(media.location, media.kind), null)

                    Text(
                        remember(media.mediaSourceId) { renderMediaSource(media.mediaSourceId) },
                        Modifier.padding(start = 12.dp).align(Alignment.CenterVertically),
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            } else {
                Button(
                    onClick = onClick,
                    Modifier,
                ) {
                    Icon(Icons.Rounded.DisplaySettings, null)

                    Text(
                        "选择数据源",
                        Modifier.padding(start = 12.dp).align(Alignment.CenterVertically),
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }

            AnimatedVisibility(
                isLoading,
                Modifier.align(Alignment.CenterVertically),
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it }),
            ) {
                CircularProgressIndicator(Modifier.size(24.dp))
            }
        }
    }
}
