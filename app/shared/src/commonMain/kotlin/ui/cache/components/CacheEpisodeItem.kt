package me.him188.ani.app.ui.cache.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.interaction.clickableAndMouseRightClick
import me.him188.ani.app.ui.foundation.text.ProvideContentColor
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.utils.platform.format1f

@Stable
class CacheEpisodeState(
    val subjectId: Int,
    val episodeId: Int,
    val cacheId: String,
    val sort: EpisodeSort,
    val displayName: String,
    screenShots: State<List<String>>, // url
    stats: State<Stats>,
    state: State<CacheEpisodePaused>,
    private val onPause: suspend () -> Unit, // background scope
    private val onResume: suspend () -> Unit, // background scope
    private val onDelete: suspend () -> Unit, // background scope
    private val onPlay: () -> Unit, // ui scope
    backgroundScope: CoroutineScope,
) {
    @Immutable
    data class Stats(
        val downloadSpeed: FileSize,
        val progress: Float?,
        val totalSize: FileSize,
    )

    val hasValidSubjectAndEpisodeId get() = subjectId != 0 && episodeId != 0

    val progress by derivedStateOf { stats.value.progress ?: 0f }

    val screenShots by screenShots

    val isPaused by derivedStateOf { state.value == CacheEpisodePaused.PAUSED }
    val isFinished by derivedStateOf { this.progress >= 1f }

    val totalSize: FileSize by derivedStateOf { stats.value.totalSize }

    val sizeText by derivedStateOf {
        // 原本打算展示 "888.88 MB / 888.88 MB" 的格式, 感觉比较啰嗦, 还是省略了
        // 这个函数有正确的 testing, 应该切换就能用
//        calculateSizeText(totalSize.value, progress.value)

        val value = this.totalSize
        if (value == FileSize.Unspecified) {
            null
        } else {
            "$value"
        }
    }

    val progressText by derivedStateOf {
        val value = stats.value.progress
        if (value == null || this.isFinished) {
            null
        } else {
            "${String.format1f(value * 100)}%"
        }
    }

    val speedText by derivedStateOf {
        val progressValue = stats.value.progress
        val speed = stats.value.downloadSpeed
        if (progressValue != null) {
            val isFinished = progressValue >= 1f
            val showSpeed = !isFinished && speed != FileSize.Unspecified
            if (showSpeed) {
                return@derivedStateOf "${speed}/s"
            }
        }
        null
    }

    val isProgressUnspecified by derivedStateOf { stats.value.progress == null }

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

    companion object {
        /**
         * @sample me.him188.ani.app.ui.cache.components.CacheEpisodeStateTest.CalculateSizeTextTest
         */
        fun calculateSizeText(
            totalSize: FileSize,
            progress: Float?,
        ): String? {
            if (progress == null && totalSize == FileSize.Unspecified) {
                return null
            }
            return when {
                progress == null -> {
                    if (totalSize != FileSize.Unspecified) {
                        "$totalSize"
                    } else null
                }

                totalSize == FileSize.Unspecified -> null

                else -> {
                    "${totalSize * progress} / $totalSize"
                }
            }
        }
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
    var showDropdown by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${state.sort}  ",
                    softWrap = false,
                )

                Text(
                    state.displayName,
                    Modifier.basicMarquee(),
                )
            }
        },
        modifier.clickableAndMouseRightClick { showDropdown = true },
        leadingContent = if (state.screenShots.isEmpty()) null else {
            {
                AsyncImage(state.screenShots.first(), "封面")
            }
        },
        supportingContent = {
            Column(
                Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProvideTextStyleContentColor(MaterialTheme.typography.labelLarge) {
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
                    ) {
                        Box(Modifier.padding(end = 16.dp)) {
                            state.sizeText?.let {
                                Text(it, softWrap = false)
                            }
                        }

                        Box(Modifier, contentAlignment = Alignment.BottomEnd) {
                            Row(
                                Modifier.basicMarquee(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.End),
                            ) {
                                Box(Modifier.align(Alignment.Bottom)) {
                                    state.speedText?.let {
                                        Text(it, softWrap = false)
                                    }
                                }

                                Box(contentAlignment = Alignment.CenterEnd) {
                                    Text("100.0%", Modifier.alpha(0f), softWrap = false)
                                    state.progressText?.let {
                                        Text(it, softWrap = false)
                                    }
                                }
                            }
                        }
                    }
                }

                if (!state.isFinished) {
                    Crossfade(state.isProgressUnspecified) {
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
            }
        },
        trailingContent = {
            Box {
                if (state.isActionInProgress) {
                    IconButton(
                        onClick = {
                            // no-op
                        },
                    ) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    }
                } else {
                    when {
                        state.isFinished -> {
                            IconButton({ showDropdown = true }) {
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
                Dropdown(
                    showDropdown, { showDropdown = false },
                    state,
                )
            }
        },
    )
}

@Composable
private fun Dropdown(
    showDropdown: Boolean,
    onDismissRequest: () -> Unit,
    state: CacheEpisodeState,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(showDropdown, onDismissRequest, modifier) {
        if (!state.isFinished) {
            if (state.isPaused) {
                DropdownMenuItem(
                    text = { Text("继续下载") },
                    leadingIcon = { Icon(Icons.Rounded.Restore, null) },
                    onClick = {
                        state.resume()
                        onDismissRequest()
                    },
                )
            } else {
                DropdownMenuItem(
                    text = { Text("暂停下载") },
                    leadingIcon = { Icon(Icons.Rounded.Pause, null) },
                    onClick = {
                        state.pause()
                        onDismissRequest()
                    },
                )
            }
        }
        if (state.hasValidSubjectAndEpisodeId) {
            DropdownMenuItem(
                text = { Text("播放") },
                leadingIcon = {
                    // 这个内容如果太大会导致影响 text
                    Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                        // 这个图标比其他图标小
                        Icon(Icons.Rounded.PlayArrow, null, Modifier.requiredSize(28.dp))
                    }
                },
                onClick = {
                    state.play()
                    onDismissRequest()
                },
            )
        }
        ProvideContentColor(MaterialTheme.colorScheme.error) {
            DropdownMenuItem(
                text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
                onClick = {
                    state.delete()
                    onDismissRequest()
                },
            )
        }
    }
}
