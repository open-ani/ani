/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.repository.SubjectSearchRepository
import me.him188.ani.app.data.repository.TrendsRepository
import me.him188.ani.app.domain.search.SubjectProvider
import me.him188.ani.app.domain.search.SubjectSearchQuery
import me.him188.ani.app.domain.search.SubjectSearcher
import me.him188.ani.app.domain.session.OpaqueSession
import me.him188.ani.app.domain.session.SessionManager
import me.him188.ani.app.domain.session.userInfo
import me.him188.ani.app.ui.exploration.ExplorationPage
import me.him188.ani.app.ui.exploration.ExplorationPageState
import me.him188.ani.app.ui.exploration.search.SearchPage
import me.him188.ani.app.ui.exploration.search.SearchPageState
import me.him188.ani.app.ui.exploration.trends.TrendingSubjectsState
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.AuthState
import me.him188.ani.app.ui.subject.details.SubjectDetailsScene
import me.him188.ani.app.ui.subject.details.SubjectDetailsViewModel
import me.him188.ani.utils.coroutines.onReplacement
import me.him188.ani.utils.coroutines.retryUntilSuccess
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class ExplorationTabViewModel : AbstractViewModel(), KoinComponent {
    private val trendsRepository: TrendsRepository by inject()
    private val sessionManager: SessionManager by inject()
    private val searchHistoryRepository: SubjectSearchRepository by inject()

    private val authState = AuthState()

    @OptIn(OpaqueSession::class)
    private val selfInfoState = sessionManager.userInfo.produceState(null)

    private val subjectProvider: SubjectProvider by inject()
    private val subjectManager: SubjectManager by inject()

    private val searcher = SubjectSearcher(subjectProvider, backgroundScope.coroutineContext)
    private val queryState = mutableStateOf("")

    val explorationPageState: ExplorationPageState = ExplorationPageState(
        authState,
        selfInfoState,
        TrendingSubjectsState(
            suspend { trendsRepository.getTrending() }
                .asFlow()
                .map { it.getOrNull() }
                .retryUntilSuccess()
                .map { it?.subjects }
                .produceState(null),
        ),
    )

    val searchPageState: SearchPageState = SearchPageState(
        searchHistoryState = searchHistoryRepository.getHistoryFlow().produceState(emptyList()),
        suggestionsState = searchHistoryRepository.getHistoryFlow()
            .produceState(emptyList()),// todo: suggestions
        onSearch = { searcher.search(SubjectSearchQuery(keyword = queryState.value)) },
        onRequestPlay = { info ->
            subjectManager.subjectCollectionFlow(info.id).first().episodes.firstOrNull()?.let {
                SearchPageState.EpisodeTarget(info.id, it.episodeInfo.id)
            }
        },
        queryState = queryState,
        backgroundScope,
    )

    val subjectDetailsViewModelFlow = snapshotFlow { searchPageState.selectedItem }
        .flowOn(Dispatchers.Main)
        .map {
            SubjectDetailsViewModel(it?.id ?: return@map null)
        }
        .onReplacement {
            it?.cancelScope()
        }
        .flowOn(Dispatchers.Default)
        .shareInBackground()

    override fun onCleared() {
        super.onCleared()
        subjectDetailsViewModelFlow.replayCache.firstOrNull()?.cancelScope()
    }
}

@Composable
internal fun ExplorationTab(
    windowInsets: WindowInsets,
    modifier: Modifier = Modifier,
    vm: ExplorationTabViewModel = viewModel { ExplorationTabViewModel() },
) {
    val navController = rememberNavController()
    NavHost(
        navController,
        startDestination = Routes.EXPLORATION,
        modifier,
    ) {
        composable<Routes.EXPLORATION> {
            ExplorationPage(
                vm.explorationPageState,
                onSearch = {
                    navController.navigate(Routes.SEARCH) {
                        launchSingleTop = true
                    }
                },
                Modifier.fillMaxSize(),
                contentWindowInsets = windowInsets,
            )
        }
        composable<Routes.SEARCH> {
            SearchPage(
                vm.searchPageState,
                windowInsets,
                detailContent = {
                    vm.subjectDetailsViewModelFlow.collectAsStateWithLifecycle(null).value?.let {
                        SubjectDetailsScene(it)
                    }
                },
                Modifier.fillMaxSize(),
            )
        }
    }
}

@Serializable
private sealed class Routes {
    @Serializable
    @SerialName("EXPLORATION")
    data object EXPLORATION : Routes()

    @Serializable
    @SerialName("SEARCH")
    data object SEARCH : Routes()
}