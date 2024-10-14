/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.Tag
import me.him188.ani.app.ui.settings.mediasource.rss.test.OutlinedMatchTag

/**
 * @see selectedItemIndex `-1` for no selection
 */
@Composable
internal fun SelectorTestSubjectResultLazyRow(
    items: List<SelectorTestSubjectPresentation>,
    selectedItemIndex: Int,
    onSelect: (Int, SelectorTestSubjectPresentation) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    SubcomposeLayout { constraints ->
        // measure 一个卡片的高度
        val (measurable) = subcompose(0) {
            SelectorTestResultCard(
                title = { Text("1\n2") },
                isSelected = false,
                onClick = {},
                tags = {
                    Tag { Text("Dummy") }
                    Tag { Text("https://example.com/example/example") }
                },
            )
        }
        val itemSize = measurable.measure(constraints)

        val (lazyRow) = subcompose(1) {
            SelectorTestResultLazyRow(items, selectedItemIndex, modifier, contentPadding) { index, item ->
                SelectorTestSubjectResultCard(
                    item,
                    selectedItemIndex == index,
                    onClick = { onSelect(index, item) },
                    Modifier.heightIn(min = itemSize.height.toDp()),
                )
            }
        }

        val lazyRowPlaceable = lazyRow.measure(constraints)
        layout(lazyRowPlaceable.width, lazyRowPlaceable.height) {
            lazyRowPlaceable.place(0, 0)
        }
    }
}

@Composable
internal fun SelectorTestSubjectResultCard(
    item: SelectorTestSubjectPresentation,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectorTestResultCard(
        title = {
            Text(
                item.name,
                Modifier.width(IntrinsicSize.Max),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        isSelected = isSelected,
        onClick = onClick,
        modifier
            .width(IntrinsicSize.Min) // 有 widthIn max 之后不知道为什么它就会默认 fillMaxWidth
            .widthIn(min = 120.dp, max = 240.dp),
        tags = {
            for (tag in item.tags) {
                OutlinedMatchTag(tag)
            }
        },
    )
}


@Composable
internal fun SelectorTestResultCard(
    title: @Composable () -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tags: @Composable FlowRowScope.() -> Unit,
) {
    val color = CardDefaults.cardColors(
        containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
        else CardDefaults.cardColors().containerColor,
    )
    Card(
        onClick,
        modifier, colors = color,
    ) {
        ListItem(
            headlineContent = title,
            supportingContent = {
                FlowRow(
                    Modifier.padding(top = 8.dp, bottom = 8.dp).width(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = tags,
                )
            },
            colors = ListItemDefaults.colors(containerColor = color.containerColor),
        )
    }
}
