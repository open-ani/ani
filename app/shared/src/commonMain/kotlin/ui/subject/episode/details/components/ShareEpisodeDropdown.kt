package me.him188.ani.app.ui.subject.episode.details.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Outbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import me.him188.ani.app.navigation.LocalBrowserNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.ResourceLocation

@Composable
fun ShareEpisodeDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    playingMedia: Media?,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    val browserNavigator = LocalUriHandler.current

    val playingMediaState by rememberUpdatedState(playingMedia)
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        val isMagnet by remember {
            derivedStateOf {
                playingMediaState?.download is ResourceLocation.MagnetLink
            }
        }
        DropdownMenuItem(
            text = {
                if (isMagnet) {
                    Text("复制磁力链接")
                } else {
                    Text("复制下载链接")
                }
            },
            onClick = {
                onDismissRequest()
                playingMediaState?.let {
                    clipboard.setText(AnnotatedString(it.originalUrl))
                }
            },
            leadingIcon = { Icon(Icons.Rounded.ContentCopy, null) },
        )

        DropdownMenuItem(
            text = { Text("使用其他应用下载") },
            onClick = {
                onDismissRequest()
                playingMediaState?.let {
                    browserNavigator.openUri(it.download.uri)
                }
            },
            leadingIcon = { Icon(Icons.Rounded.Outbox, null) },
        )
        DropdownMenuItem(
            text = { Text("访问数据源页面") },
            onClick = {
                onDismissRequest()
                playingMediaState?.let {
                    browserNavigator.openUri(it.originalUrl)
                }
            },
            leadingIcon = { Icon(Icons.Rounded.ArrowOutward, null) },
        )
    }
}

