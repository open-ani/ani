package me.him188.ani.app.ui.subject.episode.details

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.source.media.EpisodeCacheStatus
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberBackgroundScope
import me.him188.ani.app.ui.subject.details.components.TestSubjectAiringInfo
import me.him188.ani.app.ui.subject.details.components.rememberTestEditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.details.rememberTestEditableRatingState
import me.him188.ani.app.ui.subject.episode.EpisodePresentation
import me.him188.ani.app.ui.subject.episode.VideoLoadingState
import me.him188.ani.app.ui.subject.episode.statistics.testPlayerStatisticsState


@Composable
@Preview
fun PreviewEpisodeDetails() = ProvideCompositionLocalsForPreview {
    val scope = rememberBackgroundScope()
    val state = remember {
        EpisodeDetailsState(
            episodePresentation = MutableStateFlow(
                EpisodePresentation.Placeholder.copy(
                    title = "一个剧集",
                    sort = "01",
                    isPlaceholder = false,
                ),
            ),
            subjectInfo = MutableStateFlow(
                SubjectInfo.Empty.copy(
                    nameCn = "中文条目名称",
                ),
            ),
            airingInfo = MutableStateFlow(TestSubjectAiringInfo),
            scope.backgroundScope.coroutineContext,
        )
    }
    EpisodeDetails(
        state,
        episodeCarouselState = remember {
            EpisodeCarouselState(
                mutableStateOf(TestEpisodeCollections),
                mutableStateOf(TestEpisodeCollections.first()),
                cacheStatus = { EpisodeCacheStatus.NotCached },
                onSelect = {},
                onChangeCollectionType = { _, _ -> },
                backgroundScope = PreviewScope,
            )
        },
        editableRatingState = rememberTestEditableRatingState(),
        editableSubjectCollectionTypeState = rememberTestEditableSubjectCollectionTypeState(),
        playerStatisticsState = remember {
            testPlayerStatisticsState(
                videoLoadingState = VideoLoadingState.Succeed(isBt = true),
            )
        },
        Modifier.padding(vertical = 16.dp),
    )
}
