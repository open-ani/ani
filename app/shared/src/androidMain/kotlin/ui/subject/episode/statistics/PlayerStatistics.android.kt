package me.him188.ani.app.ui.subject.episode.statistics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.him188.ani.danmaku.ani.client.AniDanmakuProvider
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


class TestVideoStatistics : VideoStatistics() {
    override var mediaSourceLoading: Boolean by mutableStateOf(false)
    override var playingMedia: Media? by mutableStateOf(null)
    override var playingFilename: String? by mutableStateOf(null)
    override var videoLoadingState: VideoLoadingState by mutableStateOf(VideoLoadingState.Initial)
}

fun testPlayerStatisticsState(
    playingMedia: Media? = null,
    playingFilename: String = "filename-filename-filename-filename-filename-filename-filename.mkv",
    videoLoadingState: VideoLoadingState = VideoLoadingState.Initial,
) =
    TestVideoStatistics().apply {
        this.playingMedia = playingMedia
        this.playingFilename = playingFilename
        this.videoLoadingState = videoLoadingState
    }
