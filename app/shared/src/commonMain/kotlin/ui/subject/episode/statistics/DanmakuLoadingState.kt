package me.him188.ani.app.ui.subject.episode.statistics

import androidx.compose.runtime.Immutable
import me.him188.ani.danmaku.api.DanmakuMatchInfo

@Immutable
sealed class DanmakuLoadingState {
    @Immutable
    data object Idle : DanmakuLoadingState()

    @Immutable
    data object Loading : DanmakuLoadingState()

    @Immutable
    data class Success(
        val matchInfos: List<DanmakuMatchInfo>
    ) : DanmakuLoadingState()

    @Immutable
    data class Failed(
        val cause: Throwable,
    ) : DanmakuLoadingState()
}