package me.him188.ani.app.ui.cache.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.text.ProvideContentColor
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.FileSize

@Stable
class CacheEpisodeState(
    val subjectId: Int,
    val episodeId: Int,
    val sort: EpisodeSort,
    val displayName: String,
    screenShots: State<List<String>>, // url
    downloadSpeed: State<FileSize>,
    progress: State<Float?>,
    state: State<CacheEpisodePaused>,
    private val onPause: suspend () -> Unit, // background scope
    private val onResume: suspend () -> Unit, // background scope
    private val onDelete: suspend () -> Unit, // background scope
    private val onPlay: () -> Unit, // ui scope
    backgroundScope: CoroutineScope,
) {
    val screenShots by screenShots

    //    val combinedId: Long = (subjectId.toLong() shl 32) or episodeId.toLong()
    val isPaused by derivedStateOf { state.value == CacheEpisodePaused.PAUSED }
    val isFinished by derivedStateOf { (progress.value ?: 0f) >= 1f }
    val downloadSpeedText by derivedStateOf {
        if (!this.isFinished && downloadSpeed.value != FileSize.Unspecified) {
            "${downloadSpeed.value}/s"
        } else {
            null
        }
    }
    val progress by derivedStateOf { progress.value ?: 0f }
    val isProgressUnspecified by derivedStateOf { progress.value == null }

    private val actionTasker = MonoTasker(backgroundScope)

    val isActionInProgress by actionTasker::isRunning

    fun pause() {
        actionTasker.launch {
            onPause()
        }
    }

    fun resume() {
        actionTasker.launch {
            onResume()
        }
    }

    fun delete() {
        actionTasker.launch {
            onDelete()
        }
    }

    fun play() {
        onPlay()
    }
}

@Immutable
enum class CacheEpisodePaused {
    IN_PROGRESS,
    PAUSED,
}

@Composable
fun CacheEpisodeItem(
    state: CacheEpisodeState,
    modifier: Modifier = Modifier
) {
    Box {
        var showDropdown by remember { mutableStateOf(false) }
        ListItem(
            headlineContent = {
                Row {
                    Text(
                        "${state.sort}  ",
                        Modifier.align(Alignment.CenterVertically),
                        softWrap = false,
                    )

                    Text(
                        state.displayName,
                        Modifier.weight(1f),
                    )

                    ProvideTextStyleContentColor(MaterialTheme.typography.labelLarge) {
                        state.downloadSpeedText?.let {
                            Text(
                                it,
                                Modifier.padding(start = 16.dp).align(Alignment.Bottom),
                                softWrap = false,
                            )
                        }
                    }
                }
            },
            modifier.clickable { showDropdown = true },
            overlineContent = if (state.screenShots.isEmpty()) null else {
                {
                    AsyncImage(state.screenShots.first(), "封面")
                }
            },
            supportingContent = if (state.isFinished) null else {
                {
                    Crossfade(state.isProgressUnspecified, Modifier.padding(top = 12.dp)) {
                        if (it) {
                            LinearProgressIndicator(
                                Modifier.fillMaxWidth(),
                                trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                                strokeCap = StrokeCap.Round,
                            )
                        } else {
                            val progress by animateFloatAsState(state.progress)
                            LinearProgressIndicator(
                                { progress },
                                Modifier.fillMaxWidth(),
                                trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                                strokeCap = StrokeCap.Round,
                            )
                        }
                    }
                }
            },
            trailingContent = {
                if (state.isActionInProgress) {
                    IconButton({ }) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    }
                } else {
                    when {
                        state.isFinished -> {
                            IconButton({ state.play() }) {
                                Icon(Icons.Rounded.Check, "下载完成")
                            }
                        }

                        state.isPaused -> {
                            IconButton({ state.resume() }) {
                                Icon(Icons.Rounded.Restore, "继续下载")
                            }
                        }

                        !state.isPaused -> {
                            IconButton({ state.pause() }) {
                                Icon(Icons.Rounded.Pause, "暂停下载", Modifier.size(28.dp))
                            }
                        }
                    }
                }

            },
        )
        Dropdown(
            showDropdown, { showDropdown = false },
            state,
            isPaused = state.isPaused,
        )
    }
}

@Composable
private fun Dropdown(
    showDropdown: Boolean,
    onDismissRequest: () -> Unit,
    state: CacheEpisodeState,
    isPaused: Boolean,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(showDropdown, onDismissRequest, modifier) {
        if (isPaused) {
            DropdownMenuItem(
                text = { Text("继续下载") },
                leadingIcon = { Icon(Icons.Rounded.Restore, null) },
                onClick = { state.resume() },
            )
        } else {
            DropdownMenuItem(
                text = { Text("暂停下载") },
                leadingIcon = { Icon(Icons.Rounded.Pause, null) },
                onClick = { state.pause() },
            )
        }
        DropdownMenuItem(
            text = { Text("播放") },
            leadingIcon = { Icon(Icons.Rounded.PlayArrow, null, Modifier.size(24.dp)) },
            onClick = { state.play() },
        )
        ProvideContentColor(MaterialTheme.colorScheme.error) {
            DropdownMenuItem(
                text = { Text("删除") },
                leadingIcon = { Icon(Icons.Rounded.Delete, null) },
                onClick = { state.delete() },
            )
        }
    }
}
