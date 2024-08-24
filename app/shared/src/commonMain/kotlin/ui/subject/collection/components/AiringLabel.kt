package me.him188.ani.app.ui.subject.collection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.subject.ContinueWatchingStatus
import me.him188.ani.app.data.models.subject.SubjectAiringInfo
import me.him188.ani.app.data.models.subject.SubjectAiringKind
import me.him188.ani.app.data.models.subject.SubjectProgressInfo
import me.him188.ani.app.data.models.subject.isOnAir

// Test: AiringProgressTests
@Stable
class AiringLabelState(
    airingInfoState: State<SubjectAiringInfo?>, // null means loading
    progressInfoState: State<SubjectProgressInfo?>, // null means loading
) {
    private val airingInfo by airingInfoState
    private val progressInfo by progressInfoState

    val isLoading by derivedStateOf {
        airingInfo == null || progressInfo == null
    }

    /**
     * 显示当前看到的剧集, 或者最新连载到的剧集.
     */
    val progressText by derivedStateOf {
        // Hi, 如果你修改这里, 务必在 AiringLabelStateTest 增加测试

        val airingInfo = airingInfo
        val progressInfo = progressInfo
        if (airingInfo == null || progressInfo == null) {
            return@derivedStateOf null
        }
        when (airingInfo.kind) {
            SubjectAiringKind.UPCOMING -> {
                "未开播"
//                if (airingInfo.airDate.isInvalid) {
//                    "未开播"
//                } else {
//                    airingInfo.airDate.toStringExcludingSameYear() + " 开播"
//                }
            }

            SubjectAiringKind.ON_AIR -> {
                when (val s = progressInfo.continueWatchingStatus) {
                    ContinueWatchingStatus.Done -> "已看完"
                    is ContinueWatchingStatus.Watched -> "看过 ${s.episodeSort}"

                    is ContinueWatchingStatus.Continue,
                    is ContinueWatchingStatus.NotOnAir,
                    is ContinueWatchingStatus.Start,
                        ->
                        if (airingInfo.latestSort == null) {
                            "连载中"
                        } else {
                            "连载至 ${airingInfo.latestSort}"
                        }
                }
            }

            SubjectAiringKind.COMPLETED -> {
                when (val s = progressInfo.continueWatchingStatus) {
                    ContinueWatchingStatus.Done -> "已看完"

                    is ContinueWatchingStatus.Watched -> "看过 ${s.episodeSort}"
                    is ContinueWatchingStatus.Continue -> "看过 ${s.watchedEpisodeSort}"

                    is ContinueWatchingStatus.NotOnAir,
                    ContinueWatchingStatus.Start,
                        -> "已完结"
                }
            }
        }
    }

    val highlightProgress by derivedStateOf {
        val continueWatchingStatus = progressInfo?.continueWatchingStatus
        airingInfo?.isOnAir == true
                &&
                when (continueWatchingStatus) {
                    is ContinueWatchingStatus.Continue -> true

                    ContinueWatchingStatus.Start,
                    ContinueWatchingStatus.Done,
                    is ContinueWatchingStatus.NotOnAir,
                    is ContinueWatchingStatus.Watched,
                    null,
                        -> false
                }
    }

    /**
     * "全 xx 话"
     */
    val totalEpisodesText by derivedStateOf {
        val airingInfo = airingInfo ?: return@derivedStateOf null
        if (airingInfo.kind == SubjectAiringKind.UPCOMING && airingInfo.episodeCount == 0) {
            // 剧集还未知
            null
        } else {
            when (airingInfo.kind) {
                SubjectAiringKind.COMPLETED -> "全 ${airingInfo.episodeCount} 话"

                SubjectAiringKind.ON_AIR,
                SubjectAiringKind.UPCOMING,
                    -> "预定全 ${airingInfo.episodeCount} 话"
            }
        }
    }
}

/**
 * ```
 * 已完结 · 全 28 话
 * ```
 *
 * ```
 * 连载至第 28 话 · 全 34 话
 * ```
 *
 * @sample
 */
@Composable
fun AiringLabel(
    state: AiringLabelState,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    progressColor: Color = if (state.highlightProgress) MaterialTheme.colorScheme.primary else Color.Unspecified,
) {
    ProvideTextStyle(style) {
        FlowRow(
            modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            state.progressText?.let {
                Text(
                    it,
                    color = progressColor,
                    softWrap = false,
                )
            }
            state.totalEpisodesText?.let {
                Text(" · ", softWrap = false)
                Text(it, softWrap = false)
            }
        }
    }
}
