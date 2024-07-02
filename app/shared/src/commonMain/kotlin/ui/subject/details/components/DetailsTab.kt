package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
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
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor


object SubjectDetailsDefaults


@Suppress("UnusedReceiverParameter")
@Composable
fun SubjectDetailsDefaults.DetailsTab(
    info: SubjectInfo,
    staff: List<RelatedPersonInfo>,
    characters: List<RelatedCharacterInfo>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    horizontalPadding: Dp = 16.dp,
) {
    LazyColumn(
        modifier,
        state = state,
        verticalArrangement = Arrangement.spacedBy(24.dp), // 这个页面内容比较密集, 如果用 16 显得有点拥挤
    ) {
        item("spacer header") { }

        // 简介
        item("description") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                TagsList(info, Modifier.padding(horizontal = horizontalPadding))
            }
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

private const val ALWAYS_SHOW_TAGS_COUNT = 8

@Composable
private fun TagsList(
    info: SubjectInfo,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        val allTags by remember(info) {
            derivedStateOf { info.tags }
        }
        var isExpanded by rememberSaveable { mutableStateOf(false) }
        val hasMoreTags by remember { derivedStateOf { allTags.size > ALWAYS_SHOW_TAGS_COUNT } }
        FlowRow(
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val presentTags by remember {
                derivedStateOf {
                    when {
                        isExpanded -> allTags
                        allTags.size <= 6 -> allTags
                        else -> {
                            val filteredByCount = allTags.filter { it.count > 100 }
                            if (filteredByCount.size < ALWAYS_SHOW_TAGS_COUNT) {
                                allTags.take(ALWAYS_SHOW_TAGS_COUNT)
                            } else {
                                filteredByCount
                            }
                        }
                    }
                }
            }
            presentTags.forEach { tag ->
                OutlinedTag(
                    Modifier
                        .clickable {}
                        .height(40.dp)
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) {
                    ProvideTextStyleContentColor(
                        MaterialTheme.typography.labelMedium,
                    ) {
                        Text(
                            tag.name,
                            maxLines = 1,
                        )

                        Text(
                            tag.count.toString(),
                            Modifier.padding(start = 6.dp),
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
            if (hasMoreTags) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    if (isExpanded) {
                        TextButton(
                            { isExpanded = !isExpanded },
                            Modifier.height(40.dp),
                        ) {
                            Text("显示更少")
                        }
                    } else {
                        TextButton(
                            { isExpanded = !isExpanded },
                            Modifier.height(40.dp),
                        ) {
                            Text("显示更多")
                        }
                    }
                }
            }
        }
//        if (hasMoreTags) {
//            
//        }
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
        avatarUrl = info.personInfo.images?.medium?.takeIf { it.isNotEmpty() },
        name = info.personInfo.displayName,
        relation = info.relation,
        modifier = modifier,
    )
}

@Composable
fun PersonCard(info: RelatedCharacterInfo, modifier: Modifier = Modifier) {
    PersonCard(
        avatarUrl = info.images?.medium?.takeIf { it.isNotEmpty() },
        name = info.displayName,
        relation = info.relation,
        modifier = modifier,
        actorName = remember(info) { getFirstName(info.actors) },
    )
}

private fun getFirstName(actors: List<PersonInfo>): String {
    if (actors.isEmpty()) return ""
    val actor = actors.first()
    return actor.displayName
}

@Composable
fun PersonCard(
    avatarUrl: String?,
    name: String,
    relation: String,
    modifier: Modifier = Modifier,
    actorName: String? = null,
) {
    Row(modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.clip(MaterialTheme.shapes.small).size(48.dp)) {
                AvatarImage(avatarUrl, Modifier.matchParentSize(), alignment = Alignment.TopCenter)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    Text(name, Modifier.basicMarquee(), softWrap = false, fontWeight = FontWeight.Bold)
                }


                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                        Text(relation, softWrap = false, maxLines = 1)

                        if (actorName != null) {
                            Text(" · ", softWrap = false, maxLines = 1)
                            Text(actorName, Modifier.basicMarquee(), softWrap = false, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
