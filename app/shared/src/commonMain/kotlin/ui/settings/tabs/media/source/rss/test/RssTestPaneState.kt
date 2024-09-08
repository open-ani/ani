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
import me.him188.ani.app.data.models.fold
import me.him188.ani.app.data.source.media.source.RssMediaSourceEngine
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.settings.tabs.media.source.rss.detail.RssViewingItem
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.source.DownloadSearchQuery
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

@Stable
class RssTestPaneState(
//    private val tester: RssMediaSourceTester,
    private val searchUrlState: State<String>,
    private val engine: RssMediaSourceEngine,
    backgroundScope: CoroutineScope,
) {
    var searchKeywordPlaceholder = SampleKeywords.random()
    var searchKeyword: String by mutableStateOf(searchKeywordPlaceholder)

    var sort: String by mutableStateOf("1")

    val showPage by derivedStateOf {
        searchUrlState.value.contains("{page}")
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
        val searchUrl = searchUrlState.value
        RssTestData(
            searchUrl = searchUrl,
            page = page,
            keyword = finalKeyword,
        )
    }

    fun restartCurrentSearch() {
        restartSearch(testData)
    }

    private fun restartSearch(testData: RssTestData) {
        val query = DownloadSearchQuery(
            keywords = testData.keyword,
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

    private suspend fun doSearch(
        testData: RssTestData,
        query: DownloadSearchQuery
    ): RssTestResult {
        try {
            val res = engine.search(
                testData.searchUrl, query, testData.page,
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
        val (encodedUrl, _, document, channel, matchedMediaList) = result
        document ?: return RssTestResult.EmptyResult
        channel ?: return RssTestResult.EmptyResult
        matchedMediaList ?: return RssTestResult.EmptyResult

        return RssTestResult.Success(
            encodedUrl.toString(),
            channel,
            channel.items.map {
                RssItemPresentation(it)
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
                    .debounce(1.seconds)
                    .collect {
                        restartSearch(it)
                    }
            }
        }
    }
}

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
