@file:Suppress("PropertyName")

package me.him188.ani.app.ui.subject.cache

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.data.models.MediaSelectorSettings
import me.him188.ani.app.ui.foundation.animation.animateEnable
import me.him188.ani.app.ui.foundation.theme.stronglyWeaken
import me.him188.ani.app.ui.foundation.widgets.AnimatedLinearProgressIndicator
import me.him188.ani.app.ui.foundation.widgets.ProgressIndicatorHeight
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.TextItem
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorView
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSourceResultsView
import me.him188.ani.app.ui.subject.episode.mediaFetch.rememberMediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.rememberMediaSourceResultsPresentation
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.api.topic.isDoneOrDropped
import kotlin.time.Duration.Companion.seconds


@Immutable
data class EpisodeCacheInfo(
    val sort: EpisodeSort,
    val ep: EpisodeSort?,
    val title: String,
    val watchStatus: UnifiedCollectionType,
    /**
     * 是否已经上映了
     */
    val hasPublished: Boolean,
    val _placeholder: Int = 0,
) {
    val sortString = sort.toString()

    companion object {
        @Stable
        val Placeholder = EpisodeCacheInfo(
            EpisodeSort(0),
            null,
            "",
            UnifiedCollectionType.DONE,
            false,
            -1,
        )
    }
}

/**
 * 一个条目的所有剧集的缓存管理
 */
@Composable
fun SettingsScope.EpisodeCacheListGroup(
    state: EpisodeCacheListState,
    mediaSelectorSettingsProvider: () -> Flow<MediaSelectorSettings>,
    modifier: Modifier = Modifier,
) {
    state.currentSelectStorageTask?.let { task ->
        SelectMediaStorageDialog(
            options = task.options,
            onSelect = { state.selectStorage(it) },
            onDismissRequest = { state.cancelStorageSelector() },
            modifier
        )
    }
    state.currentSelectMediaTask?.let { task ->
        ModalBottomSheet(
            { state.cancelMediaSelector() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = modifier,
        ) {
            val selectorPresentation = rememberMediaSelectorPresentation { task.mediaSelector }
            MediaSelectorView(
                selectorPresentation,
                sourceResults = {
                    MediaSourceResultsView(
                        rememberMediaSourceResultsPresentation(
                            mediaSourceResults = { flowOf(task.fetchSession.mediaSourceResults) },
                            settings = mediaSelectorSettingsProvider,
                        ),
                        selectorPresentation,
                    )
                },
                onClickItem = {
                    state.selectMedia(it)
                },
                modifier = modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                    .navigationBarsPadding()
                    .fillMaxHeight() // 防止添加筛选后数量变少导致 bottom sheet 高度变化
                    .fillMaxWidth(),
                actions = {
                    TextButton({ state.cancelMediaSelector() }) {
                        Text("取消")
                    }
                },
            )
        }
    }

    Group(
        title = { Text("单集缓存") },
        modifier = modifier,
    ) {
        state.episodes.fastForEachIndexed { i, episodeCacheState ->
            var showDropdown by remember { mutableStateOf(false) }

            EpisodeItem(
                episodeCacheState,
                onClick = {
                    if (episodeCacheState.cacheStatus is EpisodeCacheStatus.Caching ||
                        episodeCacheState.cacheStatus is EpisodeCacheStatus.Cached
                    ) {
                        showDropdown = true
                    } else {
                        state.requestCache(episodeCacheState)
                    }
                },
                action = {
                    ItemDropdown(
                        showDropdown = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        onDeleteCache = { state.deleteCache(it) },
                        episodeCacheState = episodeCacheState
                    )
                }
            )

            // by m3 spec, progress indicator is 4dp
            Box(Modifier.height(ProgressIndicatorHeight), contentAlignment = Alignment.Center) {
                if (i != state.episodes.lastIndex) {
                    HorizontalDividerItem() // 1.dp height
                }
                AnimatedLinearProgressIndicator(
                    episodeCacheState.showProgressIndicator,
                    Modifier.zIndex(1f).fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ItemDropdown(
    showDropdown: Boolean,
    onDismissRequest: () -> Unit,
    onDeleteCache: (EpisodeCacheState) -> Unit,
    episodeCacheState: EpisodeCacheState
) {
    DropdownMenu(
        showDropdown,
        onDismissRequest
    ) {
        DropdownMenuItem(
            onClick = {
                onDeleteCache(episodeCacheState)
                onDismissRequest()
            },
            text = {
                Text("删除")
            },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Delete,
                    null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}


@Composable
private fun SettingsScope.EpisodeItem(
    episode: EpisodeCacheState,
    action: @Composable () -> Unit = {},
    onClick: () -> Unit,
) {
    val colorByWatchStatus = if (episode.info.watchStatus.isDoneOrDropped() || !episode.info.hasPublished) {
        LocalContentColor.current.stronglyWeaken()
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    TextItem(
        icon = {
            CompositionLocalProvider(LocalContentColor provides colorByWatchStatus) {
                Text(episode.info.sortString)
            }
        },
        action = {
            if (!episode.info.hasPublished) {
                CompositionLocalProvider(LocalContentColor provides colorByWatchStatus) {
                    Text("未开播")
                }
                return@TextItem
            }

            CompositionLocalProvider(LocalContentColor provides colorByWatchStatus) {
                Crossfade(episode.actionTasker.isRunning) { hasActionRunning ->
                    if (hasActionRunning) {
                        // 等一会再显示取消, 防止点错
                        var enabled by remember { mutableStateOf(false) }
                        LaunchedEffect(true) {
                            delay(1.seconds)
                            enabled = true
                        }
                        IconButton(
                            onClick = {
                                episode.actionTasker.cancel()
                            },
                            Modifier.animateEnable(enabled),
                            enabled = enabled
                        ) {
                            Icon(Icons.Rounded.Close, "取消")
                        }
                    } else {
                        EpisodeActionIcon(episode, onClick)
                    }
                }
                action()
            }
        },
        title = {
            CompositionLocalProvider(LocalContentColor provides colorByWatchStatus) {
                Text(episode.info.title, Modifier.weight(1f), overflow = TextOverflow.Ellipsis)

                when (episode.info.watchStatus) {
                    UnifiedCollectionType.DONE -> {
                        Label(Modifier.padding(start = 8.dp)) {
                            Text("看过")
                        }
                    }

                    UnifiedCollectionType.DROPPED -> {
                        Label(Modifier.padding(start = 8.dp)) {
                            Text("抛弃")
                        }
                    }

                    else -> {}
                }
            }
        },
    )
}

@Composable
private fun EpisodeActionIcon(
    episode: EpisodeCacheState,
    onClick: () -> Unit
) {
    when (val status = episode.cacheStatus) {
        is EpisodeCacheStatus.Cached ->
            IconButton(onClick) {
                Icon(Icons.Rounded.DownloadDone, null)
            }

        is EpisodeCacheStatus.Caching -> {
            Box(Modifier.clickable(onClick = onClick)) {
                val progressIsNull by remember {
                    derivedStateOf {
                        status.progress == null
                    }
                }
                if (progressIsNull) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    CircularProgressIndicator(
                        progress = { status.progress ?: 0f },
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }

        EpisodeCacheStatus.NotCached -> {
            if (episode.canCache) {
                CompositionLocalProvider(LocalContentColor providesDefault MaterialTheme.colorScheme.primary) {
                    IconButton(onClick) {
                        Icon(Icons.Rounded.Download, "缓存")
                    }
                }
            }
        }

        null -> {}
    }
}

@Composable
private fun Label(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(4.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier.border(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
            shape = MaterialTheme.shapes.small
        )
    ) {
        Box(Modifier.padding(contentPadding)) {
            ProvideTextStyle(MaterialTheme.typography.labelSmall) {
                content()
            }
        }
    }
}
