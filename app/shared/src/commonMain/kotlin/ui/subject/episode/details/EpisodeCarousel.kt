package me.him188.ani.app.ui.subject.episode.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.models.episode.EpisodeCollection
import me.him188.ani.app.data.models.episode.episode
import me.him188.ani.app.data.models.episode.type
import me.him188.ani.app.data.source.media.cache.EpisodeCacheStatus
import me.him188.ani.app.data.source.media.cache.isCachedOrCaching
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.tools.toPercentageOrZero
import me.him188.ani.app.tools.toProgress
import me.him188.ani.app.ui.foundation.icons.PlayingIcon
import me.him188.ani.app.ui.subject.episode.details.components.EpisodeWatchStatusButton
import me.him188.ani.app.ui.subject.episode.details.components.PlayingEpisodeItem
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.api.topic.isDoneOrDropped
import me.him188.ani.utils.platform.format1f


/**
 * 详细剧集列表的状态
 *
 * @see EpisodeCarousel
 */
@Stable
class EpisodeCarouselState(
    episodes: State<List<EpisodeCollection>>,
    playingEpisode: State<EpisodeCollection?>,
    private val cacheStatus: (EpisodeCollection) -> EpisodeCacheStatus,
    val onSelect: (EpisodeCollection) -> Unit,
    val onChangeCollectionType: suspend (episode: EpisodeCollection, UnifiedCollectionType) -> Unit,
    internal val gridState: LazyGridState = LazyGridState(),
    backgroundScope: CoroutineScope,
) {
    val episodes by episodes
    val playingEpisode by playingEpisode

    val size get() = episodes.size

    suspend fun animateScrollToItem(index: Int) {
        gridState.animateScrollToItem(
            index,
            scrollOffset = -calculateItemSize(),
        )
    }

    private fun calculateItemSize(): Int {
        val info = gridState.layoutInfo.visibleItemsInfo.firstOrNull() ?: return 0
        return info.size.height.times(0.2f).toInt()
    }

    @Stable
    internal fun getEpisode(index: Int) = episodes.getOrNull(index)

    @Stable
    internal fun isPlaying(episode: EpisodeCollection): Boolean {
        return playingEpisode == episode
    }

    @Stable
    internal fun cacheStatus(episode: EpisodeCollection): EpisodeCacheStatus {
        return this.cacheStatus.invoke(episode)
    }

    private val setCollectionTypeTasker = MonoTasker(backgroundScope)
    val isSettingCollectionType get() = setCollectionTypeTasker.isRunning
    fun setCollectionType(episode: EpisodeCollection, type: UnifiedCollectionType) {
        setCollectionTypeTasker.launch {
            onChangeCollectionType(episode, type)
        }
    }
}

/**
 * 详细剧集列表
 */
@Composable
fun EpisodeCarousel(
    state: EpisodeCarouselState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LaunchedEffect(state.playingEpisode) {
        val index = state.episodes.indexOf(state.playingEpisode)
        if (index == -1) return@LaunchedEffect
        state.animateScrollToItem(
            state.episodes.indexOf(state.playingEpisode),
        )
    }

    LazyVerticalGrid(
        GridCells.Adaptive(240.dp),
        modifier,
        state = state.gridState,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(state.size) { index ->
            val collection = state.getEpisode(index)
            if (collection != null) {
                val collectionUpdated by rememberUpdatedState(collection)
                val play = { state.onSelect(collectionUpdated) }
                OutlinedCard(
                    onClick = play,
                ) {
                    val isPlaying = state.isPlaying(collection)
                    PlayingEpisodeItem(
                        episodeSort = {
                            Text(
                                collection.episode.sort.toString(),
                                color = if (isPlaying) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                            )
                        },
                        title = {
                            Text(
                                collection.episode.nameCn.ifEmpty { "第 ${collection.episode.sort} 话" },
                                color = if (isPlaying) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                            )
                        },
                        watchStatus = {
                            EpisodeWatchStatusButton(
                                collection.type.isDoneOrDropped(),
                                onUnmark = {
                                    state.setCollectionType(collection, UnifiedCollectionType.NOT_COLLECTED)
                                },
                                onMarkAsDone = {
                                    state.setCollectionType(collection, UnifiedCollectionType.DONE)
                                },
                                enabled = !state.isSettingCollectionType,
                            )
                        },
                        mediaSelected = true,
                        mediaLabels = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = "评论数量")
                                Text(collection.episode.comment.toString(), softWrap = false)
                            }

                            EpisodeCacheStatusLabel(state, collectionUpdated)
                        },
                        filename = {},
                        videoLoadingSummary = {},
                        mediaSource = {},
                        playingIcon = {
                            if (isPlaying) {
                                PlayingIcon()
                            }
                        },
                    )
                }
            }
        }
    }
//    HorizontalPager(
//        state.pagerState,
//        contentPadding = PaddingValues(horizontal = 32.dp),
//        pageSpacing = 16.dp,
//    ) { page ->
//        state.getEpisode(page)?.let { episode ->
//            EpisodeCarouselItem(
//                episode = episode.episode,
//                onClick = { onPlay(episode) },
//                isPlaying = { state.isPlaying(episode) },
//                cacheStatus = { state.cacheStatus(episode) },
//                collectionButton = {
//                    EpisodeCollectionIconButton(
//                        type = episode.collectionType,
//                        onChange = onChangeCollectionType,
//                    )
//                },
//                Modifier.fillMaxWidth().carouselTransition(page, state.pagerState),
//            )
//        }
//    }
}

@Composable
private fun EpisodeCacheStatusLabel(
    state: EpisodeCarouselState,
    episode: EpisodeCollection,
) {
    val cacheStatusState by remember(state, episode) {
        derivedStateOf { state.cacheStatus(episode) }
    }
    val isCachedOrCaching by remember {
        derivedStateOf { cacheStatusState.isCachedOrCaching() }
    }

    if (isCachedOrCaching) {
        val cacheProgress by remember {
            derivedStateOf {
                val s = cacheStatusState
                if (s is EpisodeCacheStatus.Caching) {
                    s.progress
                } else {
                    0f.toProgress()
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (cacheStatusState) {
                is EpisodeCacheStatus.Cached -> {
                    Icon(Icons.Rounded.DownloadDone, contentDescription = null)
                    Text("已缓存", softWrap = false)
                }

                is EpisodeCacheStatus.Caching -> {
                    Icon(Icons.Rounded.Downloading, contentDescription = null)
                    val text by remember {
                        derivedStateOf {
                            String.format1f(cacheProgress.toPercentageOrZero()) + "%"
                        }
                    }
                    Text(text, softWrap = false)
                }

                EpisodeCacheStatus.NotCached -> {}
            }
        }
    }
}
