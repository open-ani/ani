package me.him188.ani.app.ui.subject.details.header

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.kamel.core.Resource
import me.him188.ani.app.ui.foundation.AniKamelImage
import me.him188.ani.app.ui.foundation.BrokenImagePlaceholder
import me.him188.ani.app.ui.foundation.LoadingIndicator
import me.him188.ani.app.ui.subject.details.SubjectDetailsViewModel
import me.him188.ani.app.ui.theme.weaken

private const val COVER_WIDTH_TO_HEIGHT_RATIO = 849 / 1200f

// 图片和标题
@Composable
internal fun SubjectDetailsHeader(
    coverImage: Resource<Painter>,
    viewModel: SubjectDetailsViewModel,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        val imageWidth = 120.dp

        AniKamelImage(
            coverImage,
            Modifier
                .clip(RoundedCornerShape(6.dp))
                .width(imageWidth)
                .height(imageWidth / COVER_WIDTH_TO_HEIGHT_RATIO)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop,
            onLoading = { LoadingIndicator(it) },
            onFailure = { BrokenImagePlaceholder() },
            animationSpec = tween(500),
        )

        Column(Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
            val chineseName by viewModel.chineseName.collectAsState("")
            val officialName by viewModel.officialName.collectAsState("")
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

            val tags by viewModel.tags.collectAsState(listOf())

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

            val summary by viewModel.summary.collectAsState("")
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
private fun Tag(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp),
    label: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier
            .border(1.dp, MaterialTheme.colorScheme.outline.weaken(), shape)
            .clip(shape)
    ) {
        Row(
            Modifier.height(22.dp).padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            label()
        }
    }
}
