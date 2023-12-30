/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.app.app

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import me.him188.animationgarden.api.DmhyClient
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlow
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlowImpl
import me.him188.animationgarden.api.model.SearchQuery
import me.him188.animationgarden.api.model.SearchSession
import me.him188.animationgarden.api.model.Topic
import me.him188.animationgarden.api.tags.Episode
import me.him188.animationgarden.app.app.data.AppData
import me.him188.animationgarden.app.app.data.AppDataSynchronizer
import me.him188.animationgarden.app.app.data.StarredAnimeMutations
import me.him188.animationgarden.app.ui.search.OrganizedViewState
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Stable
class ApplicationState(
    initialClient: DmhyClient,
    appDataSynchronizer: (CoroutineScope) -> AppDataSynchronizer,
    @Stable
    val applicationScope: CoroutineScope = createApplicationScope(),
) {

    @Stable
    val searchQuery: MutableState<SearchQuery> = mutableStateOf(SearchQuery())

    @Stable
    val topicsFlow: KeyedMutableListFlow<String, Topic> = KeyedMutableListFlowImpl { it.id }

    @Stable
    val client: MutableState<DmhyClient> = mutableStateOf(initialClient)

    @Stable
    val session: MutableState<SearchSession> by lazy {
        mutableStateOf(client.value.startSearchSession(SearchQuery()))
    }


    // derived
    @Stable
    val topicsFlowState: State<StateFlow<List<Topic>>> = derivedStateOf { topicsFlow.asFlow() }

    val starredAnimeListState: State<List<StarredAnime>>
        @Composable
        get() {
            val data by dataState
            return data.starredAnime.asFlow().map { it.reversed() }.collectAsState(listOf())
        }

    @Stable
    val organizedViewState: OrganizedViewState = OrganizedViewState()


    @Stable
    val dataSynchronizer: AppDataSynchronizer = appDataSynchronizer(applicationScope)

    val dataState: State<AppData>
        @Composable get() = dataSynchronizer.appDataFlow.collectAsState(AppData.initial, context = Dispatchers.Main)

    suspend fun getData() = withContext(Dispatchers.Default) { dataSynchronizer.getData() }


    @Stable
    val fetcher: Fetcher =
        Fetcher(
            CoroutineScope(applicationScope.coroutineContext + SupervisorJob(applicationScope.coroutineContext.job)),
            onFetchSucceed = {
                // update starred anime on success
                searchQuery.value.keywords?.let { query ->
                    launchDataSynchronization {
                        commit(StarredAnimeMutations.UpdateRefreshed(query, query, organizedViewState))
                    }
                }
            }
        )

    inline fun launchDataSynchronization(crossinline action: suspend context(ApplicationState, CoroutineScope, AppDataSynchronizer) () -> Unit) { // AppDataSynchronizer should be extension receiver.
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            action(this@ApplicationState, this, dataSynchronizer)
        }
    }

    fun updateSearchQuery(searchQuery: SearchQuery) {
        this.searchQuery.value = searchQuery
        fetcher.hasMorePages.value = true
        fetcher.fetchingState.value = FetchingState.Idle
        topicsFlow.value = listOf()
        session.value = client.value.startSearchSession(searchQuery)
        launchFetchNextPage(!searchQuery.keywords.isNullOrBlank())
    }

    fun launchFetchNextPage(
        continuous: Boolean,
    ) {
        fetcher.launchFetchNextPage(continuous, session.value, topicsFlow)
    }


    @Composable
    fun isEpisodeWatched(episode: Episode): Boolean {
        val data by dataState
        val starredAnime by remember {
            data.starredAnime.asFlow()
                .map { list -> list.find { it.searchQuery == searchQuery.value.keywords } }
        }.collectAsState(null)

        return starredAnime?.watchedEpisodes?.contains(episode) == true
    }

    suspend fun markEpisodeWatched(episode: Episode) {
        val id = searchQuery.value.keywords ?: return
        dataSynchronizer.commit(StarredAnimeMutations.EpisodeWatched(id, episode))
    }
}

fun createApplicationScope(parentCoroutineContext: CoroutineContext = EmptyCoroutineContext) =
    CoroutineScope(parentCoroutineContext + SupervisorJob() + CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    })

fun ApplicationState.doSearch(keywords: String?) {
    updateSearchQuery(SearchQuery(keywords?.trim(), null, null))
}


@Composable
fun ApplicationState.rememberCurrentStarredAnimeState(): State<StarredAnime?> {
    val currentStarredAnimeList by starredAnimeListState
    val starredAnimeState = remember {
        derivedStateOf {
            currentStarredAnimeList.find { it.searchQuery == searchQuery.value.keywords }
        }
    }
    val currentStarredAnime by starredAnimeState

    val currentTopics by remember { topicsFlow.asFlow() }.collectAsState()
    val currentSearchQuery by searchQuery
    LaunchedEffect(currentStarredAnime, currentTopics, currentSearchQuery) {
        organizedViewState.apply {
            selectedAlliance.value = currentStarredAnime?.preferredAlliance
            selectedResolution.value = currentStarredAnime?.preferredResolution
            selectedSubtitleLanguage.value = currentStarredAnime?.preferredSubtitleLanguage

            setTopics(currentTopics, currentSearchQuery.keywords)
        }
    }
    return starredAnimeState
}

/*
客户端每次修改推送:
- 上次的 sync token
- 本次 commit
- 本次修改后的全部数据

服务器验证数据:
- 当修改基于 HEAD:
  - 更新数据库
  - 为当前数据生成 sync token
  - 返回 sync token 给请求方
  - 同步 commit 到其他客户端
- 否则拒绝修改:
  - 返回最新全部数据和 sync token
  - 客户端 rebase 并重试

解决冲突: 选择使用本地或者使用服务器存档覆盖


一期做无冲突同步和 force push, 二期做 rebase, 三期做 history
 */
