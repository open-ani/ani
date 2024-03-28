/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.ui.collection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.foundation.pagerTabIndicatorOffset
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.profile.UnauthorizedTips
import me.him188.ani.datasources.api.CollectionType
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle


// 有顺序, https://github.com/Him188/ani/issues/73
@Stable
private val COLLECTION_TABS = listOf(
    CollectionType.Dropped,
    CollectionType.OnHold,
    CollectionType.Doing,
    CollectionType.Wish,
    CollectionType.Done,
)

/**
 * 追番列表
 */
@Composable
fun CollectionPage(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val vm = rememberViewModel { MyCollectionsViewModel() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的追番") },
            )
//            AniTopAppBar(Modifier.background(MaterialTheme.colorScheme.surface)) {
//                Text("我的追番", style = MaterialTheme.typography.titleMedium)
//            }
        }
    ) { topBarPaddings ->
//        Text("我的追番", Modifier.padding(all = 16.dp), style = MaterialTheme.typography.headlineMedium)

        val isLoggedIn by vm.isLoggedIn.collectAsStateWithLifecycle(true)


        val pagerState = rememberPagerState(initialPage = COLLECTION_TABS.size / 2) { COLLECTION_TABS.size }
        val scope = rememberCoroutineScope()

        Column(Modifier.padding(topBarPaddings).fillMaxSize()) {
            SecondaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = @Composable { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                COLLECTION_TABS.forEachIndexed { index, collectionType ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(text = collectionType.displayText())
                        }
                    )
                }
            }

            val isLoading by vm.isLoading.collectAsStateWithLifecycle()

            HorizontalPager(state = pagerState, Modifier.fillMaxSize()) { index ->
                val collections by vm.collectionsByType(COLLECTION_TABS[index])
                if (collections.isEmpty()) {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(32.dp))
                        when {
                            isLoading -> {
                                CircularProgressIndicator()
                            }

                            !isLoggedIn -> {
                                UnauthorizedTips(Modifier.fillMaxSize())
                            }

                            else -> {
                                Text("~ 空空如也 ~", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                } else {
                    MyCollectionColumn(
                        collections,
                        vm,
                        contentPadding,
                        Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

private fun CollectionType.displayText(): String {
    return when (this) {
        CollectionType.Wish -> "想看"
        CollectionType.Doing -> "在看"
        CollectionType.Done -> "看过"
        CollectionType.OnHold -> "搁置"
        CollectionType.Dropped -> "抛弃"
        CollectionType.NotCollected -> "未收藏"
    }
}

@Composable
internal expect fun PreviewCollectionPage()


@Composable
private fun PaddingValues.inner(contentPadding: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + contentPadding.calculateStartPadding(layoutDirection),
        top = calculateTopPadding() + contentPadding.calculateTopPadding(),
        end = calculateEndPadding(layoutDirection) + contentPadding.calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + contentPadding.calculateBottomPadding(),
    )
}