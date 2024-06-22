package me.him188.ani.app.ui.subject.episode.statistics

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.episode.VideoLoadingState
import me.him188.ani.danmaku.ani.client.AniDanmakuProvider
import me.him188.ani.danmaku.api.DanmakuMatchInfo
import me.him188.ani.danmaku.api.DanmakuMatchMethod
import me.him188.ani.danmaku.dandanplay.DandanplayDanmakuProvider

@Preview
@Composable
private fun PreviewPlayerStatisticsAllSuccess() {
    ProvideCompositionLocalsForPreview {
        PlayerStatistics(
            state = remember {
                PlayerStatisticsState().apply {
                    videoLoadingState.value = VideoLoadingState.Succeed
                    danmakuLoadingState.value = DanmakuLoadingState.Success(
                        listOf(
                            exactMatch(),
                            noMatch(),
                        ),
                    )
                }
            },
            Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun PreviewPlayerStatisticsVideoFailed() {
    ProvideCompositionLocalsForPreview {
        PlayerStatistics(
            state = remember {
                PlayerStatisticsState().apply {
                    videoLoadingState.value = VideoLoadingState.UnknownError(IllegalStateException())
                    danmakuLoadingState.value = DanmakuLoadingState.Success(
                        listOf(
                            exactMatch(),
                            noMatch(),
                        ),
                    )
                }
            },
            Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun PreviewPlayerStatisticsVideoLoading() {
    ProvideCompositionLocalsForPreview {
        PlayerStatistics(
            state = remember {
                PlayerStatisticsState().apply {
                    videoLoadingState.value = VideoLoadingState.ResolvingSource
                    danmakuLoadingState.value = DanmakuLoadingState.Success(
                        listOf(
                            exactMatch(),
                            noMatch(),
                        ),
                    )
                }
            },
            Modifier.padding(16.dp),
        )
    }
}

private fun noMatch() = DanmakuMatchInfo(
    providerId = DandanplayDanmakuProvider.ID,
    count = 200,
    method = DanmakuMatchMethod.NoMatch,
)

private fun exactId() = DanmakuMatchInfo(
    providerId = AniDanmakuProvider.ID,
    count = 200,
    method = DanmakuMatchMethod.ExactId(1, 2),
)

private fun exactMatch() = DanmakuMatchInfo(
    providerId = DandanplayDanmakuProvider.ID,
    count = 100,
    method = DanmakuMatchMethod.Exact("Subject Title", "Episode Title"),
)

private fun fuzzy() = DanmakuMatchInfo(
    providerId = DandanplayDanmakuProvider.ID,
    count = 100,
    method = DanmakuMatchMethod.Fuzzy("Subject Title", "Episode Title"),
)

private fun halfFuzzy() = DanmakuMatchInfo(
    providerId = DandanplayDanmakuProvider.ID,
    count = 100,
    method = DanmakuMatchMethod.ExactSubjectFuzzyEpisode("Subject Title", "Episode Title"),
)

@Preview
@Composable
private fun PreviewPlayerStatisticsDanmakuFailed() {
    ProvideCompositionLocalsForPreview {
        PlayerStatistics(
            state = remember {
                PlayerStatisticsState().apply {
                    videoLoadingState.value = VideoLoadingState.UnknownError(IllegalStateException())
                    danmakuLoadingState.value = DanmakuLoadingState.Failed(
                        cause = IllegalStateException(),
                    )
                }
            },
            Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun PreviewPlayerStatisticsDanmakuState() {
    ProvideCompositionLocalsForPreview {
        PlayerStatistics(
            state = remember {
                PlayerStatisticsState().apply {
                    videoLoadingState.value = VideoLoadingState.UnknownError(IllegalStateException())
                    danmakuLoadingState.value = DanmakuLoadingState.Success(
                        listOf(
                            exactMatch(),
                            exactId(),
                            noMatch(),
                            fuzzy(),
                            halfFuzzy(),
                        ),
                    )
                }
            },
            Modifier.padding(16.dp),
        )
    }
}
