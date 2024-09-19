/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector.episode

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import me.him188.ani.app.data.source.media.resolver.WebViewVideoExtractor
import me.him188.ani.app.data.source.media.source.web.SelectorMediaSourceEngine
import me.him188.ani.app.data.source.media.source.web.SelectorSearchConfig
import me.him188.ani.app.platform.Context
import me.him188.ani.app.ui.settings.mediasource.BackgroundSearcher
import me.him188.ani.app.ui.settings.mediasource.launchCollectedInBackground
import me.him188.ani.app.ui.settings.mediasource.selector.test.SelectorTestEpisodePresentation
import me.him188.ani.datasources.api.matcher.WebVideo
import me.him188.ani.utils.platform.Uuid
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

/**
 * 测试 [me.him188.ani.datasources.api.matcher.WebVideoMatcher]
 */
@Stable
class SelectorEpisodeState(
    private val itemState: State<SelectorTestEpisodePresentation?>,
    /**
     * null means loading. Should finally have one.
     */
    matchVideoConfigState: State<SelectorSearchConfig.MatchVideoConfig?>,
    /**
     * null means loading. Should finally have one.
     */
    private val webViewVideoExtractor: State<WebViewVideoExtractor?>,
    private val engine: SelectorMediaSourceEngine,
    backgroundScope: CoroutineScope,
    context: Context,
    flowDispatcher: CoroutineContext = Dispatchers.Default,
) {
    private var _lastNonNullId: Uuid = Uuid.Companion.random()
    val lastNonNullId by derivedStateOf {
        itemState.value?.id?.also { _lastNonNullId = it } ?: _lastNonNullId
    }

    val episodeName: String by derivedStateOf { itemState.value?.name ?: "" }
    val episodeUrl: String by derivedStateOf { itemState.value?.playUrl ?: "" }

    /**
     * 该页面的所有链接
     */
    val searcher =
        BackgroundSearcher(
            backgroundScope,
            testDataState = derivedStateOf { itemState.value?.playUrl to webViewVideoExtractor.value },
        ) { (episodeUrl, extractor) ->
            launchCollectedInBackground<String, _>(
                updateState = { SelectorEpisodeResult.InProgress(it) },
            ) { flow ->
                try {
                    if (episodeUrl != null && extractor != null) {
                        withTimeoutOrNull(30.seconds) { // timeout considered as success
                            extractor.getVideoResourceUrl(context, episodeUrl) {
                                collect(it)
                                null
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    SelectorEpisodeResult.UnknownError(e)
                }
                SelectorEpisodeResult.Success(flow)
            }
        }

    @Immutable
    data class MatchResult(
        val originalUrl: String,
        val video: WebVideo?,
    ) {
        @Stable
        fun isMatch() = video != null
    }

    val isSearchingInProgress get() = searcher.isSearching

    /**
     * 不断更新的匹配结果
     */
    val matchResults: Flow<List<MatchResult>> by derivedStateOf {
        val matchVideoConfig = matchVideoConfigState.value ?: return@derivedStateOf emptyFlow()
        val searchResult = searcher.searchResult ?: return@derivedStateOf emptyFlow()
        val flow = when (searchResult) {
            is SelectorEpisodeResult.ApiError,
            is SelectorEpisodeResult.UnknownError,
            is SelectorEpisodeResult.InvalidConfig,
                -> return@derivedStateOf emptyFlow()

            is SelectorEpisodeResult.InProgress -> searchResult.flow
            is SelectorEpisodeResult.Success -> searchResult.flow
        }

        flow.map { list ->
            list.asSequence()
                .map { original ->
                    MatchResult(original, engine.matchWebVideo(original, matchVideoConfig))
                }
                .distinctBy { it.originalUrl } // O(n) extra space, O(1) time
                .toMutableList() // single list instance construction
                .apply {
                    // sort in-place for better performance
                    sortByDescending { it.isMatch() } // 优先展示匹配的
                }
        }.flowOn(flowDispatcher) // possibly significant computation
    }
}