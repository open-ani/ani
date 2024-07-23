package me.him188.ani.app.ui.subject.components.comment

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.suspendCancellableCoroutine
import me.him188.ani.app.data.models.episode.EpisodeComment
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.tools.caching.LazyDataCacheContext
import me.him188.ani.app.tools.caching.RefreshOrderPolicy
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.richtext.toUIBriefText
import me.him188.ani.app.ui.foundation.richtext.toUIRichElements
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.utils.bbcode.BBCode
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

/**
 * A tool to load and parse comment from source to ui object
 */
class CommentLoader<T>(
    source: Flow<LazyDataCache<T>>,
    coroutineContext: CoroutineContext,
    uiMapper: suspend CommentMapperContext.(T) -> UiComment,
) : HasBackgroundScope by BackgroundScope(coroutineContext) {
    private val cachedSource = source.shareInBackground()

    val list = cachedSource
        .flatMapLatest {
            sourceVersion.value = Any()
            it.cachedDataFlow
        }
        .map { list ->
            list.map { with(CommentMapperContext) { uiMapper(it) } }
                .sortedByDescending { it.createdAt }
        }

    val hasFinished = cachedSource.flatMapLatest { it.isCompleted }
    val sourceVersion = MutableStateFlow(Any())

    init {
        cachedSource
            .onEach { sourceVersion.emit(Any()) }
            .launchIn(backgroundScope)
    }

    suspend fun reload() {
        cachedSource.first().refresh(RefreshOrderPolicy.REPLACE)
    }

    suspend fun loadMore() {
        cachedSource.first().requestMore()
    }

    companion object {
        /**
         * create comment loader of episode
         */
        fun episode(
            episodeId: Flow<Int>,
            coroutineContext: CoroutineContext,
            episodeCommentSource: suspend LazyDataCacheContext.(Int) -> PagedSource<EpisodeComment>
        ) = CommentLoader(
            source = episodeId.map { eid ->
                LazyDataCache(
                    createSource = { episodeCommentSource(eid) },
                    getKey = { it.id },
                    debugName = "episodeComment-$eid",
                )
            },
            coroutineContext = coroutineContext,
            uiMapper = { comment ->
                UiComment(
                    id = comment.toString(),
                    creator = comment.creator,
                    content = parseBBCode(comment.content),
                    createdAt = comment.createdAt * 1000L,
                    briefReplies = comment.replies.take(3).map { reply ->
                        UiComment(
                            id = reply.toString(),
                            creator = reply.creator,
                            content = parseBBCodeAsReply(reply.content),
                            createdAt = reply.createdAt * 1000L,
                            briefReplies = emptyList(),
                            replyCount = 0,
                        )
                    },
                    replyCount = comment.replies.size,
                )
            },
        )
    }
}

object CommentMapperContext {
    suspend fun parseBBCode(code: String): UIRichText = suspendCancellableCoroutine { cont ->
        val richText = try {
            BBCode.parse(code)
        } catch (ex: Exception) {
            cont.cancel()
            return@suspendCancellableCoroutine
        }
        cont.resume(UIRichText(richText.toUIRichElements()))
    }

    suspend fun parseBBCodeAsReply(code: String): UIRichText = suspendCancellableCoroutine { cont ->
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