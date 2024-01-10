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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import me.him188.ani.app.platform.isInLandscapeMode
import me.him188.ani.app.ui.subject.details.COVER_WIDTH_TO_HEIGHT_RATIO
import me.him188.ani.app.ui.subject.details.Tag
import me.him188.ani.datasources.bangumi.processing.nameCNOrName
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.openapitools.client.models.UserSubjectCollection

/**
 * 追番列表
 */
@Composable
fun CollectionPage(viewModel: MyCollectionsViewModel) {
    if (isInLandscapeMode()) {
        CollectionPageLandscape(viewModel)
    } else {
        CollectionPagePortrait(viewModel)
    }
}

@Composable
private fun CollectionPageLandscape(viewModel: MyCollectionsViewModel) {
    CollectionPagePortrait(viewModel)
}

@Composable
private fun CollectionPagePortrait(viewModel: MyCollectionsViewModel) {
    Column(
        Modifier.systemBarsPadding().fillMaxSize()
    ) {
        val collections by viewModel.collections.collectAsStateWithLifecycle(listOf())
        val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle(true)
        if (collections.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("~ 空空如也 ~", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), rememberLazyListState()) {
                items(collections) {
                    CollectionItem(it)
                }
            }
        }
    }
}

@Composable
fun CollectionItem(item: UserSubjectCollection) {
    Card(Modifier.clip(RoundedCornerShape(16.dp)).fillMaxWidth().height(128.dp)) {
        Row(Modifier.weight(1f, fill = false)) {
            KamelImage(
                asyncPainterResource(item.subject?.images?.common ?: ""),
                modifier = Modifier
                    .height(128.dp).width(128.dp * COVER_WIDTH_TO_HEIGHT_RATIO),
                contentDescription = null,
            )
            Column(Modifier.padding(start = 8.dp).weight(1f, fill = false)) {
                val name = item.subject?.nameCNOrName() ?: ""
                Text(
                    name,
                    Modifier.offset(y = (-2).dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    item.subject?.name ?: "",
                    Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                val tags = item.subject?.tags

                Box(Modifier.height(56.dp).clip(RectangleShape)) {
                    FlowRow(
                        Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        for (tag in tags.orEmpty()) {
                            Tag { Text(tag.name, style = MaterialTheme.typography.labelMedium) }
                        }
                    }
                }

            }
        }
    }
}

@Composable
internal expect fun PreviewCollectionPage()