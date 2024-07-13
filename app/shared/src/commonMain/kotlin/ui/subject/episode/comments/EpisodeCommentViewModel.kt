package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.app.data.repository.EpisodeRevisionRepository
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.tools.caching.RefreshOrderPolicy
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.richtext.RichTextDefaults
import me.him188.ani.app.ui.foundation.richtext.UIRichElement
import me.him188.ani.app.ui.foundation.richtext.toUIBriefText
import me.him188.ani.app.ui.foundation.richtext.toUIRichElements
import me.him188.ani.utils.bbcode.BBCode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.resume

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

    private val freshLoaded = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = dataCache.isCompleted
        .combine(freshLoaded) { exhausted, initialCompleted ->
            if (initialCompleted) false else exhausted
        }
        .stateInBackground(false)
    val list: StateFlow<List<UiComment>> = dataCache.cachedDataFlow
        .map {
            it.map { comment ->
                UiComment(
                    id = comment.toString(),
                    creator = comment.creator,
                    content = parseBBCode(comment.content),
                    createdAt = comment.createdAt * 1000L,
                    briefReplies = comment.replies.take(3).map { reply ->
                        UiComment(
                            id = comment.toString(),
                            creator = comment.creator,
                            content = parseBBCodeAsReply(reply.content),
                            createdAt = comment.createdAt * 1000L,
                            briefReplies = emptyList(),
                            replyCount = 0,
                        )
                    },
                    replyCount = comment.replies.size,
                )
            }
        }
        .stateInBackground(listOf())

    fun reloadComments() {
        backgroundScope.launch {
            freshLoaded.value = false
            pullToRefreshState.startRefresh()

            dataCache.refresh(RefreshOrderPolicy.REPLACE)

            freshLoaded.value = true
            pullToRefreshState.endRefresh()
        }
    }

    fun loadMoreComments() {
        backgroundScope.launch {
            dataCache.requestMore()
        }
    }

    private suspend fun parseBBCode(code: String): UIRichText = suspendCancellableCoroutine { cont ->
        val richText = try {
            BBCode.parse(code)
        } catch (ex: Exception) {
            cont.cancel()
            return@suspendCancellableCoroutine
        }
        cont.resume(UIRichText(richText.toUIRichElements()))
    }

    private suspend fun parseBBCodeAsReply(
        code: String,
    ): UIRichText = suspendCancellableCoroutine { cont ->
        val richText = try {
            BBCode.parse(code)
        } catch (ex: Exception) {
            cont.cancel()
            return@suspendCancellableCoroutine
        }

        listOf(richText.toUIBriefText().copy(maxLine = 2))
            .also { cont.resume(UIRichText(it)) }
    }
}

fun UIRichText.prependText(prependix: String, color: Color): UIRichText = run {
    // 如果 elements 是空的则直接返回一个 annotated text
    val first = elements.firstOrNull()
        ?: return@run listOf(
            UIRichElement.AnnotatedText(
                listOf(UIRichElement.Annotated.Text(prependix, RichTextDefaults.FontSize, color)),
            ),
        )

    // 如果第一个 element 是 annotated text，则把 prepend 添加到其中
    if (first is UIRichElement.AnnotatedText) {
        listOf(
            first.copy(
                slice = listOf(
                    UIRichElement.Annotated.Text(prependix, RichTextDefaults.FontSize, color),
                    *first.slice.toTypedArray(),
                ),
            ),
            *elements.drop(1).toTypedArray(),
        )
    } else { // 如果不是就添加一个 annotated text
        listOf(
            UIRichElement.AnnotatedText(
                listOf(UIRichElement.Annotated.Text(prependix, RichTextDefaults.FontSize, color)),
            ),
            *elements.toTypedArray(),
        )
    }
}.let {
    UIRichText(it)
}

@Immutable
class UIRichText(val elements: List<UIRichElement>)

@Immutable
class UiComment(
    val id: String,
    val creator: UserInfo,
    val content: UIRichText,
    val createdAt: Long, // timestamp millis
    val briefReplies: List<UiComment>,
    val replyCount: Int
)

