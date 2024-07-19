package me.him188.ani.app.ui.subject.episode.details

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material.icons.outlined.ExpandCircleDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.episode.displayName
import me.him188.ani.app.data.models.subject.SubjectAiringInfo
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.ui.subject.collection.EditableSubjectCollectionTypeIconButton
import me.him188.ani.app.ui.subject.collection.EditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.collection.OnAirLabel
import me.him188.ani.app.ui.subject.episode.EpisodePresentation
import me.him188.ani.app.ui.subject.episode.details.components.EpisodeWatchStatusButton
import me.him188.ani.app.ui.subject.episode.details.components.PlayingEpisodeItem
import me.him188.ani.app.ui.subject.episode.details.components.PlayingEpisodeItemDefaults
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSourceResultsPresentation
import me.him188.ani.app.ui.subject.episode.statistics.DanmakuStatistics
import me.him188.ani.app.ui.subject.episode.statistics.PlayerStatisticsState
import me.him188.ani.app.ui.subject.episode.statistics.VideoStatistics
import me.him188.ani.app.ui.subject.episode.statistics.renderProperties
import me.him188.ani.app.ui.subject.rating.EditableRatingState
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.SubtitleLanguage
import me.him188.ani.datasources.api.unwrapCached
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Stable
class EpisodeDetailsState(
    episodePresentation: State<EpisodePresentation>,
    subjectInfo: State<SubjectInfo>,
    airingInfo: State<SubjectAiringInfo>,
) {
    private val episode by episodePresentation
    private val subject by subjectInfo
    val airingInfo by airingInfo

    val subjectId by derivedStateOf { subject.id }
    val episodeTitle by derivedStateOf { episode.title }
    val episodeSort by derivedStateOf { episode.sort }
    val subjectTitle by derivedStateOf { subject.displayName }
}

/**
 * 番剧详情内容, 包含条目的基本信息, 选集, 评分.
 */
@Composable
fun EpisodeDetails(
    state: EpisodeDetailsState,
    episodeCarouselState: EpisodeCarouselState,
    editableRatingState: EditableRatingState,
    editableSubjectCollectionTypeState: EditableSubjectCollectionTypeState,
    playerStatisticsState: PlayerStatisticsState,
    mediaSelectorPresentation: MediaSelectorPresentation,
    mediaSourceResultsPresentation: MediaSourceResultsPresentation,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
) {
    EpisodeDetailsScaffold(
        subjectTitle = { Text(state.subjectTitle) },
        onAirLabel = {
//            OutlinedTag { Text(renderSubjectSeason(state.airingInfo.airDate)) }
            OnAirLabel(
                state.airingInfo,
                Modifier.align(Alignment.CenterVertically),
                style = LocalTextStyle.current,
                statusColor = LocalContentColor.current,
            )
        },
        subjectCollectionActionButton = {
            EditableSubjectCollectionTypeIconButton(editableSubjectCollectionTypeState)
        },
        exposedEpisodeItem = { contentPadding ->
            val playingMedia by playerStatisticsState.playingMedia.collectAsStateWithLifecycle(null)
            val originalMedia by remember {
                derivedStateOf {
                    playingMedia?.unwrapCached() // 显示原始来源
                }
            }
            val mediaSelected by remember {
                derivedStateOf {
                    originalMedia != null
                }
            }
            episodeCarouselState.playingEpisode?.let { episode ->
                PlayingEpisodeItem(
                    episodeSort = { Text(episode.episodeInfo.sort.toString()) },
                    title = { Text(episode.episodeInfo.displayName) },
                    watchStatus = { EpisodeWatchStatusButton(editableSubjectCollectionTypeState) },
                    mediaSelected = mediaSelected,
                    mediaLabels = {
                        val mediaPropertiesText by remember {
                            derivedStateOf {
                                originalMedia?.renderProperties()
                            }
                        }
                        Text(mediaPropertiesText ?: "")
                    },
                    filename = {
                        val filename by playerStatisticsState.playingFilename.collectAsStateWithLifecycle(null)
                        filename?.let {
                            Text(filename ?: "请选择数据源", maxLines = 3, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    mediaSource = {
                        var showMediaSelector by rememberSaveable { mutableStateOf(false) }
                        PlayingEpisodeItemDefaults.MediaSource(
                            media = originalMedia,
                            isLoading = playerStatisticsState.mediaSourceLoading,
                            onClick = { showMediaSelector = !showMediaSelector },
                        )
                        if (showMediaSelector) {
                            ModalBottomSheet({ showMediaSelector = false }) {
                                EpisodePlayMediaSelector(
                                    mediaSelectorPresentation,
                                    mediaSourceResultsPresentation,
                                    onDismissRequest = { showMediaSelector = false },
                                    onSelected = { showMediaSelector = false },
                                )
                            }
                        }
                    },
                    actions = {
                        val navigator = LocalNavigator.current
                        PlayingEpisodeItemDefaults.ActionCache({ navigator.navigateSubjectCaches(state.subjectId) })
                        PlayingEpisodeItemDefaults.ActionShare(playingMedia)
                    },
                    modifier = Modifier.padding(contentPadding).animateContentSize(),
                )
            }
        },
        episodeCarousel = { contentPadding ->
            EpisodeCarousel(
                episodeCarouselState,
                contentPadding = contentPadding,
            )
        },
        videoStatistics = {
            VideoStatistics(playerStatisticsState)
        },
        danmakuStatistics = {
            DanmakuStatistics(playerStatisticsState)
        },
        modifier = modifier,
        horizontalPadding = horizontalPadding,
    )
}

@Composable
private fun SectionTitle(
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    Row(
        modifier.heightIn(min = 40.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProvideTextStyle(MaterialTheme.typography.titleMedium) {
            Row(Modifier.weight(1f)) {
                content()
            }
            Row(Modifier.padding(start = 16.dp)) {
                actions()
            }
        }
    }
}

@Composable
fun EpisodeDetailsScaffold(
    subjectTitle: @Composable () -> Unit,
    onAirLabel: @Composable (FlowRowScope.() -> Unit),
    subjectCollectionActionButton: @Composable () -> Unit,
    exposedEpisodeItem: @Composable (PaddingValues) -> Unit,
    episodeCarousel: @Composable (PaddingValues) -> Unit,
    videoStatistics: @Composable () -> Unit,
    danmakuStatistics: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
) {
    Column(modifier) {
        // header
        Box {
            Column(
                Modifier.padding(horizontal = horizontalPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row {
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically),
                        ) {
                            ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                                SelectionContainer { subjectTitle() }
                            }
                        }

//                        Row {
//                            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
//                                FlowRow(
//                                    Modifier.weight(1f),
//                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
//                                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
//                                ) {
//                                    subjectSeasonTags()
//                                }
//                            }
//
//                            Box(Modifier.padding(start = 16.dp).align(Alignment.Bottom)) {
//                                subjectRating()
//                            }
//                        }
                    }

                    Column(Modifier.offset(y = (-8).dp).padding(start = 24.dp)) {
                        Row {
//                            subjectCollectionActionButton()
                            IconButton({}) {
                                Icon(Icons.Outlined.ExpandCircleDown, null)
                            }
                        }
                    }
                }
            }
        }

        SectionTitle(
            Modifier.padding(top = 16.dp),
            actions = {
                var showCarousel by rememberSaveable { mutableStateOf(false) }
                IconButton({ showCarousel = true }) {
                    Icon(Icons.Outlined.Dataset, null)
                }
                if (showCarousel) {
                    ModalBottomSheet({ showCarousel = false }) {
                        episodeCarousel(PaddingValues(vertical = 16.dp))
                    }
                }
            },
        ) {
            FlowRow(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            ) {
                onAirLabel()
            }
        }

        Row {
            exposedEpisodeItem(PaddingValues(horizontal = horizontalPadding))
        }

//        SectionTitle(
//            Modifier.padding(top = 16.dp),
//        ) {
//            Text("视频统计")
//        }
//
//        Card(Modifier.padding(horizontal = horizontalPadding).fillMaxWidth()) {
//            Column(Modifier.padding(16.dp)) {
//                videoStatistics()
//            }
//        }

        Row(Modifier.padding(top = 16.dp).padding(horizontal = horizontalPadding).fillMaxWidth()) {
            danmakuStatistics()
        }
    }
}

fun renderSubtitleLanguage(id: String): String {
    return when (id) {
        SubtitleLanguage.ChineseCantonese.id -> "粤语"
        SubtitleLanguage.ChineseSimplified.id -> "简中"
        SubtitleLanguage.ChineseTraditional.id -> "繁中"
        SubtitleLanguage.Japanese.id -> "日语"
        SubtitleLanguage.English.id -> "英语"
        else -> id
    }
}

fun renderResolution(id: String): String {
    return Resolution.tryParse(id)?.displayName ?: id
}

