/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.transformLatest
import me.him188.ani.app.tools.ldc.LazyDataCache
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.widgets.FastLinearProgressIndicator
import me.him188.ani.utils.platform.annotations.TestOnly
import kotlin.coroutines.CoroutineContext


/**
 * 通用的搜索状态.
 *
 * 数据实现: [LdcSearchState]
 *
 * UI: [SearchDefaults]
 *
 * All methods must be called on the main thread.
 */
@Stable
abstract class SearchState<T> {
    abstract val stage: SearchStage

    /**
     * 当前搜索的总物品数量. 这个值应当在展示第一份结果时就已经设置, 并且在 [requestMore] 时保持不变.
     *
     * 当没有开始搜索时, 此属性可以是任意值.
     *
     * `null` 表示未知.
     */
    abstract val totalItemCount: Int?

    /**
     * 是否还有更多的物品可以请求.
     *
     * 当没有开始搜索时, 此属性可以是任意值.
     */
    abstract val hasMoreItems: Boolean

    /**
     * 当前搜索的物品. 如果搜索未开始, 则此 flow 会 emit 空 list.
     * 当清空搜索结果或重新开始搜索时, 此 flow 都会立即 emit 空列表以清空旧数据.
     */
    abstract val items: List<T>

    /**
     * 当且仅当搜索完成且没有任何结果时为 `true`.
     */
    val isEmpty by derivedStateOf {
        (stage == SearchStage.Finished) && items.isEmpty()
    }

    /**
     * 请求下一页.
     *
     * 如果没有更多的物品, 则不会有任何效果. 如果搜索未开始, 也不会有任何效果.
     *
     * 此函数是挂起函数, 但也仍然需要在主线程移动. 它将一直挂起, 直到下一页的数据准备好.
     * 异常将会被捕获, 因此不会 crash UI. 此函数支持 coroutine cancellation (例如退出到当前页面, 届时会中止搜索以释放资源).
     * 具体行为参考 [LazyDataCache.requestMore].
     */
    abstract suspend fun requestMore() // if there is more

    /**
     * 清空当前所有的结果并且重新开始搜索.
     */
    abstract fun startSearch()

    /**
     * 清空所有搜索结果. [状态][stage]将被重置为 [SearchStage.Idle].
     */
    abstract fun clear()
}

@Stable
val SearchState<*>.showSearchProgressIndicator: Boolean
    get() = stage == SearchStage.LoadingFirstPage

@Stable
val SearchState<*>.showLoadingMore: Boolean
    get() = stage == SearchStage.FirstPageReady

@Stable
val SearchState<*>.showSearchResultSummary: Boolean
    get() = stage != SearchStage.Idle

@Immutable
sealed class SearchStage {
    data object Idle : SearchStage()
    data object LoadingFirstPage : SearchStage()
    data object FirstPageReady : SearchStage()
    data object Finished : SearchStage()
}

/**
 * @see LazyDataCache
 */
@Stable
class LdcSearchState<T>(
    /**
     * 当 [startSearch] 时调用
     */
    private val createLdc: () -> LazyDataCache<T>,
    parentCoroutineContext: CoroutineContext,
) : SearchState<T>(), HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    override var stage: SearchStage by mutableStateOf(SearchStage.Idle)

    private val currentLdc: MutableStateFlow<LazyDataCache<T>?> = MutableStateFlow(null)

    override val items: List<T> by currentLdc.transformLatest { ldc ->
        if (ldc == null) {
            emit(emptyList())
        } else {
            emitAll(ldc.cachedDataFlow)
        }
    }.produceState(emptyList())

    override val totalItemCount: Int? by currentLdc.transformLatest { ldc ->
        if (ldc == null) {
            emit(null)
        } else {
            emitAll(ldc.totalSize)
        }
    }.produceState(null)

    override var hasMoreItems: Boolean by mutableStateOf(false)

    override suspend fun requestMore() {
        val currentLdc = currentLdc.value ?: return
        check(stage != SearchStage.Idle)
        if (currentLdc.requestMore()) {
            hasMoreItems = true
            if (stage == SearchStage.LoadingFirstPage) {
                stage = SearchStage.FirstPageReady
            }
        } else {
            hasMoreItems = false
            stage = SearchStage.Finished
        }
    }

    override fun startSearch() {
        clear()
        currentLdc.value = createLdc()
        hasMoreItems = true
        stage = SearchStage.LoadingFirstPage
    }

    override fun clear() {
        currentLdc.value = null
        hasMoreItems = false
        stage = SearchStage.Idle
    }
}

@TestOnly
class TestSearchState<T>(
    override val items: List<T>,
    override val stage: SearchStage = SearchStage.Finished,
    override val totalItemCount: Int? = 100,
    override val hasMoreItems: Boolean = true,
) : SearchState<T>() {
    override suspend fun requestMore() {
    }

    override fun startSearch() {
    }

    override fun clear() {
    }
}

@Stable
object SearchDefaults {
    @Composable
    fun <T> ResultColumn(
        state: SearchState<T>,
        modifier: Modifier = Modifier,
        listItemColors: ListItemColors = ListItemDefaults.colors(containerColor = Color.Unspecified),
        content: LazyListScope.() -> Unit,
    ) {
        LazyColumn(modifier.fillMaxWidth()) {
            stickyHeader {
                FastLinearProgressIndicator(
                    state.showSearchProgressIndicator,
                    Modifier.padding(vertical = 4.dp),
                    minimumDurationMillis = 300,
                )
            }

            item { Spacer(Modifier.height(Dp.Hairline)) } // 如果空白内容, 它可能会有 bug

            item {
                ListItem(
                    headlineContent = {
                        when {
                            state.isEmpty -> {
                                Text("无搜索记录")
                            }

                            state.showSearchResultSummary -> {
                                Text("搜索到 ${state.items.size} 个结果")
                            }
                        }
                    },
                    colors = listItemColors,
                )
            }

            content()

            item {
                LaunchedEffect(state.stage) {
                    state.requestMore()
                }
                if (state.showLoadingMore) {
                    ListItem(
                        headlineContent = {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        },
                        colors = listItemColors,
                    )
                }
            }
        }
    }
}
