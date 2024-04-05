package me.him188.ani.app.ui.subject.episode.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.ui.foundation.launchInMain
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle


/**
 * 一行功能按钮.
 *
 * 选择播放源, 复制磁力, 下载, 原始页面.
 */
@Composable
fun EpisodeActionRow(
    viewModel: EpisodeViewModel,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
    ) {
        // 选择播放源
        PlaySourceSelectionAction(viewModel)

        val clipboard by rememberUpdatedState(LocalClipboardManager.current)
        ActionButton(
            onClick = {
                viewModel.launchInMain {
                    copyDownloadLink(clipboard, snackbar)
                }
            },
            icon = { Icon(Icons.Rounded.ContentCopy, null) },
            text = { Text("复制磁力") },
            modifier,
        )

        val context = LocalContext.current
        ActionButton(
            onClick = {
                viewModel.launchInMain {
                    browseDownload(context, snackbar)
                }
            },
            icon = { Icon(Icons.Rounded.Download, null) },
            text = { Text("下载") },
            modifier,
        )

        ActionButton(
            onClick = {
                viewModel.launchInMain {
                    browsePlaySource(context, snackbar)
                }
            },
            icon = { Icon(Icons.Rounded.ArrowOutward, null) },
            text = { Text("原始页面") },
            modifier,
        )
    }

    val showPlaySourceSheet by viewModel.isShowPlaySourceSheet.collectAsStateWithLifecycle()
    if (showPlaySourceSheet) {
        PlaySourceSheet(viewModel)
    }
}


/**
 * 选择播放源
 */
@Composable
private fun PlaySourceSelectionAction(
    viewModel: EpisodeViewModel,
    modifier: Modifier = Modifier,
) {
    val isPlaySourcesLoading by viewModel.isPlaySourcesLoading.collectAsStateWithLifecycle()

    ActionButton(
        onClick = {
            viewModel.setShowPlaySourceSheet(true)
        },
        icon = { Icon(Icons.Rounded.DisplaySettings, null) },
        text = { Text("数据源") },
        modifier,
        isPlaySourcesLoading
    )
}

/**
 * 数据源; 下载; 分享
 */
@Composable
private fun ActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = onClick
            )
            .size(64.dp)
            .padding(all = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        icon()
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                text()
            }

            AnimatedVisibility(isLoading) {
                Box(Modifier.padding(start = 8.dp).height(12.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(12.dp), strokeWidth = 2.dp)
                }
            }
        }
    }
}
