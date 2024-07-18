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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ExpandCircleDown
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Outbox
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import me.him188.ani.app.ui.foundation.icons.PlayingIcon
import me.him188.ani.app.ui.foundation.text.ProvideContentColor
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.settings.rendering.MediaSourceIcons
import me.him188.ani.app.ui.settings.rendering.renderMediaSource
import me.him188.ani.app.ui.subject.collection.EditableSubjectCollectionTypeIconButton
import me.him188.ani.app.ui.subject.collection.EditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.collection.OnAirLabel
import me.him188.ani.app.ui.subject.episode.EpisodePresentation
import me.him188.ani.app.ui.subject.episode.statistics.DanmakuStatistics
import me.him188.ani.app.ui.subject.episode.statistics.PlayerStatisticsState
import me.him188.ani.app.ui.subject.episode.statistics.VideoStatistics
import me.him188.ani.app.ui.subject.episode.statistics.renderProperties
import me.him188.ani.app.ui.subject.rating.EditableRatingState
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.SubtitleLanguage
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
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
    val coverImageUrl by derivedStateOf { subject.imageLarge }
    val episodeTitle by derivedStateOf { episode.title }
    val episodeSort by derivedStateOf { episode.sort }
    val subjectTitle by derivedStateOf { subject.displayName }
}

@Composable
private fun EpisodeWatchStatus(
    isDone: Boolean,
    onUnmark: () -> Unit,
    onMarkAsDone: () -> Unit,
) {
    if (isDone) {
        IconButton(onUnmark) {
            Icon(Icons.Outlined.TaskAlt, null)
        }
    } else {
        AssistChip(
            onClick = onMarkAsDone,
            label = {
                Text("标记看过")
            },
            leadingIcon = {
                Icon(Icons.Outlined.AddTask, null)
            },
        )
    }
}

@Composable
private fun PlayingEpisodeItem(
    episodeSort: @Composable () -> Unit,
    title: @Composable () -> Unit,
    watchStatus: @Composable () -> Unit,
    mediaLabels: @Composable () -> Unit,
    filename: @Composable () -> Unit,
    mediaSource: @Composable RowScope.() -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp).padding(bottom = 16.dp),
//                .padding(top = 4.dp)
//                .padding(bottom = 4.dp),
        ) {
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                FlowRow(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                ) {
                    ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                        ProvideContentColor(MaterialTheme.colorScheme.primary) {
                            PlayingIcon()
                        }
                        episodeSort()
                        title()
                    }
                }
                Row {
                    watchStatus()
                }
            }
            Spacer(Modifier.height(12.dp))
            ProvideTextStyleContentColor(MaterialTheme.typography.labelLarge, MaterialTheme.colorScheme.secondary) {
                Row {
                    mediaLabels()
                }
            }
            Spacer(Modifier.height(16.dp))
            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.Description, null)
                    filename()
                }
            }
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier, // cancel out semantic paddings
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp), // 20.dp effectively
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        mediaSource()
                    }
                    Row(
                        Modifier.padding(start = 32.dp).offset(x = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp), // 20.dp effectively
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        actions()
                    }
                }
            }
        }
    }
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
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
) {
    var showMore by rememberSaveable { mutableStateOf(false) }
//    ShareEpisodeDropdown(
//        showMore,
//        { showMore = false },
//        onClickCopyLink = {
//            GlobalContext.get().get<BrowserNavigator>()
//                .copyToClipboard(context, "magnet:?xt=urn:btih:1234567890")
//        },
//        onClickDownload = {
//            GlobalContext.get().get<BrowserNavigator>()
//                .openBrowser(context, state.ori)
//        },
//    )
    val navigator = LocalNavigator.current
    EpisodeDetailsScaffold(
        coverImageUrl = state.coverImageUrl,
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
            val media by remember {
                derivedStateOf {
                    playingMedia?.unwrapCached() // 显示原始来源
                }
            }
            episodeCarouselState.playingEpisode?.let { episode ->
                PlayingEpisodeItem(
                    episodeSort = { Text(episode.episodeInfo.sort.toString()) },
                    title = { Text(episode.episodeInfo.displayName) },
                    watchStatus = {
                        EpisodeWatchStatus(
                            episode.collectionType == UnifiedCollectionType.DONE,
                            onUnmark = {
                                editableSubjectCollectionTypeState.setSelfCollectionType(UnifiedCollectionType.NOT_COLLECTED)
                            },
                            onMarkAsDone = {
                                editableSubjectCollectionTypeState.setSelfCollectionType(UnifiedCollectionType.DONE)
                            },
                        )
                    },
                    mediaLabels = {
                        val mediaPropertiesText by remember {
                            derivedStateOf {
                                media?.renderProperties()
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
                        media?.let { media ->
                            Icon(MediaSourceIcons.location(media.location, media.kind), null)

                            Text(
                                remember(media.mediaSourceId) { renderMediaSource(media.mediaSourceId) },
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    },
                    actions = {
                        IconButton({}) {
                            Icon(Icons.Rounded.Download, null)
                        }
                        IconButton({}) {
                            Icon(Icons.Rounded.Outbox, null)
                        }
                    },
                    modifier = Modifier.padding(contentPadding).animateContentSize(),
                )
//                EpisodeCarouselItem(
//                    episode.episodeInfo,
//                    onClick = { episodeCarouselState.onSelect(episode) },
//                    isPlaying = { episodeCarouselState.isPlaying(episode) },
//                    cacheStatus = { episodeCarouselState.cacheStatus(episode) },
//                    collectionButton = {
//                        EpisodeCollectionIconButton(
//                            type = episode.collectionType,
//                            onChange = { episodeCarouselState.setCollectionType(episode, it) },
//                            enabled = !episodeCarouselState.isSettingCollectionType,
//                        )
//                    },
//                    modifier = Modifier.padding(contentPadding),
//                )
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
        onClickCache = {
            navigator.navigateSubjectCaches(state.subjectId)
        },
        onClickShare = {
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
    coverImageUrl: String?,
    subjectTitle: @Composable () -> Unit,
    onAirLabel: @Composable() (FlowRowScope.() -> Unit),
    subjectCollectionActionButton: @Composable () -> Unit,
    exposedEpisodeItem: @Composable (PaddingValues) -> Unit,
    episodeCarousel: @Composable (PaddingValues) -> Unit,
    videoStatistics: @Composable () -> Unit,
    danmakuStatistics: @Composable () -> Unit,
    onClickCache: () -> Unit,
    onClickShare: () -> Unit,
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

@Composable
private fun ShareEpisodeDropdown(
    showMore: Boolean,
    onDismissRequest: () -> Unit,
    onClickCopyLink: () -> Unit,
    onClickDownload: () -> Unit,
    onClickOriginalPage: () -> Unit,
) {
    DropdownMenu(showMore, onDismissRequest) {
        DropdownMenuItem(
            text = { Text("复制磁力链接") },
            onClick = onClickCopyLink,
            leadingIcon = { Icon(Icons.Rounded.ContentCopy, null) },
        )
        DropdownMenuItem(
            text = { Text("使用其他应用打开") },
            onClick = onClickDownload,
            leadingIcon = { Icon(Icons.Rounded.Outbox, null) },
        )
        DropdownMenuItem(
            text = { Text("访问原始页面") },
            onClick = onClickOriginalPage,
            leadingIcon = { Icon(Icons.Rounded.ArrowOutward, null) },
        )
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

