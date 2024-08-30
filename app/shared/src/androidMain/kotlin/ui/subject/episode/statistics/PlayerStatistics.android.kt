package me.him188.ani.app.ui.subject.episode.statistics

import me.him188.ani.app.data.source.danmaku.AniDanmakuProvider
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.danmaku.api.DanmakuMatchInfo
import me.him188.ani.danmaku.api.DanmakuMatchMethod
import me.him188.ani.danmaku.dandanplay.DandanplayDanmakuProvider
import me.him188.ani.datasources.api.Media


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

fun testPlayerStatisticsState(
    playingMedia: Media? = null,
    playingFilename: String = "filename-filename-filename-filename-filename-filename-filename.mkv",
    videoLoadingState: VideoLoadingState = VideoLoadingState.Initial,
) = VideoStatistics(
    playingMedia = stateOf(playingMedia),
    playingMediaSourceInfo = stateOf(null),
    playingFilename = stateOf(playingFilename),
    mediaSourceLoading = stateOf(true),
    videoLoadingState = stateOf(videoLoadingState),
)
