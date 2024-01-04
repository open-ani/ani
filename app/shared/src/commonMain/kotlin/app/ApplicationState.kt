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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import me.him188.animationgarden.app.ui.search.OrganizedViewState
import me.him188.animationgarden.datasources.api.DownloadSearchQuery
import me.him188.animationgarden.datasources.api.SearchSession
import me.him188.animationgarden.datasources.api.topic.Episode
import me.him188.animationgarden.datasources.api.topic.Topic
import me.him188.animationgarden.datasources.dmhy.DmhyClient
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Stable
class ApplicationState(
    initialClient: DmhyClient,
    @Stable
    val applicationScope: CoroutineScope = createApplicationScope(),
) {

    @Stable
    val searchQuery: MutableStateFlow<DownloadSearchQuery> = MutableStateFlow(DownloadSearchQuery())

    @Stable
    val topicsFlow: MutableStateFlow<List<Topic>> = MutableStateFlow(listOf())

    @Stable
    val client: MutableState<DmhyClient> = mutableStateOf(initialClient)

    @Stable
    val session: MutableState<SearchSession<Topic>> by lazy {
        mutableStateOf(client.value.startSearchSession(DownloadSearchQuery()))
    }

    val starredAnimeListState: State<List<StarredAnime>>
        @Composable
        get() {
            TODO()
        }

    @Stable
    val organizedViewState: OrganizedViewState = OrganizedViewState()


    @Stable
    val fetcher: Fetcher =
        Fetcher(
            CoroutineScope(applicationScope.coroutineContext + SupervisorJob(applicationScope.coroutineContext.job)),
            onFetchSucceed = {
                // update starred anime on success
                searchQuery.value.keywords?.let { query ->
                    launchDataSynchronization {
                        TODO()
//                        commit(StarredAnimeMutations.UpdateRefreshed(query, query, organizedViewState))
                    }
                }
            }
        )

    inline fun launchDataSynchronization(crossinline action: suspend context(ApplicationState, CoroutineScope) () -> Unit) { // AppDataSynchronizer should be extension receiver.
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            TODO()
//            action(this@ApplicationState, this, dataSynchronizer)
        }
    }

    fun updateSearchQuery(searchQuery: DownloadSearchQuery) {
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
        TODO()
//        val data by dataState
//        val starredAnime by remember {
//            data.starredAnime.asFlow()
//                .map { list -> list.find { it.searchQuery == searchQuery.value.keywords } }
//        }.collectAsState(null)
//
//        return starredAnime?.watchedEpisodes?.contains(episode) == true
    }

    suspend fun markEpisodeWatched(episode: Episode) {
        val id = searchQuery.value.keywords ?: return
    }
}

fun createApplicationScope(parentCoroutineContext: CoroutineContext = EmptyCoroutineContext) =
    CoroutineScope(parentCoroutineContext + SupervisorJob() + CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    })

fun ApplicationState.doSearch(keywords: String?) {
    updateSearchQuery(DownloadSearchQuery(keywords?.trim(), null, null))
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

    val currentTopics by remember { topicsFlow.asStateFlow() }.collectAsState()
    val currentSearchQuery by searchQuery.collectAsState()
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
