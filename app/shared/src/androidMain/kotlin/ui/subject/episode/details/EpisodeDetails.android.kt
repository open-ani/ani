package me.him188.ani.app.ui.subject.episode.details

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.source.media.EpisodeCacheStatus
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.cache.TestMediaList
import me.him188.ani.app.ui.subject.details.components.TestSubjectAiringInfo
import me.him188.ani.app.ui.subject.details.components.rememberTestEditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.details.rememberTestEditableRatingState
import me.him188.ani.app.ui.subject.episode.EpisodePresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.rememberTestMediaSourceResults
import me.him188.ani.app.ui.subject.episode.statistics.DanmakuLoadingState
import me.him188.ani.app.ui.subject.episode.statistics.VideoLoadingState
import me.him188.ani.app.ui.subject.episode.statistics.testPlayerStatisticsState
import me.him188.ani.app.ui.subject.episode.video.MutableDanmakuStatistics
import me.him188.ani.danmaku.api.DanmakuMatchInfo
import me.him188.ani.danmaku.api.DanmakuMatchMethod


@Composable
@PreviewLightDark
fun PreviewEpisodeDetailsLongTitle() = ProvideCompositionLocalsForPreview {
    val state = rememberTestEpisodeDetailsState(
        remember {
            SubjectInfo.Empty.copy(
                nameCn = "中文条目名称啊中文条目名称中文条啊目名称中文条目名称中文条目名称中文",
            )
        },
    )
    PreviewEpisodeDetailsImpl(state)
}

@Composable
@PreviewLightDark
fun PreviewEpisodeDetailsShortTitle() = ProvideCompositionLocalsForPreview {
    val state = rememberTestEpisodeDetailsState(
        remember {
            SubjectInfo.Empty.copy(
                nameCn = "小市民系列",
            )
        },
    )
    PreviewEpisodeDetailsImpl(state)
}

@Composable
private fun rememberTestEpisodeDetailsState(
    subjectInfo: SubjectInfo = SubjectInfo.Empty.copy(
        nameCn = "中文条目名称啊中文条目名称中文条啊目名称中文条目名称中文条目名称中文",
    ),
) = remember {
    EpisodeDetailsState(
        episodePresentation = mutableStateOf(
            EpisodePresentation.Placeholder.copy(
                title = "一个剧集",
                sort = "01",
                isPlaceholder = false,
            ),
        ),
        subjectInfo = mutableStateOf(subjectInfo),
        airingInfo = mutableStateOf(TestSubjectAiringInfo),
    )
}

@Composable
private fun PreviewEpisodeDetailsImpl(state: EpisodeDetailsState) {
    Scaffold {
        EpisodeDetails(
            state,
            episodeCarouselState = remember {
                EpisodeCarouselState(
                    mutableStateOf(TestEpisodeCollections),
                    mutableStateOf(TestEpisodeCollections[1]),
                    cacheStatus = { EpisodeCacheStatus.NotCached },
                    onSelect = {},
                    onChangeCollectionType = { _, _ -> },
                    backgroundScope = PreviewScope,
                )
            },
            editableRatingState = rememberTestEditableRatingState(),
            editableSubjectCollectionTypeState = rememberTestEditableSubjectCollectionTypeState(),
            danmakuStatistics = remember {
                MutableDanmakuStatistics().apply {
                    danmakuLoadingState = DanmakuLoadingState.Success(
                        listOf(
                            DanmakuMatchInfo(
                                "弹幕源 A", 100,
                                DanmakuMatchMethod.Fuzzy("条目标题", "剧集标题"),
                            ),
                            DanmakuMatchInfo(
                                "弹幕源 B", 100,
                                DanmakuMatchMethod.ExactId(123456, 222222),
                            ),
                        ),
                    )
                }
            },
            videoStatistics = remember {
                testPlayerStatisticsState(
                    playingMedia = TestMediaList.first(),
                    playingFilename = "filename-filename-filename-filename-filename-filename-filename.mkv",
                    videoLoadingState = VideoLoadingState.Succeed(isBt = true),
                )
            },
            mediaSelectorPresentation = rememberTestMediaSelectorPresentation(),
            mediaSourceResultsPresentation = rememberTestMediaSourceResults(),
            Modifier
                .padding(bottom = 16.dp, top = 8.dp)
                .padding(it),
        )
    }
}
