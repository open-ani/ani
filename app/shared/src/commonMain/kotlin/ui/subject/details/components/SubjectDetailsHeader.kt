package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.subject.PackedDate
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.data.subject.seasonMonth
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.theme.weaken

const val COVER_WIDTH_TO_HEIGHT_RATIO = 849 / 1200f

// 图片和标题
@Composable
internal fun SubjectDetailsHeader(
    info: SubjectInfo,
    coverImageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.Top) {
        val imageWidth = 120.dp

        Box(Modifier.clip(MaterialTheme.shapes.medium)) {
            AsyncImage(
                coverImageUrl,
                null,
                Modifier
                    .width(imageWidth)
                    .height(imageWidth / COVER_WIDTH_TO_HEIGHT_RATIO),
                contentScale = ContentScale.Crop,
                placeholder = if (currentAniBuildConfig.isDebug) remember { ColorPainter(Color.Gray) } else null,
            )
        }

        Column(
            Modifier.weight(1f, fill = false)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Column(
                Modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SelectionContainer {
                    Text(
                        info.displayName,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                if (info.name != info.displayName) {
                    SelectionContainer {
                        Text(
                            info.name,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                Tag {
                    Text(
                        renderSubjectSeason(info.publishDate),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            Column {
                // 评分
            }
        }
    }
}


// 一个标签, 例如 "2023年10月", "漫画改"
@Composable
fun Tag(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    label: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier
            .border(1.dp, MaterialTheme.colorScheme.outline.weaken(), shape)
            .clip(shape),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            label()
        }
    }
}

@Stable
private fun renderSubjectSeason(date: PackedDate): String {
    if (date.seasonMonth == 0) {
        return date.toString()
    }
    return "${date.year} 年 ${date.seasonMonth} 月"
}
