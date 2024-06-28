package me.him188.ani.app.ui.subject.details.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
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
import me.him188.ani.app.ui.foundation.layout.LocalLayoutMode
import me.him188.ani.app.ui.foundation.theme.weaken

const val COVER_WIDTH_TO_HEIGHT_RATIO = 849 / 1200f

// 图片和标题
@Composable
internal fun SubjectDetailsHeader(
    info: SubjectInfo,
    coverImageUrl: String?,
    collectionData: @Composable () -> Unit,
    collectionAction: @Composable () -> Unit,
    selectEpisodeButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalLayoutMode.current.showLandscapeUI) {
        SubjectDetailsHeaderWide(
            coverImageUrl = coverImageUrl,
            title = {
                Text(info.displayName)
            },
            subtitle = {
                if (info.name != info.displayName) {
                    Text(info.name)
                }
            },
            seasonTag = {
                Text(renderSubjectSeason(info.publishDate))
            },
            collectionData = collectionData,
            collectionAction = collectionAction,
            selectEpisodeButton = selectEpisodeButton,
            modifier,
        )
    } else {
        SubjectDetailsHeaderCompact(
            coverImageUrl = coverImageUrl,
            title = {
                Text(info.displayName)
            },
            subtitle = {
                if (info.name != info.displayName) {
                    Text(info.name)
                }
            },
            seasonTag = {
                Text(renderSubjectSeason(info.publishDate))
            },
            collectionData = collectionData,
            collectionAction = collectionAction,
            selectEpisodeButton = selectEpisodeButton,
            modifier,
        )
    }
}


// 适合手机, 窄
@Composable
fun SubjectDetailsHeaderCompact(
    coverImageUrl: String?,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    seasonTag: @Composable () -> Unit,
    collectionData: @Composable () -> Unit,
    collectionAction: @Composable () -> Unit,
    selectEpisodeButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(Modifier, verticalAlignment = Alignment.Top) {
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
                    .padding(horizontal = 12.dp),
            ) {
                Column(
                    Modifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SelectionContainer {
                        ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                            title()
                        }
                    }
                    SelectionContainer {
                        ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                            subtitle()
                        }
                    }
                    Tag {
                        ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                            seasonTag()
                        }
                    }
                }
                Column {
                    // 评分
                }
            }
        }

        Row(Modifier.padding(vertical = 16.dp).align(Alignment.Start)) {
            collectionData()
        }
        Row(
            Modifier.align(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            selectEpisodeButton()
            collectionAction()
        }
    }
}

@Composable
fun SubjectDetailsHeaderWide(
    coverImageUrl: String?,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    seasonTag: @Composable () -> Unit,
    collectionData: @Composable () -> Unit,
    collectionAction: @Composable () -> Unit,
    selectEpisodeButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(
            Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top,
        ) {
            val imageWidth = 220.dp

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
                Modifier
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    Modifier.weight(1f, fill = true),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SelectionContainer {
                        ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                            title()
                        }
                    }
                    SelectionContainer {
                        ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                            subtitle()
                        }
                    }
                    Tag {
                        ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                            seasonTag()
                        }
                    }
                }
                Row(Modifier.padding(vertical = 4.dp).align(Alignment.Start)) {
                    collectionData()
                }
                Row(
                    Modifier.align(Alignment.Start),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    collectionAction()
                    selectEpisodeButton()
                }
                Column {
                    // 评分
                }
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
fun renderSubjectSeason(date: PackedDate): String {
    if (date.seasonMonth == 0) {
        return date.toString()
    }
    return "${date.year} 年 ${date.seasonMonth} 月"
}
