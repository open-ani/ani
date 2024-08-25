package me.him188.ani.app.ui.subject.collection.progress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import me.him188.ani.app.data.models.episode.EpisodeProgressInfo
import me.him188.ani.app.data.models.subject.ContinueWatchingStatus
import me.him188.ani.app.data.models.subject.SubjectCollection
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.models.subject.SubjectProgressInfo
import me.him188.ani.app.data.models.toLocalDateOrNull
import me.him188.ani.app.data.source.media.cache.EpisodeCacheStatus
import me.him188.ani.app.tools.WeekFormatter
import me.him188.ani.app.tools.caching.ContentPolicy
import me.him188.ani.app.ui.foundation.stateOf
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.coroutines.CoroutineContext

// 在 VM 中创建
@Stable
class SubjectProgressStateFactory(
    private val subjectManager: SubjectManager,
    val onPlay: (subjectId: Int, episodeId: Int) -> Unit,
    private val flowCoroutineContext: CoroutineContext = Dispatchers.Default,
) {
    fun subjectCollection(subjectId: Int) =
        subjectManager.subjectCollectionFlow(subjectId)
            .flowOn(flowCoroutineContext)

    fun episodeProgressInfoList(subjectId: Int) = subjectManager
        .subjectProgressFlow(subjectId, ContentPolicy.CACHE_ONLY)
        .flowOn(flowCoroutineContext)
}

/**
 * 为特定条目创建一个 [SubjectProgressState], 绑定到当前 composition
 */
@Composable
fun SubjectProgressStateFactory.rememberSubjectProgressState(
    subjectCollection: SubjectCollection,
): SubjectProgressState {
    val subjectId: Int = subjectCollection.subjectId
    val subjectCollectionState by rememberUpdatedState(subjectCollection)
    val info = remember {
        derivedStateOf {
            SubjectProgressInfo.calculate(subjectCollectionState)
        }
    }
    val episodeProgressInfoList = remember(subjectId) { episodeProgressInfoList(subjectId) }
        .collectAsStateWithLifecycle(emptyList())
    return remember(info, this, subjectId) {
        SubjectProgressState(
            stateOf(subjectId),
            info,
            episodeProgressInfoList,
            onPlay = onPlay,
        )
    }
}

/**
 * 条目的观看进度, 用于例如 "看到 12" 的按钮
 */
@Stable // Test: AiringProgressTests
class SubjectProgressState(
    subjectId: State<Int>,
    info: State<SubjectProgressInfo?>,
    episodeProgressInfos: State<List<EpisodeProgressInfo>>,
    private val onPlay: (subjectId: Int, episodeId: Int) -> Unit,
    private val weekFormatter: WeekFormatter = WeekFormatter.System,
) {
    private val episodeProgressInfos by episodeProgressInfos
    private val subjectId by subjectId

    @Stable
    fun episodeCacheStatus(episodeId: Int): EpisodeCacheStatus? {
        return episodeProgressInfos.find { it.episode.id == episodeId }?.cacheStatus
    }

    fun play(episodeId: Int) {
        onPlay(subjectId, episodeId)
    }

    private val continueWatchingStatus by derivedStateOf {
        info.value?.continueWatchingStatus
    }

    /**
     * 是否拥有至少一话, 并且已经观看了这一话, 并且没有更新的了.
     */
    val isLatestEpisodeWatched by derivedStateOf {
        continueWatchingStatus is ContinueWatchingStatus.Watched
    }

    /**
     * 已经完结并且看完了
     */
    val isDone by derivedStateOf {
        continueWatchingStatus == ContinueWatchingStatus.Done
    }

    val episodeIdToPlay: Int? by derivedStateOf {
        info.value?.nextEpisodeIdToPlay
    }

    val buttonText by derivedStateOf {
        when (val s = continueWatchingStatus) {
            is ContinueWatchingStatus.Continue -> "继续观看 ${s.episodeSort}"
            ContinueWatchingStatus.Done -> "已看完"
            is ContinueWatchingStatus.NotOnAir -> {
                val date = s.airDate.toLocalDateOrNull()
                if (date != null) {
                    val week = weekFormatter.format(date)
                    "${week}开播"
                } else {
                    "还未开播"
                }
            }

            ContinueWatchingStatus.Start -> "开始观看"
            is ContinueWatchingStatus.Watched -> {
                val date = s.nextEpisodeAirDate.toLocalDateOrNull()
                if (date != null) {
                    val week = weekFormatter.format(date)
                    "${week}更新"
                } else {
                    "看过 ${s.episodeSort}"
                }
            }

            null -> "未知"
        }
    }

    val buttonIsPrimary by derivedStateOf {
        when (continueWatchingStatus) {
            is ContinueWatchingStatus.Start,
            is ContinueWatchingStatus.Continue -> true

            else -> false
        }
    }

    fun onClickButton() {
        episodeIdToPlay?.let { play(it) }
    }
}
