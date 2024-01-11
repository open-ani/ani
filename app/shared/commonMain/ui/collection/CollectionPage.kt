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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import io.kamel.image.asyncPainterResource
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.isInLandscapeMode
import me.him188.ani.app.ui.foundation.AniKamelImage
import me.him188.ani.app.ui.foundation.AniTopAppBar
import me.him188.ani.app.ui.foundation.launchInBackground
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
fun CollectionPage(contentPadding: PaddingValues = PaddingValues(0.dp), viewModel: MyCollectionsViewModel) {
    if (isInLandscapeMode()) {
        CollectionPageLandscape(contentPadding, viewModel)
    } else {
        CollectionPagePortrait(contentPadding, viewModel)
    }
}

@Composable
private fun CollectionPageLandscape(contentPadding: PaddingValues, viewModel: MyCollectionsViewModel) {
    CollectionPagePortrait(contentPadding, viewModel)
}

@Composable
private fun CollectionPagePortrait(contentPadding: PaddingValues, viewModel: MyCollectionsViewModel) {
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
        Column(Modifier.fillMaxSize()) {
            val collections by viewModel.collections.collectAsStateWithLifecycle(listOf())
            val isEmpty by viewModel.isEmpty.collectAsStateWithLifecycle(null)
            val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle(true)
            if (collections.isEmpty() && isEmpty == true) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val context = LocalContext.current
                    if (!isLoggedIn) {
                        TextButton({ viewModel.launchInBackground { navigateToAuth(context) } }) {
                            Text("请先登录", style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        Text("~ 空空如也 ~", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                MyCollectionColumn(collections, viewModel, localPaddingValues + contentPadding)
            }
        }
    }
}

@Composable
private fun ColumnScope.MyCollectionColumn(
    collections: List<SubjectCollectionItem>,
    viewModel: MyCollectionsViewModel,
    contentPadding: PaddingValues
) {
    val spacedBy = 8.dp
    LazyColumn(
        Modifier.padding(horizontal = 12.dp).padding(vertical = 12.dp).fillMaxSize(),
        rememberLazyListState(),
        verticalArrangement = Arrangement.spacedBy(spacedBy),
    ) {
        (contentPadding.calculateTopPadding() - spacedBy).coerceAtLeast(0.dp).takeIf { it > 0.dp }?.let {
            item { Spacer(Modifier.height(it)) }
        }
        items(collections, key = { it.subjectId }) { collection ->
            var visible by remember { mutableStateOf(false) }
            val context = LocalContext.current
            AnimatedVisibility(
                visible,
                enter = fadeIn()
            ) {
                CollectionItem(
                    collection,
                    onClick = {
                        viewModel.navigateToSubject(context, collection.subjectId)
                    },
                    onClickEpisode = {
                        viewModel.navigateToEpisode(context, collection.subjectId, it.episode.id)
                    },
                    viewModel,
                )
            }
            SideEffect {
                visible = true
            }
        }

        (contentPadding.calculateBottomPadding() - spacedBy).coerceAtLeast(0.dp).takeIf { it > 0.dp }?.let {
            item { Spacer(Modifier.height(it)) }
        }
    }
}

@Composable
private fun CollectionItem(
    item: SubjectCollectionItem,
    onClick: () -> Unit,
    onClickEpisode: (episode: UserEpisodeCollection) -> Unit,
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

@Composable
private fun CollectionItemContent(
    item: SubjectCollectionItem,
    onClickEpisode: (episode: UserEpisodeCollection) -> Unit,
    viewModel: MyCollectionsViewModel,
    modifier: Modifier = Modifier,
) {
    val onClickEpisodeState by rememberUpdatedState(onClickEpisode)
    Column(modifier) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            Text(
                item.displayName,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )

            Spacer(Modifier.weight(1f, fill = true))

            Box {
                var showDropdown by remember { mutableStateOf(false) }
                IconButton({ showDropdown = true }, Modifier.height(28.dp).padding()) {
                    Icon(Icons.Outlined.MoreVert, null, Modifier.size(20.dp))
                }

                EditCollectionTypeDropDown(
                    currentType = item.collectionType,
                    showDropdown, { showDropdown = false },
                    onClick = { action ->
                        viewModel.launchInBackground { updateCollection(item.subjectId, action) }
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

        Row(Modifier.padding(top = 4.dp).weight(1f), verticalAlignment = Alignment.CenterVertically) {
            val state = rememberLazyListState()
            LazyRow(
                Modifier.fillMaxWidth(),
                state,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(item.episodes) {
                    SmallEpisodeButton(it, onClick = { onClickEpisodeState(it) })
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
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick,
        modifier.size(36.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
        colors = ButtonDefaults.elevatedButtonColors(
//            containerColor = MaterialTheme.colorScheme.onSurface.copy(0.12f),
//            contentColor = MaterialTheme.colorScheme.onSurface.copy(0.38f),
            containerColor = when {
                it.type == EpisodeCollectionType.WATCHED || it.type == EpisodeCollectionType.DISCARDED ->
                    MaterialTheme.colorScheme.primary.weaken()

                it.episode.isOnAir() == true ->  // 未开播
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