package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.data.models.episode.EpisodeComment
import me.him188.ani.app.data.repository.EpisodeRevisionRepository
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.tools.caching.RefreshOrderPolicy
import me.him188.ani.app.ui.foundation.AbstractViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EpisodeCommentViewModel(
    episodeId: Int,
    private val pullToRefreshState: PullToRefreshState
) : AbstractViewModel(), KoinComponent {
    private val revisionRepository by inject<EpisodeRevisionRepository>()

    private val dataCache = LazyDataCache(
        createSource = { revisionRepository.getSubjectEpisodeComments(episodeId) },
        getKey = { comment -> comment.id },
        debugName = "episodeComment-$episodeId",
    )

    private val initialLoadComplete = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = dataCache.isCompleted
        .combine(initialLoadComplete) { exhausted, initialCompleted ->
            if (initialCompleted) false else exhausted
        }
        .stateInBackground(false)
    val list: StateFlow<List<UiComment>> = dataCache.cachedDataFlow
        .map { it.map(EpisodeComment::toUiComment) }
        .stateInBackground(listOf())

    fun reloadComments() {
        backgroundScope.launch {
            pullToRefreshState.startRefresh()
            dataCache.refresh(RefreshOrderPolicy.REPLACE)
            pullToRefreshState.endRefresh()
        }
    }

    fun loadMoreComments() {
        backgroundScope.launch {
            dataCache.requestMore()
        }
    }
}

@Immutable
class UiComment(
    val id: String,
    val creator: UserInfo,
    val summary: String,
    val createdAt: Long, // timestamp millis
    val replies: List<UiComment>
)

fun EpisodeComment.toUiComment(): UiComment {
    return UiComment(
        id = id.toString(),
        creator = creator,
        summary = content,
        createdAt = createdAt * 1000L,
        replies = replies.map { it.toUiComment() },
    )
}