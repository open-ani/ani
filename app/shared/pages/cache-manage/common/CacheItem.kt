package me.him188.ani.app.pages.cache.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.sample
import me.him188.ani.app.ui.subject.episode.mediaFetch.renderMediaSource
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
import me.him188.ani.datasources.core.cache.MediaCache
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.openapitools.client.models.Subject


@Stable
class CacheItem(
    val cache: MediaCache,
    subject: Subject?,
//    private val episode: EpisodeDetail?,
//    val episode: Flow<Episode>,
) {
    val origin: Media get() = cache.origin

    //    val subjectImage = subject?.images?.large
    val subjectName = subject?.nameCNOrName() ?: cache.metadata.subjectNames.firstOrNull() ?: "未知"
    val episodeName = cache.metadata.episodeName

    val mediaSourceId = cache.origin.mediaId
    val episodeSort = cache.metadata.episodeSort

    val downloadSpeed = cache.downloadSpeed.sample(1000)
    val uploadSpeed = cache.uploadSpeed.sample(1000)
    val progress = cache.progress.sample(1000)
        .onCompletion { if (it == null) emit(1f) }
    val totalSize = cache.totalSize
}

@Composable
fun CacheItemView(
    item: CacheItem,
    onDelete: (CacheItem) -> Unit,
    mediaSourceId: () -> String,
    modifier: Modifier = Modifier,
) {
    Card(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Row(
            Modifier.padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                renderMediaSource(item.subjectName),
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )

            var showDeleteDialog by remember { mutableStateOf(false) }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    text = { Text("确认删除缓存吗? 该操作不可撤销") },
                    // for Delete, see me.him188.ani.app.pages.cache.manage.MediaCacheStorageState.delete
                    confirmButton = { Button({ onDelete(item) }) { Text("删除") } },
                    dismissButton = { TextButton({ showDeleteDialog = false }) { Text("取消") } }
                )
            }

            IconButton(
                { showDeleteDialog = true },
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = "删除")
            }
        }

        Column(
            Modifier.padding(bottom = 16.dp).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row {
                Text(
                    renderMediaSource(item.episodeSort.toString()),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    renderMediaSource(item.episodeName),
                    Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                // progress bar
                val progress by item.progress.collectAsStateWithLifecycle(null)
                if (progress != null && progress != 1f) {
                    Row {
                        LinearProgressIndicator(
                            progress = { progress ?: 0f },
                            Modifier.weight(1f),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        // 图标
                        if (progress == 1f) {
                            Icon(
                                Icons.Rounded.DownloadDone,
                                null,
                                Modifier.padding(end = 8.dp)
                            )
                        } else {
                            Icon(
                                Icons.Rounded.Downloading,
                                null,
                                Modifier.padding(end = 8.dp)
                            )
                        }

                        Text(renderMediaSource(mediaSourceId())) // "本地"

                        val totalSize by item.totalSize.collectAsStateWithLifecycle(null)
                        Text(
                            remember(totalSize) {
                                totalSize?.toString().orEmpty()
                            },
                            Modifier.padding(start = 16.dp),
                        )
                    }

                    // 百分比和速度
                    if (progress != null) {
                        val downloadSpeed by item.downloadSpeed.collectAsStateWithLifecycle(FileSize.Unspecified)
                        val uploadSpeed by item.uploadSpeed.collectAsStateWithLifecycle(FileSize.Unspecified)
                        CacheProgressLabel(
                            uploadSpeedProvider = { uploadSpeed },
                            downloadSpeedProvider = { downloadSpeed },
                            progress = progress,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 未完成时展示下载速度, 完成时展示上传速度
 */
@Composable
fun CacheProgressLabel(
    uploadSpeedProvider: () -> FileSize,
    downloadSpeedProvider: () -> FileSize,
    progress: Float?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val uploadSpeed by derivedStateOf(uploadSpeedProvider)
        val downloadSpeed by derivedStateOf(downloadSpeedProvider)
        val progressText by derivedStateOf {
            if (progress == 1f) {
                renderSpeed(uploadSpeed)
            } else {
                renderSpeed(downloadSpeed)
            }
        }

        // max width is 100.0%
        Row(Modifier.padding(end = 8.dp).widthIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(progressText)
        }

        if (progress != 1f) {
            Text(
                remember(progress) {
                    "${String.format("%.1f", (progress ?: 0f) * 100)}%"
                },
                Modifier.padding(end = 8.dp).widthIn(min = 48.dp), // max width is 100.0%
                textAlign = TextAlign.Center
            )
        }
    }
}

@Stable
fun renderFileSize(size: FileSize): String {
    if (size == FileSize.Unspecified) {
        return ""
    }
    return "$size"
}

@Stable
fun renderSpeed(speed: FileSize): String {
    if (speed == FileSize.Unspecified) {
        return ""
    }
    return "$speed/s"
}