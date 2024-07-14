package me.him188.ani.app.ui.subject.episode.details

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.models.episode.EpisodeCollection
import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.data.models.episode.episode
import me.him188.ani.app.data.source.media.EpisodeCacheStatus
import me.him188.ani.app.data.source.media.isCachedOrCaching
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.icons.PlayingIcon
import me.him188.ani.app.ui.foundation.text.ProvideContentColor
import me.him188.ani.app.ui.foundation.text.toPercentageString
import me.him188.ani.app.ui.subject.details.components.OutlinedTag
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import kotlin.math.absoluteValue


@Stable
class EpisodeCarouselState(
    episodes: State<List<EpisodeCollection>>,
    playingEpisode: State<EpisodeCollection?>,
    private val cacheStatus: (EpisodeCollection) -> EpisodeCacheStatus,
    val onSelect: (EpisodeCollection) -> Unit,
    val onChangeCollectionType: suspend (episode: EpisodeCollection, UnifiedCollectionType) -> Unit,
    val lazyListState: LazyListState = LazyListState(),
    backgroundScope: CoroutineScope,
) {
    val episodes by episodes
    val playingEpisode by playingEpisode

    val size get() = episodes.size

    suspend fun animateScrollToItem(index: Int) {
        lazyListState.animateScrollToItem(
            index,
            scrollOffset = -calculateItemSize(),
        )
    }

    private fun calculateItemSize(): Int {
        val info = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull() ?: return 0
        return info.size.times(0.2f).toInt()
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
 * [LazyRow] 的剧集列表
 */
@Composable
fun EpisodeCarousel(
    state: EpisodeCarouselState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    collectionButtonEnabled: Boolean = !state.isSettingCollectionType,
) {
    LaunchedEffect(state.playingEpisode) {
        val index = state.episodes.indexOf(state.playingEpisode)
        if (index == -1) return@LaunchedEffect
        state.animateScrollToItem(
            state.episodes.indexOf(state.playingEpisode),
        )
    }

    LazyRow(
        modifier,
        state = state.lazyListState,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(state.size) { index ->
            val episode = state.getEpisode(index)
            if (episode != null) {
                EpisodeCarouselItem(
                    episode = episode.episode,
                    onClick = { state.onSelect(episode) },
                    isPlaying = { state.isPlaying(episode) },
                    cacheStatus = { state.cacheStatus(episode) },
                    collectionButton = {
                        EpisodeCollectionIconButton(
                            type = episode.collectionType,
                            onChange = { state.setCollectionType(episode, it) },
                            enabled = collectionButtonEnabled,
                        )
                    },
                    Modifier.widthIn(max = 240.dp).wrapContentWidth(),
                )
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
fun EpisodeCarouselItem(
    episode: EpisodeInfo,
    onClick: () -> Unit,
    isPlaying: () -> Boolean,
    cacheStatus: () -> EpisodeCacheStatus,
    collectionButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick,
        modifier,
    ) {
        Column(
            Modifier.padding(horizontal = 16.dp)
                .padding(top = 4.dp)
                .padding(bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                        OutlinedTag(contentPadding = PaddingValues(horizontal = 6.dp, vertical = 3.dp)) {
                            Text(
                                episode.sort.toString(),
                            )
                        }
                    }

                    var showOriginalName by rememberSaveable {
                        mutableStateOf(false)
                    }
                    ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                        SelectionContainer {
                            Text(
                                text = renderEpisodeName(
                                    if (showOriginalName) episode.name else episode.nameCn,
                                    episode.sort,
                                ),
                                Modifier.clickable(onClick = { showOriginalName = !showOriginalName }).weight(1f),
                            )
                        }
                    }
                }
                collectionButton()
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                    Row(
                        Modifier.weight(1f).height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        ) {
//                            Icon(Icons.Rounded.Event, contentDescription = "评论数量")
//                            Text(
//                                if (episode.airDate.isInvalid) "TBA" else episode.airDate.toString(),
//                                softWrap = false,
//                            )
//                        }
//                        VerticalDivider(Modifier.padding(vertical = 2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = "评论数量")
                            Text(episode.comment.toString(), softWrap = false)
                        }

                        val cacheStatusState by remember(cacheStatus) { derivedStateOf(cacheStatus) }
                        val isCachedOrCaching by remember {
                            derivedStateOf {
                                cacheStatusState.isCachedOrCaching()
                            }
                        }
                        val cacheProgress by remember {
                            derivedStateOf {
                                val s = cacheStatusState
                                if (s is EpisodeCacheStatus.Caching) {
                                    s.progress
                                } else {
                                    0f
                                }
                            }
                        }

                        if (isCachedOrCaching) {
                            VerticalDivider(Modifier.padding(vertical = 2.dp))

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
                                                cacheProgress?.toPercentageString() ?: "正在缓存"
                                            }
                                        }
                                        Text(text, softWrap = false)
                                    }

                                    EpisodeCacheStatus.NotCached -> {}
                                }
                            }
                        }
                    }


                    val isPlayingState by remember(isPlaying) { derivedStateOf(isPlaying) }
                    ProvideContentColor(MaterialTheme.colorScheme.primary) {
                        if (isPlayingState) {
                            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                PlayingIcon("正在播放")
                            }
                        } else {
                            Spacer(Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun renderEpisodeName(
    name: String,
    sort: EpisodeSort,
): String {
    return name.ifEmpty { "第 $sort 话" }
}

/**
 * 剧集的观看状态, 点击可以修改
 */
@Composable
fun EpisodeCollectionIconButton(
    type: UnifiedCollectionType,
    onChange: (new: UnifiedCollectionType) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val typeState by rememberUpdatedState(type)
    val isWatched by remember {
        derivedStateOf {
            typeState == UnifiedCollectionType.DONE
        }
    }

    Box(modifier.animateContentSize(), contentAlignment = Alignment.Center) {
        if (isWatched) {
            IconButton(
                { onChange(UnifiedCollectionType.NOT_COLLECTED) },
                enabled = enabled,
            ) {
                Icon(Icons.Rounded.TaskAlt, contentDescription = "已看过")
            }
        } else {
            IconButton(
                { onChange(UnifiedCollectionType.DONE) },
                enabled = enabled,
            ) {
                Icon(Icons.Rounded.AddTask, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

fun Modifier.carouselTransition(page: Int, pagerState: PagerState) =
    graphicsLayer {
        val pageOffset =
            ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

        val transformation =
            lerp(
                start = 0.7f,
                stop = 1f,
                fraction = 1f - pageOffset.coerceIn(0f, 1f),
            )
        alpha = transformation
        scaleY = transformation
    }