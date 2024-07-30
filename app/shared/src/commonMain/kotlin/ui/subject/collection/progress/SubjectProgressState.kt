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
import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.data.models.episode.EpisodeProgressInfo
import me.him188.ani.app.data.models.subject.SubjectCollection
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.source.media.EpisodeCacheStatus
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
@Stable
class SubjectProgressState(
    subjectId: State<Int>,
    info: State<SubjectProgressInfo>,
    episodeProgressInfos: State<List<EpisodeProgressInfo>>,
    private val onPlay: (subjectId: Int, episodeId: Int) -> Unit,
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

    val continueWatchingStatus by derivedStateOf {
        info.value.continueWatchingStatus
    }

    val episodeToPlay: EpisodeInfo? by derivedStateOf {
        info.value.nextEpisodeToPlay
    }
}
