package me.him188.ani.app.ui.collection

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.debounce
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.data.subject.SubjectCollectionItem
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.ui.foundation.indication.HorizontalIndicator
import me.him188.ani.app.ui.foundation.indication.IndicatedBox
import me.him188.ani.app.ui.subject.details.COVER_WIDTH_TO_HEIGHT_RATIO
import me.him188.ani.app.ui.subject.details.Tag
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.openapitools.client.models.UserEpisodeCollection
import kotlin.time.Duration.Companion.seconds

/**
 * Lazy column of [item]s, designed for My Collections.
 *
 * @param item composes each item. See [SubjectCollectionItem]
 * @param onEmpty content to be displayed when [LazyDataCache.cachedDataFlow] is empty.
 * @param contentPadding 要求该 column 的内容必须保持的 padding.
 * [SubjectCollectionsColumn] 将会允许元素渲染到这些区域, 但会在列表首尾添加 padding.
 * 这样可以让列表渲染到 bottom bar 的下面 (bottom bar 设置 alpha 0.97), 而用户又能正常地滑动到列表尾部并完整显示最后一个元素.
 */
@Composable
fun SubjectCollectionsColumn(
    cache: LazyDataCache<SubjectCollectionItem>,
    onRequestMore: () -> Unit,
    item: @Composable (item: SubjectCollectionItem) -> Unit,
    onEmpty: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val spacedBy = 16.dp

    val data = cache.cachedDataFlow.collectAsState(null).value // 反正下面会立即用到, recompose 总是重组整个函数

    // 如果不 debounce, 会导致刚刚加载完成后会显示一小会 "空空如也"
    val isCompleted by remember(cache) { cache.isCompleted.debounce(1.seconds) }.collectAsState(false)

    if (data.isNullOrEmpty() && isCompleted) {
        onEmpty()
        return
    }

    if (data != null) {
        LazyColumn(
            modifier.padding(horizontal = 12.dp).padding(vertical = 0.dp),
            lazyListState,
            verticalArrangement = Arrangement.spacedBy(spacedBy),
        ) {
            // 每两个 item 之间有 spacedBy dp, 这里再上补充 contentPadding 要求的高度, 这样顶部的总留空就是 contentPadding 要求的高度
            // 这个高度是可以滚到上面的, 所以它
            (contentPadding.calculateTopPadding() - spacedBy).coerceAtLeast(0.dp).let {
                item { Spacer(Modifier.height(it)) }
            }


            items(data, key = { it.subjectId }) { collection ->
                // 在首次加载时展示一个渐入的动画, 随后不再展示
                var targetAlpha by rememberSaveable { mutableStateOf(0f) }
                val alpha by animateFloatAsState(
                    targetAlpha,
                    if (lazyListState.canScrollBackward || lazyListState.canScrollForward) {
                        snap(0)
                    } else tween(150)
                )
                Box(
                    Modifier.then(
                        if (LocalIsPreviewing.current) { // 预览模式下无动画
                            Modifier
                        } else {
                            // improves performance than `.alpha()`
                            Modifier.graphicsLayer {
                                this.alpha = alpha
                            }
                        }
                    ).animateItemPlacement()
                ) {
                    item(collection)
                }
                SideEffect {
                    targetAlpha = 1f
                }
            }

            if (!isCompleted) {
                item("dummy loader") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }

                    LaunchedEffect(true) {
                        onRequestMore()
                    }
                }
            }

            (contentPadding.calculateBottomPadding() - spacedBy).coerceAtLeast(0.dp).let {
                item { Spacer(Modifier.height(it)) }
            }
        }
    }
}


/**
 * 追番列表的一个条目卡片
 *
 * @param onClick on clicking this card (background)
 */
@Composable
fun SubjectCollectionItem(
    item: SubjectCollectionItem,
    episodeCacheStatus: @Composable (subjectId: Int, episodeId: Int) -> EpisodeCacheStatus?,
    onClick: () -> Unit,
    onClickEpisode: (episode: UserEpisodeCollection) -> Unit,
    onClickSelectEpisode: () -> Unit,
    onSetAllEpisodesDone: () -> Unit,
    onSetCollectionType: (new: UnifiedCollectionType) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 148.dp,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    doneButton: @Composable (() -> Unit)? = null,
) {
    Card(
        onClick,
        modifier.clip(shape).fillMaxWidth().height(height),
        shape = shape,
    ) {
        Row(Modifier.weight(1f, fill = false)) {
            AsyncImage(
                item.image,
                contentDescription = null,
                modifier = Modifier
                    .height(height).width(height * COVER_WIDTH_TO_HEIGHT_RATIO),
            )

            Box(Modifier.weight(1f)) {
                SubjectCollectionItemContent(
                    item,
                    episodeCacheStatus,
                    onClickEpisode,
                    onClickSelectEpisode,
                    onSetAllEpisodesDone,
                    onSetCollectionType,
                    Modifier.padding(start = 12.dp).fillMaxSize(),
                    doneButton = doneButton,
                )
            }
        }
    }
}

/**
 * 追番列表的一个条目卡片的内容
 */
@Composable
private fun SubjectCollectionItemContent(
    item: SubjectCollectionItem,
    cacheStatus: @Composable (subjectId: Int, episodeId: Int) -> EpisodeCacheStatus?,
    onClickEpisode: (episode: UserEpisodeCollection) -> Unit,
    onClickSelectEpisode: () -> Unit,
    onSetAllEpisodesDone: (() -> Unit)?,
    onSetCollectionType: (new: UnifiedCollectionType) -> Unit,
    modifier: Modifier = Modifier,
    doneButton: @Composable (() -> Unit)? = null,
) {
    Column(modifier) {
        // 标题和右上角菜单
        Row(
            Modifier.fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                item.displayName,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Box {
                var showDropdown by rememberSaveable { mutableStateOf(false) }
                IconButton({ showDropdown = true }, Modifier.fillMaxHeight().padding()) {
                    Icon(Icons.Outlined.MoreVert, null, Modifier.size(24.dp))
                }

                EditCollectionTypeDropDown(
                    currentType = item.collectionType,
                    showDropdown, { showDropdown = false },
                    onSetAllEpisodesDone = onSetAllEpisodesDone,
                    onClick = { action ->
                        onSetCollectionType(action.type)
                    }
                )
            }
        }

        Row(
            Modifier.padding(top = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                // 2023 年 10 月
                item.date?.let {
                    Tag { Text(item.date) }
                }

                // 连载至第 28 话 · 全 34 话
                OnAirLabel(item, Modifier.padding(start = 8.dp))
            }
        }

        Spacer(Modifier.weight(1f))


        Row(
            Modifier
                .padding(vertical = 12.dp)
                .padding(horizontal = 12.dp)
                .align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClickSelectEpisode) {
                Text("选集")
            }

            val onPlay: () -> Unit = {
                getEpisodeToPlay(item)?.let(onClickEpisode)
            }
            IndicatedBox(indicator = {
                getEpisodeToPlay(item)?.episode?.id?.let { episodeId ->
                    HorizontalIndicator(
                        6.dp,
                        CircleShape,
                        cacheStatusIndicationColor(
                            cacheStatus(
                                item.subjectId,
                                episodeId
                            ),
                            item.continueWatchingStatus is ContinueWatchingStatus.Watched
                        ),
                        Modifier.offset(y = (-2).dp)
                    )
                }
            }) {
                when (val status = item.continueWatchingStatus) {
                    is ContinueWatchingStatus.Continue -> {
                        Button(onClick = onPlay) {
                            Text("继续观看 ${status.episodeSort}")
                        }
                    }

                    ContinueWatchingStatus.Done -> {
                        doneButton?.invoke()
                    }

                    ContinueWatchingStatus.NotOnAir -> {
                        androidx.compose.material3.FilledTonalButton(onClick = onPlay) {
                            Text("还未开播")
                        }
                    }

                    ContinueWatchingStatus.Start -> {
                        Button(onClick = onPlay) {
                            Text("开始观看")
                        }
                    }

                    is ContinueWatchingStatus.Watched -> {
                        androidx.compose.material3.FilledTonalButton(onClick = onPlay) {
                            Text("看到 ${status.episodeSort}")
                        }
                    }
                }
            }
        }
    }
}

private fun getEpisodeToPlay(
    item: SubjectCollectionItem,
): UserEpisodeCollection? {
    if (item.continueWatchingStatus is ContinueWatchingStatus.Watched) {
        return item._episodes[item.continueWatchingStatus.episodeIndex]
    } else {
        item.lastWatchedEpIndex?.let {
            item._episodes.getOrNull(it + 1)
        }?.let {
            return it
        }

        item.lastWatchedEpIndex?.let {
            item._episodes.getOrNull(it)
        }?.let {
            return it
        }

        item._episodes.firstOrNull()?.let {
            return it
        }
    }

    return null
}


// The label "已完结 · 全 28 话"
@Composable
private fun OnAirLabel(
    item: SubjectCollectionItem,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium,
) {
    ProvideTextStyle(style) {
        Row(modifier.width(IntrinsicSize.Max).height(IntrinsicSize.Min)) {
            Text(
                item.onAirDescription,
                color = if (item.isOnAir) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                maxLines = 1,
            )
            Text(
                " · ",
                maxLines = 1,
            )
            Text(
                item.serialProgress,
                maxLines = 1,
            )
        }
    }
}