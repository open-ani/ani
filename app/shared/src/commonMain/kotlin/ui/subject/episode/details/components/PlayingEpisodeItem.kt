package me.him188.ani.app.ui.subject.episode.details.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Outbox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.icons.PlayingIcon
import me.him188.ani.app.ui.foundation.layout.paddingIfNotEmpty
import me.him188.ani.app.ui.foundation.text.ProvideContentColor
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.settings.rendering.MediaSourceIcons
import me.him188.ani.app.ui.settings.rendering.getMediaSourceIconResource
import me.him188.ani.app.ui.settings.rendering.renderMediaSource
import me.him188.ani.datasources.api.Media

/**
 * 剧集详情页中的正在播放的剧集卡片. 需要放在合适的 `Card` 中.
 *
 * 可交互内容:
 * - 标记看过
 * - 切换数据源
 * - 跳转缓存页
 *
 * @param mediaSourceInfo [PlayingEpisodeItemDefaults.MediaSourceInfo]
 */
@Composable
fun PlayingEpisodeItem(
    episodeSort: @Composable () -> Unit,
    title: @Composable () -> Unit,
    watchStatus: @Composable () -> Unit,
    mediaSelected: Boolean,
    mediaLabels: @Composable FlowRowScope.() -> Unit,
    mediaSourceInfo: @Composable () -> Unit,
    videoLoadingSummary: @Composable RowScope.() -> Unit,
    mediaSource: @Composable RowScope.() -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
    playingIcon: @Composable () -> Unit = { PlayingIcon() },
    rowSpacing: Dp = 16.dp,
    horizontalPadding: Dp = 20.dp,
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
    ) {
        // top padding 16.dp
        // 每两排之间留 20.dp. 每个 Row 需要保证自己有 20.dp bottom padding

        Row(
            Modifier.padding(
                horizontal = horizontalPadding,
                vertical = rowSpacing - 12.dp, // 让 12.dp 给 watchStatus button
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 标题和"看过"按钮
            // padding 以 text 的为准, top = bottom = 8 + 12 = 20
            FlowRow(
                Modifier.padding(vertical = 12.dp).weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically),
            ) {
                ProvideTextStyle(
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                ) {
                    ProvideContentColor(MaterialTheme.colorScheme.primary) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            playingIcon()
                            episodeSort()
                        }
                        SelectionContainer {
                            title()
                        }
                    }
                }
            }
            Box(Modifier.align(Alignment.Top).padding(start = 16.dp).height(48.dp)) {
                watchStatus()
            }
        }
        // 上面 Row 有 bottom padding = rowSpacing

        if (mediaSelected) {
            // 1080P · 简中 · 122 MB · XX 字幕组
            ProvideTextStyleContentColor(
                MaterialTheme.typography.labelLarge,
            ) {
                FlowRow(
                    Modifier
                        .paddingIfNotEmpty(top = 4.dp) // 额外多 pad 一点
                        .padding(horizontal = horizontalPadding)
                        .paddingIfNotEmpty(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    mediaLabels()
                }
            }
        }

        // 文件名
        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
            Row(
                Modifier
                    .minimumInteractiveComponentSize()
                    .padding(horizontal = horizontalPadding - 8.dp),
            ) {
                mediaSourceInfo()
            }
        }

        // ! 不支持的视频类型
        if (mediaSelected) {
            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                // 上面的 InfoRow 的 Icon 有 8dp padding, 所以这里就不 pad 了
                Row(
                    Modifier
                        .padding(
                            start = horizontalPadding - 8.dp, // 让给 icon
                            end = horizontalPadding,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    videoLoadingSummary()
                }
            }

            // 上面的 InfoRow 的 Icon 有 8dp padding
            Spacer(Modifier.height(rowSpacing - 8.dp))
        } else {
            Spacer(Modifier.height(8.dp)) // 额外 pad 一点,  否则 "选择数据源" 按钮到 "看过" 按钮之间有点挤
        }

        ProvideTextStyle(MaterialTheme.typography.labelLarge) {
            Row(
                Modifier.align(Alignment.End)
                    .padding(horizontal = horizontalPadding - 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    actions()
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
            AnimatedVisibility(
                isLoading,
                Modifier.align(Alignment.CenterVertically),
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
            ) {
                CircularProgressIndicator(Modifier.size(24.dp))
            }
            if (media != null) {
                OutlinedIconButton(
                    onClick = onClick,
                    Modifier,
                    colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = LocalContentColor.current),
                ) {
                    Image(
                        getMediaSourceIconResource(media.mediaSourceId)
                            ?: rememberVectorPainter(MediaSourceIcons.location(media.location, media.kind)),
                        null,
                        Modifier.size(24.dp),
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
        }
    }
}


/**
 * @param icon [MediaSourceInfoDefaults.MediaSourceIcon]
 * @param filename [MediaSourceInfoDefaults.Filename]
 */
@Composable
fun MediaSourceInfo(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    filename: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
) {
    Box(
        modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick),
    ) {
        AnimatedVisibility(
            selected,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onClick,
                    Modifier,
                    colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = LocalContentColor.current),
                ) {
                    Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                        icon()
                    }
                }
                Row(Modifier.weight(1f).basicMarquee(), verticalAlignment = Alignment.CenterVertically) {
                    filename()
                }
                FilledIconButton(
                    onClick,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(Icons.Outlined.Sync, "更换")
                }
            }
        }
        AnimatedVisibility(
            !selected,
            enter = fadeIn(snap()),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
        ) {
            Button(
                onClick = onClick,
                Modifier.fillMaxWidth(),
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
    }
}

object MediaSourceInfoDefaults {
    @Composable
    fun MediaSourceIcon(
        media: Media,
        modifier: Modifier = Modifier,
    ) {
        getMediaSourceIconResource(media.mediaSourceId)?.let {
            Image(
                it,
                null,
                modifier.size(24.dp),
            )
        } ?: Icon(
            MediaSourceIcons.location(media.location, media.kind), null,
            modifier.size(24.dp),
        )
    }

    @Composable
    fun Filename(
        filename: String?,
        mediaSourceId: String?,
        modifier: Modifier = Modifier,
    ) {
        Text(
            filename?.takeIf { it.isNotBlank() } ?: mediaSourceId?.let { renderMediaSource(it) } ?: "",
            modifier,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

