package me.him188.ani.app.ui.subject.cache

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.ui.preference.PreferenceScope
import me.him188.ani.app.ui.preference.PreferenceTab
import me.him188.ani.app.ui.preference.SwitchItem
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

@Immutable
class EpisodeCacheState(
    val id: Int,
    val sort: EpisodeSort,
    val title: String,
    val watchStatus: UnifiedCollectionType,
    val cacheStatus: EpisodeCacheStatus?,
)

@Stable
interface SubjectCacheState {
    /**
     * List of episodes in this subject.
     */
    val episodes: List<EpisodeCacheState>
}

class DefaultSubjectCacheState(override val episodes: List<EpisodeCacheState>) : SubjectCacheState

/**
 * Cache settings for one subject.
 */
@Composable
fun SubjectCachePage(
    state: SubjectCacheState,
    title: @Composable () -> Unit,
    onClickGlobalCacheSettings: () -> Unit,
    onClickEpisode: (EpisodeCacheState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(
                title = {
                    title()
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
                        Text("缓存设置")
                    }
                }
            }

            PreferenceTab(Modifier.padding(vertical = 16.dp)) {
                AutoCacheGroup(onClickGlobalCacheSettings)

                Group(
                    title = { Text("单集缓存") },
//                    description = {
//                        Text("忽略全局限制")
//                    },
                ) {
                    state.episodes.fastForEachIndexed { i, episodeCacheState ->
                        EpisodeItem(
                            episodeCacheState,
                            onClick = {
                                onClickEpisode(episodeCacheState)
                            }
                        )
                        if (i != state.episodes.lastIndex) {
                            HorizontalDividerItem()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceScope.AutoCacheGroup(onClickGlobalCacheSettings: () -> Unit) {
    Group(
        title = { Text("自动预缓存") },
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

        AnimatedVisibility(useGlobalSettings) {
            TextItem(
                title = { Text("查看全局设置") },
                icon = { Icon(Icons.Rounded.ArrowOutward, null) },
                onClick = onClickGlobalCacheSettings,
            )
        }

        AnimatedVisibility(!useGlobalSettings) {
            var sliderValue by remember { mutableFloatStateOf(0f) }
            SliderItem(
                title = { Text("最大预缓存话数") },
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
        }
    }
}

private fun autoCacheDescription(sliderValue: Float) = when (sliderValue) {
    0f -> "当前设置: 不自动缓存"
    10f -> "当前设置: 自动缓存全部未观看剧集, "
    else -> "当前设置: 自动缓存观看进度之后的 ${sliderValue.toInt()} 话, " +
            "预计占用 ${600.megaBytes * sliderValue}"
}

@Composable
private fun PreferenceScope.EpisodeItem(
    episode: EpisodeCacheState,
    onClick: () -> Unit,
) {
    TextItem(
        action = {
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
                    CompositionLocalProvider(LocalContentColor providesDefault MaterialTheme.colorScheme.primary) {
                        IconButton(onClick) {
                            Icon(Icons.Rounded.Download, "缓存")
                        }
                    }
                }

                null -> {}
            }
        },
        icon = {
            Text(episode.sort.toString())
        },
        title = {
            Text(episode.title)

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