package me.him188.ani.app.ui.subject.cache

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.ArrowOutward
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
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.ui.foundation.theme.stronglyWeaken
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.components.RowButtonItem
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SliderItem
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.settings.framework.components.TextItem
import me.him188.ani.app.ui.settings.tabs.media.autoCacheDescription
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.api.topic.isDoneOrDropped

@Immutable
data class EpisodeCacheState(
    val episodeId: Int,
    val sort: EpisodeSort,
    val ep: EpisodeSort?,
    val title: String,
    val watchStatus: UnifiedCollectionType,
    val cacheStatus: EpisodeCacheStatus?,
    /**
     * 是否已经上映了
     */
    val hasPublished: Boolean,
    /**
     * 该缓存来自的 [Media].
     */
    val originMedia: Media? = null,
) {
    /**
     * 该缓存来自的 [Media] 的包含的所有剧集范围. 例如当前缓存可能只是第二集, 但这个 [Media] 是一个包含 1-12 集的季度全集资源.
     */
    val mediaEpisodeRange: EpisodeRange? get() = originMedia?.episodeRange
}

@Stable
inline val EpisodeCacheState.mediaCache get() = cacheStatus?.cache

@Stable
class SubjectCacheState(
    /**
     * List of episodes in this subject.
     */
    val episodes: List<EpisodeCacheState>
) {
    /**
     * 用户请求缓存这个剧集
     */
    var selectedEpisode: EpisodeCacheState? by mutableStateOf(null)
}

/**
 * 一个番剧的缓存设置页面, 包括自动缓存设置和手动单集缓存管理.
 *
 * @param mediaSelector 当用户点击缓存按钮时, 显示的视频源选择器. See [EpisodeCacheMediaSelector]
 */
@Composable
fun SubjectCachePage(
    state: SubjectCacheState,
    subjectTitle: @Composable () -> Unit,
    onClickGlobalCacheSettings: () -> Unit,
    onClickGlobalCacheManage: () -> Unit,
    onDeleteCache: (EpisodeCacheState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("缓存管理")
                },
                navigationIcon = {
                    TopAppBarGoBackButton()
                },
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            Surface(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                    ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                        subjectTitle()
                    }
                }
            }

            SettingsTab {
                Spacer(Modifier.fillMaxWidth()) // tab has spacedBy arrangement

                AutoCacheGroup(onClickGlobalCacheSettings, onClickGlobalCacheManage)

                Group(
                    title = { Text("单集缓存") },
//                    description = {
//                        Text("忽略全局限制")
//                    },
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
                                    state.selectedEpisode = episodeCacheState
                                }
                            },
                            action = {
                                DropdownMenu(
                                    showDropdown,
                                    { showDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        onClick = {
                                            onDeleteCache(episodeCacheState)
                                            showDropdown = false
                                        },
                                        text = {
                                            Text("删除")
                                        },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Rounded.Delete,
                                                null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                }
                            }
                        )
                        if (i != state.episodes.lastIndex) {
                            HorizontalDividerItem()
                        }
                    }
                }
                Spacer(Modifier.fillMaxWidth()) // tab has spacedBy arrangement
            }
        }
    }
}

@Composable
private fun SettingsScope.AutoCacheGroup(
    onClickGlobalCacheSettings: () -> Unit,
    onClickGlobalCacheManage: () -> Unit,
) {
    Group(
        title = { Text("自动缓存") },
        description = {
            Text("自动缓存未观看的剧集")
        }
    ) {
        var useGlobalSettings by remember { mutableStateOf(true) }
        SwitchItem(
            title = { Text("使用全局设置") },
            description = { Text("关闭后可为该番剧单独设置 (暂不支持单独设置)") },
            checked = useGlobalSettings,
            onCheckedChange = { useGlobalSettings = !useGlobalSettings },
            enabled = false,
        )

        AnimatedVisibility(!useGlobalSettings) {
            var sliderValue by remember { mutableFloatStateOf(0f) }
            SliderItem(
                title = { Text("最大自动缓存话数") },
                description = {
                    Row {
                        Text(remember(sliderValue) { autoCacheDescription(sliderValue) })
                        if (sliderValue == 10f) {
                            Text("可能会占用大量空间", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            ) {
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    enabled = !useGlobalSettings,
                )
            }
            HorizontalDividerItem()
        }

        AnimatedVisibility(useGlobalSettings) {
            RowButtonItem(
                onClick = onClickGlobalCacheSettings,
                icon = { Icon(Icons.Rounded.ArrowOutward, null) },
            ) { Text("查看全局设置") }
        }

        RowButtonItem(
            onClick = onClickGlobalCacheManage,
            icon = { Icon(Icons.AutoMirrored.Rounded.ViewList, null) },
        ) { Text("管理全部缓存") }
    }
}

@Composable
private fun SettingsScope.EpisodeItem(
    episode: EpisodeCacheState,
    action: @Composable () -> Unit = {},
    onClick: () -> Unit,
) {
    val colorByWatchStatus = if (episode.watchStatus.isDoneOrDropped() || !episode.hasPublished) {
        LocalContentColor.current.stronglyWeaken()
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    TextItem(
        icon = {
            CompositionLocalProvider(LocalContentColor provides colorByWatchStatus) {
                Text(episode.sort.toString())
            }
        },
        action = {
            if (!episode.hasPublished) {
                CompositionLocalProvider(LocalContentColor provides colorByWatchStatus) {
                    Text("未开播")
                }
                return@TextItem
            }

            CompositionLocalProvider(LocalContentColor provides colorByWatchStatus) {
                when (val status = episode.cacheStatus) {
                    is EpisodeCacheStatus.Cached ->
                        IconButton(onClick) {
                            Icon(Icons.Rounded.DownloadDone, null)
                        }

                    is EpisodeCacheStatus.Caching -> {
                        Box(Modifier.clickable(onClick = onClick)) {
                            val progress = status.progress
                            if (progress == null) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                )
                            } else {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                        }
                    }

                    EpisodeCacheStatus.NotCached -> {
                        if (!episode.watchStatus.isDoneOrDropped()) {
                            CompositionLocalProvider(LocalContentColor providesDefault MaterialTheme.colorScheme.primary) {
                                IconButton(onClick) {
                                    Icon(Icons.Rounded.Download, "缓存")
                                }
                            }
                        }
                    }

                    null -> {}
                }
                action()
            }
        },
        title = {
            CompositionLocalProvider(LocalContentColor provides colorByWatchStatus) {
                Text(episode.title, Modifier.weight(1f), overflow = TextOverflow.Ellipsis)

                when (episode.watchStatus) {
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