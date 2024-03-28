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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import io.kamel.image.asyncPainterResource
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.ui.foundation.AniKamelImage
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.subject.details.COVER_WIDTH_TO_HEIGHT_RATIO
import me.him188.ani.app.ui.subject.details.Tag
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.UserEpisodeCollection

/**
 * 我的追番的番剧列表
 * @param contentPadding 要求该 column 的内容必须保持的 padding.
 * [MyCollectionColumn] 将会允许元素渲染到这些区域, 但会在列表首尾添加 padding.
 * 这样可以让列表渲染到 bottom bar 的下面 (bottom bar 设置 alpha 0.97), 而用户又能正常地滑动到列表尾部并完整显示最后一个元素.
 */
@Composable
fun MyCollectionColumn(
    collections: List<SubjectCollectionItem>,
    viewModel: MyCollectionsViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val spacedBy = 16.dp
    val state = rememberLazyListState()
    LazyColumn(
        modifier.padding(horizontal = 12.dp).padding(vertical = 0.dp),
        state,
        verticalArrangement = Arrangement.spacedBy(spacedBy),
    ) {
        // 每两个 item 之间有 spacedBy dp, 这里再上补充 contentPadding 要求的高度, 这样顶部的总留空就是 contentPadding 要求的高度
        // 这个高度是可以滚到上面的, 所以它
        (contentPadding.calculateTopPadding() - spacedBy).coerceAtLeast(0.dp).let {
            item { Spacer(Modifier.height(it)) }
        }

        items(collections, key = { it.subjectId }) { collection ->
            // 在首次加载时展示一个渐入的动画, 随后不再展示
            var targetAlpha by remember { mutableStateOf(0f) }
            val alpha by animateFloatAsState(
                targetAlpha,
                if (state.canScrollBackward || state.canScrollForward)
                    snap(0)
                else tween(150)
            )
            Box(
                Modifier.then(
                    if (LocalIsPreviewing.current) // 预览模式下无动画
                        Modifier
                    else
                        Modifier.alpha(alpha)
                ).animateItemPlacement()
            ) {
                val navigator = LocalNavigator.current
                CollectionItem(
                    collection,
                    onClick = {
                        navigator.navigateSubjectDetails(collection.subjectId)
                    },
                    onClickEpisode = {
                        navigator.navigateEpisodeDetails(collection.subjectId, it.episode.id)
                    },
                    onLongClickEpisode = {
                        viewModel.launchInBackground {
                            viewModel.setEpisodeWatched(
                                collection.subjectId,
                                it.episode.id,
                                it.type != EpisodeCollectionType.WATCHED
                            )
                        }
                    },
                    viewModel,
                )
            }
            SideEffect {
                targetAlpha = 1f
            }
        }

        (contentPadding.calculateBottomPadding() - spacedBy).coerceAtLeast(0.dp).let {
            item { Spacer(Modifier.height(it)) }
        }
    }
}


/**
 * 追番列表的一个条目卡片
 */
@Composable
private fun CollectionItem(
    item: SubjectCollectionItem,
    onClick: () -> Unit,
    onClickEpisode: (episode: UserEpisodeCollection) -> Unit,
    onLongClickEpisode: (episode: UserEpisodeCollection) -> Unit,
    viewModel: MyCollectionsViewModel,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(8.dp)
    val height = 148.dp
    Card(
        onClick,
        modifier.clip(cardShape).fillMaxWidth().height(height),
        shape = cardShape,
    ) {
        Row(Modifier.weight(1f, fill = false)) {
            AniKamelImage(
                asyncPainterResource(item.image),
                modifier = Modifier
                    .height(height).width(height * COVER_WIDTH_TO_HEIGHT_RATIO),
                contentDescription = null,
            )

            Box(Modifier.weight(1f)) {
                CollectionItemContent(
                    item,
                    onClickEpisode,
                    onLongClickEpisode,
                    viewModel,
                    Modifier.fillMaxSize()
                        .padding(start = 16.dp),
                )

                Box(Modifier.padding(all = 15.dp).align(Alignment.TopEnd)) {

//                    FilledTonalButton(
//                        onClick = { },
//                        Modifier.width(64.dp).height(32.dp),
//                        colors = ButtonDefaults.filledTonalButtonColors(
//                            containerColor = MaterialTheme.colorScheme.secondary,
//                            contentColor = MaterialTheme.colorScheme.onSecondary,
//                        ),
//                        shape = RoundedCornerShape(bottomStart = 4.dp),
//                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
//                    ) {
//                    }
                }
            }
        }
    }
}

/**
 * 追番列表的一个条目卡片的内容
 */
@Composable
private fun CollectionItemContent(
    item: SubjectCollectionItem,
    onClickEpisode: (episode: UserEpisodeCollection) -> Unit,
    onLongClickEpisode: (episode: UserEpisodeCollection) -> Unit,
    viewModel: MyCollectionsViewModel,
    modifier: Modifier = Modifier,
) {
    val onClickEpisodeState by rememberUpdatedState(onClickEpisode)
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
                var showDropdown by remember { mutableStateOf(false) }
                IconButton({ showDropdown = true }, Modifier.fillMaxHeight().padding()) {
                    Icon(Icons.Outlined.MoreVert, null, Modifier.size(24.dp))
                }

                EditCollectionTypeDropDown(
                    currentType = item.collectionType,
                    showDropdown, { showDropdown = false },
                    onSetAllEpisodesDone = {
                        viewModel.launchInBackground { viewModel.setAllEpisodesWatched(item.subjectId) }
                    },
                    onClick = { action ->
                        viewModel.launchInBackground { updateSubjectCollection(item.subjectId, action) }
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

        var showEpisodeProgressDialog by remember { mutableStateOf(false) }
        if (showEpisodeProgressDialog) {
            val navigator = LocalNavigator.current
            EpisodeProgressDialog(
                onDismissRequest = { showEpisodeProgressDialog = false },
                onClickDetails = { navigator.navigateSubjectDetails(item.subjectId) },
                title = { Text(text = item.displayName) },
            ) {
                EpisodeProgressRow(
                    item = item,
                    onClickEpisodeState = {},
                    onLongClickEpisode = {}
                )
            }
        }

        Row(
            Modifier
                .padding(vertical = 16.dp)
                .padding(horizontal = 16.dp)
                .align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton({ showEpisodeProgressDialog = true }) {
                Text("选集")
            }

            val onPlay: () -> Unit = {
                item.lastWatchedEpIndex?.let {
                    item.episodes.getOrNull(it)
                }?.let {
                    onClickEpisode(it)
                }
            }
            when {
                // 还没看过
                item.lastWatchedEpIndex == null -> {
                    me.him188.ani.app.ui.foundation.Button(
                        onClick = onPlay,
                    ) {
                        Text("开始观看")
                    }
                }

                // 看了第 n 集并且还有第 n+1 集
                item.lastWatchedEpIndex in item.episodes.indices
                        && item.latestEpIndex != null
                        && item.lastWatchedEpIndex < item.latestEpIndex -> {
                    me.him188.ani.app.ui.foundation.Button(
                        onClick = onPlay,
                    ) {
                        Text("继续观看 ${item.lastWatchedEpIndex + 2}")
                    }
                }

                else -> {
                    Button(
                        {},
                        enabled = false
                    ) {
                        Text("已看完", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

//        // 剧集列表
//        EpisodeProgressRow(
//            item, onClickEpisodeState,
//            onLongClickEpisode,
//            Modifier.padding(top = 4.dp).weight(1f)
//        )

//                val tags = item.subject?.tags
//
//                Box(Modifier.height(28.dp).clip(RectangleShape)) {
//                    FlowRow(
//                        Modifier.padding(top = 8.dp),
//                        horizontalArrangement = Arrangement.spacedBy(3.dp),
//                    ) {
//                        for (tag in tags.orEmpty()) {
//                            Tag { Text(tag.name, style = MaterialTheme.typography.labelMedium) }
//                        }
//                    }
//                }
    }
}


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