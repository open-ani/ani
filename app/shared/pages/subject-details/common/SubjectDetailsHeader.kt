package me.him188.ani.app.ui.subject.details

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.theme.weaken
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

const val COVER_WIDTH_TO_HEIGHT_RATIO = 849 / 1200f

// 图片和标题
@Composable
internal fun SubjectDetailsHeader(
    viewModel: SubjectDetailsViewModel,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        val imageWidth = 120.dp

        Box(
            Modifier
                .clip(MaterialTheme.shapes.medium)
        ) {
            val coverImage by viewModel.coverImage.collectAsStateWithLifecycle(null)
            AsyncImage(
                coverImage,
                null,
                Modifier
                    .width(imageWidth)
                    .height(imageWidth / COVER_WIDTH_TO_HEIGHT_RATIO),
                contentScale = ContentScale.Crop,
            )
        }

        Column(Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
            val chineseName by viewModel.chineseName.collectAsStateWithLifecycle("")
            val officialName by viewModel.officialName.collectAsStateWithLifecycle("")
            Text(
                chineseName,
                Modifier.offset(y = (-2).dp),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                officialName,
                Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            val tags by viewModel.tags.collectAsStateWithLifecycle(listOf())

            Box(Modifier.height(56.dp).clip(RectangleShape)) {
                FlowRow(
                    Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    for (tag in tags) {
                        Tag { Text(tag.name, style = MaterialTheme.typography.labelMedium) }
                    }
                }
            }

            val summary by viewModel.summary.collectAsStateWithLifecycle("")
            Text(
                summary,
                Modifier.height(108.dp / 2).padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}


// 一个标签, 例如 "2023年10月", "漫画改"
@Composable
fun Tag(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    label: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier
            .border(1.dp, MaterialTheme.colorScheme.outline.weaken(), shape)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            label()
        }
    }
}
