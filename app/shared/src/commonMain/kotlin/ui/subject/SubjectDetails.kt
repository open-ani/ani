package me.him188.animationgarden.app.ui.subject

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import me.him188.animationgarden.app.ProvideCompositionLocalsForPreview
import me.him188.animationgarden.app.preview.PreviewData


private const val COVER_WIDTH_TO_HEIGHT_RATIO = 849 / 1200f

/**
 * 一部番的详情页
 */
@Composable
fun SubjectDetails(viewModel: SubjectDetailsViewModel) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp)) {
            val imageWidth = 120.dp
            KamelImage(
                asyncPainterResource(viewModel.coverImage),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(imageWidth)
                    .height(imageWidth / COVER_WIDTH_TO_HEIGHT_RATIO),
            )

            Column(Modifier.padding(start = 16.dp)) {
                val chineseName by viewModel.chineseName.collectAsState("")
                val officialName by viewModel.officialName.collectAsState("")
                Text(chineseName, style = MaterialTheme.typography.titleMedium)
                Text(officialName, Modifier.padding(top = 8.dp))
            }
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