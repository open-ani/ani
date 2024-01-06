package me.him188.animationgarden.app.ui.subject.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import io.kamel.core.Resource
import io.kamel.image.asyncPainterResource
import me.him188.animationgarden.app.ProvideCompositionLocalsForPreview
import me.him188.animationgarden.app.preview.PreviewData
import me.him188.animationgarden.app.ui.foundation.backgroundWithGradient
import me.him188.animationgarden.app.ui.subject.details.header.SubjectDetailsHeader
import me.him188.animationgarden.datasources.bangumi.client.BangumiEpisode


/**
 * 一部番的详情页
 */
@Composable
fun SubjectDetails(viewModel: SubjectDetailsViewModel) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            val coverImageUrl by viewModel.coverImage.collectAsState(null)
            val coverPainter = asyncPainterResource(coverImageUrl ?: "")

            // 虚化渐变背景
            Box(
                Modifier.align(Alignment.TopStart).height(250.dp).fillMaxWidth()
                    .blur(12.dp)
                    .backgroundWithGradient(
                        coverImageUrl, MaterialTheme.colorScheme.background,
                        brush = Brush.verticalGradient(
                            0f to Color(0xA2FAFAFA),
                            0.4f to Color(0xA2FAFAFA),
                            1.00f to MaterialTheme.colorScheme.background,
                        ),
                    )
            ) {
            }

            // 内容
            SubjectDetailsContent(
                coverPainter, viewModel,
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 6.dp, bottom = 16.dp)
            )
        }
    }
}

// 详情页内容 (不包含背景)
@Composable
private fun SubjectDetailsContent(
    coverImage: Resource<Painter>,
    viewModel: SubjectDetailsViewModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        // 封面, 标题, 标签 
        SubjectDetailsHeader(coverImage, viewModel, Modifier.padding(top = 16.dp, bottom = 4.dp))

//        ProvideTextStyle(MaterialTheme.typography.titleLarge) {
//            var selectedTabIndex by remember { mutableStateOf(0) }
//            TabRow(
//                selectedTabIndex,
//                Modifier.padding(top = 16.dp),
//                divider = {},
//                containerColor = Color.Transparent,
//                indicator = @Composable { tabPositions ->
//                    if (selectedTabIndex < tabPositions.size) {
//                        TabRowDefaults.Indicator(
//                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
//                        )
//                    }
//                }
//            ) {
//                Tab(true, {}) {
//                    Text("正片")
//                }
//                Tab(false, {}) {
//                    Text("PV")
//                }
//                Tab(false, {}) {
//                    Text("SP")
//                }
//                Tab(false, {}) {
//                    Text("SP")
//                }
//            }
//        }

        val episodesMain by viewModel.episodesMain.collectAsState(listOf())
        if (episodesMain.isNotEmpty()) {
            SectionTitle { Text("正片", color = MaterialTheme.colorScheme.onBackground) }
            EpisodeList(episodesMain, Modifier.padding(top = 8.dp))
        }

        val episodesSP by viewModel.episodesSP.collectAsState(listOf())
        if (episodesSP.isNotEmpty()) {
            SectionTitle { Text("SP", color = MaterialTheme.colorScheme.onBackground) }
            EpisodeList(episodesSP, Modifier.padding(top = 8.dp))
        }

        val episodesPV by viewModel.episodesPV.collectAsState(listOf())
        if (episodesPV.isNotEmpty()) {
            SectionTitle { Text("PV", color = MaterialTheme.colorScheme.onBackground) }
            EpisodeList(episodesPV, Modifier.padding(top = 8.dp))
        }

//        Column(
//            Modifier.wrapContentHeight().fillMaxWidth(),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            for (bangumiEpisode in episodesMain) {
//                EpisodeItem(bangumiEpisode)
//            }
//        }
    }
}

@Composable
private fun EpisodeList(episodes: List<BangumiEpisode>, modifier: Modifier = Modifier) {
    LazyHorizontalGrid(
        GridCells.Fixed(1),
        modifier = modifier.height(60.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(episodes, key = { it.id }) { episode ->
            EpisodeItem(episode)
        }
    }
}

@Composable
fun EpisodeItem(
    episode: BangumiEpisode,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    ElevatedCard(
        onClick = {},
        modifier.clip(shape)
            .fillMaxWidth(),
        shape = shape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(episode.sort.fixToString(2), style = MaterialTheme.typography.bodyMedium)
                Text(
                    episode.chineseName,
                    Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.ChatBubbleOutline,
                    null,
                    Modifier.size(16.dp)
                )
                Text(
                    remember { "${episode.comment}" },
                    Modifier.padding(start = 4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

fun Int.fixToString(length: Int, prefix: Char = '0'): String {
    val str = this.toString()
    return if (str.length >= length) {
        str
    } else {
        prefix.toString().repeat(length - str.length) + str
    }
}

@Composable
private fun SectionTitle(modifier: Modifier = Modifier, text: @Composable () -> Unit) {
    Row(modifier.padding(top = 8.dp, bottom = 8.dp)) {
        ProvideTextStyle(MaterialTheme.typography.titleLarge) {
            text()
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