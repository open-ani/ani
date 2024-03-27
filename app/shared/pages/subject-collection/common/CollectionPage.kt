/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.ui.collection

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import io.kamel.image.asyncPainterResource
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.ui.foundation.AniKamelImage
import me.him188.ani.app.ui.foundation.AniTopAppBar
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.profile.UnauthorizedTips
import me.him188.ani.app.ui.subject.details.COVER_WIDTH_TO_HEIGHT_RATIO
import me.him188.ani.app.ui.subject.details.Tag
import me.him188.ani.app.ui.theme.stronglyWeaken
import me.him188.ani.app.ui.theme.weaken
import me.him188.ani.datasources.bangumi.processing.isOnAir
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.UserEpisodeCollection

/**
 * 追番列表
 */
@Composable
fun CollectionPage(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val vm = rememberViewModel { MyCollectionsViewModel() }
    Scaffold(
        topBar = {
            AniTopAppBar(
                Modifier.statusBarsPadding(),
                title = { Text("我的追番", style = MaterialTheme.typography.titleMedium) },
            )
//            AniTopAppBar(Modifier.background(MaterialTheme.colorScheme.surface)) {
//                Text("我的追番", style = MaterialTheme.typography.titleMedium)
//            }
        }
    ) { localPaddingValues ->
        val collections by vm.collections.collectAsStateWithLifecycle(null)
        val isLoading by vm.isLoading.collectAsStateWithLifecycle(true)
        val isLoggedIn by vm.isLoggedIn.collectAsStateWithLifecycle(true)

        Column(Modifier.fillMaxSize()) {
            if (!isLoading && collections?.isEmpty() == true) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (!isLoggedIn) {
                        UnauthorizedTips(Modifier.fillMaxSize())
                    } else {
                        Text("~ 空空如也 ~", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                MyCollectionColumn(
                    vm, localPaddingValues + contentPadding,
                    Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MyCollectionColumn(
    viewModel: MyCollectionsViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val spacedBy = 8.dp
    val state = viewModel.collectionsListState
    LazyColumn(
        modifier.padding(horizontal = 12.dp).padding(vertical = 12.dp),
        state,
        verticalArrangement = Arrangement.spacedBy(spacedBy),
    ) {
        (contentPadding.calculateTopPadding() - spacedBy).coerceAtLeast(0.dp).takeIf { it > 0.dp }?.let {
            item { Spacer(Modifier.height(it)) }
        }
        items(collections.orEmpty(), key = { it.subjectId }) { collection ->
            var targetAlpha by remember { mutableStateOf(0f) }
            val alpha by animateFloatAsState(
                targetAlpha,
                if (state.canScrollBackward || state.canScrollForward) snap(0) else tween(150)
            )
            Box(Modifier.alpha(alpha)) {
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

        (contentPadding.calculateBottomPadding() - spacedBy).coerceAtLeast(0.dp).takeIf { it > 0.dp }?.let {
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
    Card(
        onClick,
        modifier.clip(cardShape).fillMaxWidth().height(128.dp),
        shape = cardShape,
    ) {
        Row(Modifier.weight(1f, fill = false)) {
            AniKamelImage(
                asyncPainterResource(item.image),
                modifier = Modifier
                    .height(128.dp).width(128.dp * COVER_WIDTH_TO_HEIGHT_RATIO),
                contentDescription = null,
            )

            Box(Modifier.weight(1f)) {
                CollectionItemContent(
                    item,
                    onClickEpisode,
                    onLongClickEpisode,
                    viewModel,
                    Modifier.fillMaxSize()
                        .padding(start = 8.dp)
                        .padding(vertical = 6.dp),
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
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            Text(
                item.displayName,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Box {
                var showDropdown by remember { mutableStateOf(false) }
                IconButton({ showDropdown = true }, Modifier.height(28.dp).padding()) {
                    Icon(Icons.Outlined.MoreVert, null, Modifier.size(20.dp))
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

        ProvideTextStyle(MaterialTheme.typography.labelMedium) {
            Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                item.date?.let {
                    Tag(Modifier.padding(end = 6.dp)) { Text(item.date) }
                }

                // 连载至第 28 话 · 全 34 话
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

        // 剧集列表
        Row(Modifier.padding(top = 4.dp).weight(1f), verticalAlignment = Alignment.CenterVertically) {
            val state = rememberLazyListState()
            LazyRow(
                Modifier.fillMaxWidth(),
                state,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(item.episodes) {
                    SmallEpisodeButton(
                        it,
                        onClick = { onClickEpisodeState(it) },
                        onLongClick = { onLongClickEpisode(it) },
                    )
                }
            }

            // 初始化时滚动到上次观看的那一集
            val density by rememberUpdatedState(LocalDensity.current)
            LaunchedEffect(state) {
                if (item.episodes.isNotEmpty()) {
                    item.lastWatchedEpIndex?.let {
                        val index = it.minus(1).coerceAtLeast(0)
                        if (index != 0) {
                            // 左边留出来半个按钮的宽度, 让用户知道还有前面的
                            state.scrollToItem(
                                index,
                                density.run { 16.dp.toPx().toInt() }
                            )
                        }
                    }
                }
            }
        }

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
private fun SmallEpisodeButton(
    it: UserEpisodeCollection,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    me.him188.ani.app.ui.foundation.FilledTonalButton(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier.combinedClickable(onLongClick = onLongClick, onClick = onClick).size(36.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
        colors = ButtonDefaults.elevatedButtonColors(
//            containerColor = MaterialTheme.colorScheme.onSurface.copy(0.12f),
//            contentColor = MaterialTheme.colorScheme.onSurface.copy(0.38f),
            containerColor = when {
                it.type == EpisodeCollectionType.WATCHED || it.type == EpisodeCollectionType.DISCARDED ->
                    MaterialTheme.colorScheme.primary.weaken()

                it.episode.isOnAir() != false ->  // 未开播
                    MaterialTheme.colorScheme.onSurface.stronglyWeaken()

                // 还没看
                else -> MaterialTheme.colorScheme.primary
            },
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(it.episode.sort.toString(), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
internal expect fun PreviewCollectionPage()


@Composable
private operator fun PaddingValues.plus(contentPadding: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + contentPadding.calculateStartPadding(layoutDirection),
        top = calculateTopPadding() + contentPadding.calculateTopPadding(),
        end = calculateEndPadding(layoutDirection) + contentPadding.calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + contentPadding.calculateBottomPadding(),
    )
}