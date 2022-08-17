package me.him188.animationgarden.desktop.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlow
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlowImpl
import me.him188.animationgarden.api.model.SearchFilter
import me.him188.animationgarden.api.model.SearchSession
import me.him188.animationgarden.api.model.Topic

@Stable
class ApplicationState(
    private val client: AnimationGardenClient,
) {
    @Stable
    val applicationScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
            throwable.printStackTrace()
        })


    @Stable
    val fetchingState: MutableStateFlow<FetchingState> = MutableStateFlow(FetchingState.Idle)

    @Stable
    val hasMorePages = MutableStateFlow(true)

    @Stable
    val searchFilter: MutableState<SearchFilter> = mutableStateOf(SearchFilter())

    @Stable
    val topicsFlow: KeyedMutableListFlow<String, Topic> = KeyedMutableListFlowImpl { it.id }

    @Stable
    val session: MutableState<SearchSession> by lazy {
        mutableStateOf(client.startSearchSession(SearchFilter()))
    }


    init {
        applicationScope.launch {
            hasMorePages.collect {
                println("hasMorePages changed: $it")
            }
        }
        applicationScope.launch {
            fetchingState.collect {
                println("isFetching changed: $it")
            }
        }
    }

    fun updateSearchFilter(searchFilter: SearchFilter) {
        this.searchFilter.value = searchFilter
        hasMorePages.value = true
        fetchingState.value = FetchingState.Idle
        topicsFlow.value = listOf()
        session.value = client.startSearchSession(searchFilter)
        launchFetchNextPage(!searchFilter.keywords.isNullOrEmpty())
    }

    private suspend fun fetchAndUpdatePage(
        session: SearchSession,
        topicsFlow: KeyedMutableListFlow<String, Topic>
    ): Boolean {
        val nextPage = session.nextPage()
        return if (!nextPage.isNullOrEmpty()) {
            nextPage.forEach { it.details } // init
            val old = topicsFlow.value
            val new = sequenceOf(topicsFlow.value, nextPage).flatten().distinctBy { it.id }.toList()
            if (old.size == new.size) {
                //                hasMorePages.value = false
            } else {
                topicsFlow.value = new
            }
            true
        } else {
            hasMorePages.value = false
            false
        }
    }

    fun launchFetchNextPage(continuous: Boolean) {
        while (true) {
            val value = fetchingState.value
            if (value != FetchingState.Fetching) {
                if (fetchingState.compareAndSet(value, FetchingState.Fetching)) {
                    break
                }
            }
        }
        applicationScope.launch {
            try {
                do {
                    val fetchedNewPage = fetchAndUpdatePage(session.value, topicsFlow)
                } while (fetchedNewPage && continuous)
                fetchingState.value = FetchingState.Succeed
            } catch (e: Throwable) {
                fetchingState.value = FetchingState.Failed(e)
            }
        }
    }
}

fun ApplicationState.doSearch(keywords: String) {
    updateSearchFilter(SearchFilter(keywords.trim(), null, null))
}
