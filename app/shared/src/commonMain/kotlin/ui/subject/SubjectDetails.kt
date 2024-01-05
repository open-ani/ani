package me.him188.animationgarden.app.ui.subject

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.kamel.core.Resource
import io.kamel.image.asyncPainterResource
import me.him188.animationgarden.app.ProvideCompositionLocalsForPreview
import me.him188.animationgarden.app.preview.PreviewData
import me.him188.animationgarden.app.ui.foundation.AniKamelImage
import me.him188.animationgarden.app.ui.foundation.BrokenImagePlaceholder
import me.him188.animationgarden.app.ui.foundation.LoadingIndicator
import me.him188.animationgarden.app.ui.foundation.backgroundWithGradient


private const val COVER_WIDTH_TO_HEIGHT_RATIO = 849 / 1200f

/**
 * 一部番的详情页
 */
@Composable
fun SubjectDetails(viewModel: SubjectDetailsViewModel) {
    Box(Modifier.fillMaxWidth()) {
        val coverImageUrl by viewModel.coverImage.collectAsState(null)
        val coverPainter = asyncPainterResource(coverImageUrl ?: "")

        // 虚化渐变背景
        Box(
            Modifier.height(360.dp).fillMaxWidth()
                .blur(12.dp)
                .backgroundWithGradient(
                    coverImageUrl, MaterialTheme.colorScheme.background,
                    brush = Brush.verticalGradient(
                        0f to Color(0xA2FAFAFA),
                        0.5f to Color(0xA2FAFAFA),
                        1.00f to MaterialTheme.colorScheme.background,
                    ),
                )
        ) {
        }

        // 内容
        SubjectDetailsContent(coverPainter, viewModel)
    }
}

// 详情页内容 (不包含背景)
@Composable
private fun SubjectDetailsContent(
    coverImage: Resource<Painter>,
    viewModel: SubjectDetailsViewModel,
) {
    Column(Modifier.fillMaxWidth()) {
        Header(coverImage, viewModel)

        val infoboxList by viewModel.infoboxList.collectAsState(emptyList())
        Column {
            for (infobox in infoboxList) {
                Row {
                    Text(infobox.key)
                    Spacer(Modifier.width(8.dp))
                    Text(infobox.value.toString())
                }
            }
        }
    }
}

// 图片和标题
@Composable
private fun Header(
    coverImage: Resource<Painter>,
    viewModel: SubjectDetailsViewModel,
) {
    Row(Modifier.padding(16.dp)) {
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
                Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            val tags by viewModel.tags.collectAsState(listOf())

            Box(Modifier.height(104.dp).clip(RectangleShape)) {
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
        }
    }
}

// 一个标签, 例如 "2023年10月", "漫画改"
@Composable
fun Tag(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp),
    label: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clip(shape)
    ) {
        Row(Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
            label()
        }
    }
}

@Composable
internal expect fun PreviewSubjectDetails()

@Composable
internal fun PreviewSubjectDetailsImpl() {
    ProvideCompositionLocalsForPreview {
        val vm = remember {
            SubjectDetailsViewModel(PreviewData.SosouNoFurilenId.toString())
        }
        SubjectDetails(vm)
    }
}