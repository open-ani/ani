package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.subject.PersonInfo
import me.him188.ani.app.data.subject.RelatedCharacterInfo
import me.him188.ani.app.data.subject.RelatedPersonInfo
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.foundation.avatar.defaultAvatar


object SubjectDetailsDefaults


@Suppress("UnusedReceiverParameter")
@Composable
fun SubjectDetailsDefaults.DetailsTab(
    info: SubjectInfo,
    staff: List<RelatedPersonInfo>,
    characters: List<RelatedCharacterInfo>,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
) {
    LazyColumn(
        modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp), // 这个页面内容比较密集, 如果用 16 显得有点拥挤
    ) {
        item("spacer header") { }

        // 简介
        item("description") {
            SelectionContainer {
                var expanded by rememberSaveable { mutableStateOf(false) }
                Text(
                    info.summary,
                    Modifier.fillMaxWidth().padding(horizontal = horizontalPadding)
                        .clickable { expanded = !expanded },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 5, // TODO: add animation 
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // 标签
        item("labels") {
            TagsList(horizontalPadding, info)
        }

        item("characters title") {
            Text(
                "角色",
                Modifier.padding(horizontal = horizontalPadding),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        item("characters") {
            PersonCardList(
                values = characters,
                sheetTitle = { Text("角色 ${characters.size}") },
                modifier = Modifier.padding(horizontal = horizontalPadding),
                itemContent = { PersonCard(it) },
            )
        }

        item("staff title") {
            Text(
                "制作人员",
                Modifier.padding(horizontal = horizontalPadding),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        item("staff") {
            PersonCardList(
                values = staff,
                sheetTitle = { Text("制作人员 ${staff.size}") },
                modifier = Modifier.padding(horizontal = horizontalPadding),
                itemContent = { PersonCard(it) },
            )
        }

        item("spacer footer") {
        }
    }
}

@Composable
private fun TagsList(
    horizontalPadding: Dp,
    info: SubjectInfo,
    modifier: Modifier = Modifier,
) {
    val gridItemSpacing = 12.dp
    LazyHorizontalStaggeredGrid(
        StaggeredGridCells.FixedSize(40.dp),
        horizontalItemSpacing = gridItemSpacing,
        verticalArrangement = Arrangement.spacedBy(gridItemSpacing, alignment = Alignment.CenterVertically),
        modifier = modifier.height(40.dp * 2 + gridItemSpacing),
    ) {
        item(
            contentType = "spacer header",
            span = StaggeredGridItemSpan.FullLine,
        ) { Spacer(Modifier.width(horizontalPadding - gridItemSpacing)) }
        items(info.tags, contentType = { 1 }) { tag ->
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                OutlinedTag(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(tag.name, maxLines = 1, color = MaterialTheme.colorScheme.secondary)

                    Text(
                        tag.count.toString(),
                        Modifier.padding(start = 8.dp),
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
        item(
            contentType = "spacer footer",
            span = StaggeredGridItemSpan.FullLine,
        ) { Spacer(Modifier.width(horizontalPadding - gridItemSpacing)) }
    }
}

@Composable
private fun <T> PersonCardList(
    values: List<T>,
    sheetTitle: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    exposedItems: Int = 6,
    maxItemsInEachRow: Int = 2,
    itemSpacing: Dp = 12.dp,
    itemContent: @Composable (T) -> Unit,
) {
    Column(modifier) {
        val valuesUpdated by rememberUpdatedState(values)
        val exposedItemsUpdated by rememberUpdatedState(exposedItems)
        val showStaff by remember { derivedStateOf { valuesUpdated.take(exposedItemsUpdated) } }
        val hasMore by remember { derivedStateOf { valuesUpdated.size > exposedItemsUpdated } }

        var showSheet by rememberSaveable { mutableStateOf(false) }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            maxItemsInEachRow = maxItemsInEachRow,
        ) {
            for (item in showStaff) {
                Box(Modifier.weight(1f)) {
                    itemContent(item)
                }
            }
        }
        if (hasMore) {
            TextButton(
                { showSheet = true },
                Modifier.padding(top = 8.dp).align(Alignment.End),
            ) {
                Text("查看全部")
            }
        }

        if (showSheet) {
            ModalBottomSheet({ showSheet = false }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                        Row { sheetTitle() }
                    }

                    LazyVerticalGrid(
                        GridCells.Fixed(maxItemsInEachRow),
                        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                        verticalArrangement = Arrangement.spacedBy(itemSpacing),
                    ) {
                        items(values) {
                            itemContent(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PersonCard(info: RelatedPersonInfo, modifier: Modifier = Modifier) {
    PersonCard(
        avatarUrl = info.personInfo.images?.medium?.takeIf { it.isNotEmpty() }
            ?: defaultAvatar(
                info.personInfo.displayName,
                MaterialTheme.colorScheme.background,
                LocalContentColor.current,
            ),
        name = info.personInfo.displayName,
        relation = info.relation,
        modifier = modifier,
    )
}

@Composable
fun PersonCard(info: RelatedCharacterInfo, modifier: Modifier = Modifier) {
    PersonCard(
        avatarUrl = info.images?.medium?.takeIf { it.isNotEmpty() }
            ?: defaultAvatar(
                info.displayName,
                MaterialTheme.colorScheme.background,
                LocalContentColor.current,
            ),
        name = info.displayName,
        relation = remember(info) { renderCharacterRelation(info.relation, info.actors) },
        modifier = modifier,
    )
}

private fun renderCharacterRelation(relation: String, actors: List<PersonInfo>): String {
    if (actors.isEmpty()) return relation
    val actor = actors.first()
    return "$relation · ${actor.displayName}"
}

@Composable
fun PersonCard(
    avatarUrl: String?,
    name: String,
    relation: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.clip(MaterialTheme.shapes.small).size(48.dp)) {
                AvatarImage(avatarUrl, alignment = Alignment.TopCenter)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    Text(name, softWrap = false, fontWeight = FontWeight.Bold)
                }

                Text(relation, softWrap = false, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
