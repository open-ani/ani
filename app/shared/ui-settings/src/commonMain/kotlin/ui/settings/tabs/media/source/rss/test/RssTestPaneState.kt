package me.him188.ani.app.ui.settings.tabs.media.source.rss.test

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import me.him188.ani.app.data.models.fold
import me.him188.ani.app.data.source.media.source.RssMediaSourceEngine
import me.him188.ani.app.data.source.media.source.RssSearchConfig
import me.him188.ani.app.data.source.media.source.RssSearchQuery
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.settings.tabs.media.source.rss.EditRssMediaSourceState
import me.him188.ani.app.ui.settings.tabs.media.source.rss.detail.RssViewingItem
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

/**
 * 测试功能的状态
 *
 * @see EditRssMediaSourceState
 * @see RssTestResult
 * @see RssTestData
 * @see RssItemPresentation.compute
 */
@Stable
class RssTestPaneState(
    private val searchConfigState: State<RssSearchConfig>,
    private val engine: RssMediaSourceEngine,
    backgroundScope: CoroutineScope,
) {
    var searchKeywordPlaceholder = SampleKeywords.random()
    var searchKeyword: String by mutableStateOf(searchKeywordPlaceholder)

    var sort: String by mutableStateOf("1")

    val showPage by derivedStateOf {
        searchConfigState.value.searchUrl.contains("{page}")
    }
    var pageString: String by mutableStateOf("0")
    private val page by derivedStateOf {
        pageString.toIntOrNull()
    }
    val pageIsError by derivedStateOf {
        page == null
    }

    fun randomKeyword() {
        val newRandom = SampleKeywords.random()
        searchKeywordPlaceholder = newRandom
        searchKeyword = newRandom
    }

    // 持有 state 以保证切换页面时仍然有滚动状态
    val pagerState = PagerState(RssTestPaneTab.Overview.ordinal) { RssTestPaneTab.entries.size }
    val overallTabGridState = LazyGridState()
    val rssTabGridState = LazyStaggeredGridState()
    val finalResultsTabGridState = LazyStaggeredGridState()

    ///////////////////////////////////////////////////////////////////////////
    // searching
    ///////////////////////////////////////////////////////////////////////////

    private val searchTasker = MonoTasker(backgroundScope)
    val isSearching by searchTasker::isRunning
    var searchResult: RssTestResult? by mutableStateOf(null)

    var viewingItem: RssViewingItem? by mutableStateOf(null)

    fun viewDetails(media: Media) {
        viewingItem = RssViewingItem.ViewingMedia(media)
    }

    fun viewDetails(rssItem: RssItemPresentation) {
        viewingItem = RssViewingItem.ViewingRssItem(rssItem)
    }

    /**
     * 当前的请求
     */
    private val testData by derivedStateOf {
        val finalKeyword = searchKeyword.ifEmpty { searchKeywordPlaceholder }
        val searchUrl = searchConfigState.value
        RssTestData(
            page = page,
            keyword = finalKeyword,
            searchUrl,
        )
    }

    fun restartCurrentSearch() {
        restartSearch(testData)
    }

    private fun restartSearch(testData: RssTestData) {
        val query = RssSearchQuery(
            subjectName = testData.keyword,
            episodeSort = EpisodeSort(sort),
        )
        searchResult = null
        viewingItem = null
        searchTasker.launch {
            // background scope
            val res = doSearch(testData, query)
            withContext(Dispatchers.Main) {
                searchResult = res
            }
        }
    }

    // 只会抛出 CancellationException
    private suspend fun doSearch(
        testData: RssTestData,
        query: RssSearchQuery,
    ): RssTestResult {
        try {
            val res = engine.search(
                testData.searchConfig, query, testData.page,
                mediaSourceId = "test",
            )

            return res.fold(
                onSuccess = { result ->
                    convertResult(result)
                },
                onKnownFailure = {
                    RssTestResult.ApiError(it)
                },
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return RssTestResult.UnknownError(e)
        }
    }

    private fun convertResult(result: RssMediaSourceEngine.Result): RssTestResult {
        val (encodedUrl, query, document, channel, matchedMediaList) = result
        document ?: return RssTestResult.EmptyResult
        channel ?: return RssTestResult.EmptyResult
        matchedMediaList ?: return RssTestResult.EmptyResult

        return RssTestResult.Success(
            encodedUrl.toString(),
            channel,
            channel.items.map {
                RssItemPresentation.compute(it, searchConfigState.value, query)
            },
            matchedMediaList,
            origin = document,
        )
    }

    /**
     * 在 UI 调用, 当测试数据变化时重新搜索
     */
    suspend fun observeChangeLoop() {
        withContext(Dispatchers.Main.immediate) {
            while (true) {
                snapshotFlow { testData }
                    .distinctUntilChanged()
                    .debounce(0.5.seconds)
                    .collect {
                        restartSearch(it)
                    }
            }
        }
    }
}

/**
 * 用于测试的数据
 */
@Serializable
private data class RssTestData(
    val page: Int?,
    val keyword: String,
    val searchConfig: RssSearchConfig,
)

@Stable
private val SampleKeywords
    get() = listOf(
        "败犬女主太多了！",
        "白箱",
        "CLANNAD",
        "轻音少女",
        "战姬绝唱",
        "凉宫春日的忧郁",
        "樱 Trick",
        "命运石之门",
    )
