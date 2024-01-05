/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.app.ui.subject

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.http.Url
import me.him188.animationgarden.datasources.api.Subject

/**
 * 番剧列表, 双列模式
 */
@Composable
fun SubjectColumn(
    viewModel: SubjectListViewModel,
    modifier: Modifier = Modifier,
    onClickSubject: (Subject) -> Unit = {},
) {
    val items by viewModel.list.collectAsState()

    val cellsCount = 2
    LazyVerticalGrid(
        columns = GridCells.Fixed(cellsCount),
        modifier,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items) { subject ->
            Row {
                SubjectPreviewCard(
                    title = subject.chineseName,
                    imageUrl = subject.images.landscapeCommon(),
                    onClick = {
                        onClickSubject(subject)
                    },
                )
            }
        }

        item("loading", span = { GridItemSpan(cellsCount) }) {
            val hasMore by viewModel.hasMore.collectAsState()
            val loading by viewModel.loading.collectAsState()
            if (loading || hasMore) {
                viewModel.loadMore()

                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 1.5.dp)
                    Text(
                        "加载中",
                        Modifier.height(IntrinsicSize.Max)
                            .padding(start = 8.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
//                LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }
        }
    }
}

/**
 * 番剧预览卡片, 一行显示两个的那种, 只有图片和名称
 */
@Composable
fun SubjectPreviewCard(
    title: String,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Column(modifier.padding(4.dp)) {
            KamelImage(
                asyncPainterResource(Url(imageUrl)),
                "Image",
                Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                onLoading = {
                    Box(
                        Modifier.fillMaxSize()
                            .background(Color.LightGray)
                    ) {
                        CircularProgressIndicator(it, Modifier.align(Alignment.Center))
                    }
                },
                onFailure = {
                    it.printStackTrace()
                    Box(
                        Modifier.fillMaxSize()
                            .background(Color.LightGray)
                    ) {
                        Icon(Icons.Outlined.BrokenImage, "Broken", Modifier.align(Alignment.Center))
                    }
                },
                animationSpec = tween(500),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(4.dp))
            Text(title)
        }
    }
}
