package me.him188.ani.app.ui.cache.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.FilePresent
import androidx.compose.material.icons.rounded.Hd
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.navigation.LocalBrowserNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.settings.rendering.MediaSourceIcon
import me.him188.ani.app.ui.subject.episode.details.renderSubtitleLanguage
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.SubtitleKind
import me.him188.ani.datasources.api.source.MediaSourceInfo
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.isSingleEpisode

@Composable
fun MediaDetailsColumn(
    media: Media,
    sourceInfo: MediaSourceInfo?,
    modifier: Modifier = Modifier,
) {
    val browser = LocalBrowserNavigator.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Column(modifier) {
        val copyContent = @Composable { value: () -> String ->
            val toaster = LocalToaster.current
            IconButton(
                {
                    clipboard.setText(AnnotatedString(value()))
                    toaster.toast("已复制")
                },
            ) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = "复制")
            }
        }
        val browseContent = @Composable { url: String ->
            IconButton({ browser.openBrowser(context, url) }) {
                Icon(Icons.Rounded.ArrowOutward, contentDescription = "打开链接")
            }
        }

        ListItem(
            headlineContent = { SelectionContainer { Text(media.originalTitle) } },
            trailingContent = { copyContent { media.originalTitle } },
        )
        ListItem(
            headlineContent = { Text("剧集范围") },
            leadingContent = { Icon(Icons.Rounded.Layers, contentDescription = null) },
            supportingContent = {
                val range = media.episodeRange
                SelectionContainer {
                    Text(
                        when {
                            range == null -> "未知"
                            range.isSingleEpisode() -> range.knownSorts.firstOrNull().toString()
                            else -> range.toString()
                        },
                    )
                }
            },
        )
        ListItem(
            headlineContent = { Text("数据源") },
            leadingContent = { MediaSourceIcon(sourceInfo?.imageUrl, Modifier.size(24.dp)) },
            supportingContent = {
                val kind = when (media.kind) {
                    MediaSourceKind.WEB -> "在线"
                    MediaSourceKind.BitTorrent -> "BT"
                    MediaSourceKind.LocalCache -> "本地"
                }
                SelectionContainer { Text("[$kind] ${sourceInfo?.displayName ?: "未知"}") }
            },
            trailingContent = kotlin.run {
                val originalUrl by rememberUpdatedState(media.originalUrl)
                val isUrlLegal by remember {
                    derivedStateOf {
                        originalUrl.startsWith("http://", ignoreCase = true)
                                || originalUrl.startsWith("https://", ignoreCase = true)
                    }
                }
                if (isUrlLegal) {
                    {
                        browseContent(originalUrl)
                    }
                } else {
                    {
                        copyContent { originalUrl }
                    }
                }
            },
        )
        ListItem(
            headlineContent = { Text("字幕组") },
            leadingContent = { Icon(Icons.Rounded.Subtitles, contentDescription = null) },
            supportingContent = { SelectionContainer { Text(media.properties.alliance) } },
            trailingContent = { copyContent { media.properties.alliance } },
        )
        ListItem(
            headlineContent = { Text("字幕语言") },
            leadingContent = { Icon(Icons.Rounded.Subtitles, contentDescription = null) },
            supportingContent = {
                SelectionContainer {
                    Text(
                        remember(media) {
                            buildString {
                                val subtitleKind = media.properties.subtitleKind
                                if (subtitleKind != null) {
                                    append("[")
                                    append(
                                        when (subtitleKind) {
                                            SubtitleKind.EMBEDDED -> "内嵌"
                                            SubtitleKind.CLOSED -> "内封"
                                            SubtitleKind.EXTERNAL_PROVIDED -> "外挂"
                                            SubtitleKind.EXTERNAL_DISCOVER -> "未知"
                                            SubtitleKind.CLOSED_OR_EXTERNAL_DISCOVER -> "内封或未知"
                                        },
                                    )
                                    append("] ")
                                } else {
                                    if (media.properties.subtitleLanguageIds.isEmpty()) {
                                        append("未知")
                                    }
                                }

                                for ((index, subtitleLanguageId) in media.properties.subtitleLanguageIds.withIndex()) {
                                    append(renderSubtitleLanguage(subtitleLanguageId))
                                    if (index != media.properties.subtitleLanguageIds.size - 1) {
                                        append(" ")
                                    }
                                }
                            }
                        },
                    )
                }
            },
        )
        ListItem(
            headlineContent = { Text("发布时间") },
            leadingContent = { Icon(Icons.Rounded.Event, contentDescription = null) },
            supportingContent = { SelectionContainer { Text(formatDateTime(media.publishedTime)) } },
        )
        ListItem(
            headlineContent = { Text("分辨率") },
            leadingContent = { Icon(Icons.Rounded.Hd, contentDescription = null) },
            supportingContent = { SelectionContainer { Text(media.properties.resolution) } },
        )
        ListItem(
            headlineContent = { Text("文件大小") },
            leadingContent = { Icon(Icons.Rounded.Description, contentDescription = null) },
            supportingContent = {
                SelectionContainer {
                    if (media.properties.size == FileSize.Unspecified) {
                        Text("未知")
                    } else {
                        Text(media.properties.size.toString())
                    }
                }
            },
        )
        ListItem(
            headlineContent = { Text("原始下载方式") },
            leadingContent = { Icon(Icons.Rounded.VideoFile, contentDescription = null) },
            supportingContent = {
                SelectionContainer {
                    Text(media.download.contentUri, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            },
            trailingContent = { copyContent { media.download.contentUri } },
        )

        media.extraFiles.subtitles.forEachIndexed { index, subtitle ->
            ListItem(
                headlineContent = {
                    SelectionContainer {
                        Text(
                            remember(subtitle) {
                                buildString {
                                    append("外挂字幕 ${index + 1}")
                                    subtitle.language?.let {
                                        append(": ")
                                        append(it)
                                    }
                                }
                            },
                        )
                    }
                },
                leadingContent = { Icon(Icons.Rounded.FilePresent, contentDescription = null) },
                supportingContent = { SelectionContainer { Text(subtitle.uri) } },
                trailingContent = { browseContent(subtitle.uri) },
            )
        }
    }
}

@Stable
private val ResourceLocation.contentUri: String
    get() = when (this) {
        is ResourceLocation.HttpStreamingFile -> this.uri
        is ResourceLocation.HttpTorrentFile -> this.uri
        is ResourceLocation.LocalFile -> this.filePath
        is ResourceLocation.MagnetLink -> this.uri
        is ResourceLocation.WebVideo -> this.uri
    }
