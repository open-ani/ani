package me.him188.animationgarden.desktop.app

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlow
import me.him188.animationgarden.api.model.SearchSession
import me.him188.animationgarden.api.model.Topic

class Fetcher(
    private val scope: CoroutineScope,

    ) {
    @Stable
    val fetchingState: MutableStateFlow<FetchingState> = MutableStateFlow(FetchingState.Idle)

    @Stable
    val hasMorePages = MutableStateFlow(true)


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

    private var fetchJob: Job? = null

    fun launchFetchNextPage(
        continuous: Boolean,
        searchSession: SearchSession,
        topicsFlow: KeyedMutableListFlow<String, Topic>
    ) {
        // cancel old fetch and start new one
        while (true) {
            val value = fetchingState.value
            if (value is FetchingState.Fetching) {
                // is fetching
                if (value.query != searchSession.query) {
                    // different query, cancel old fetch and initialize a new one
                    fetchJob?.cancel()
                    break
                } else {
                    // same query, ignore this one
                    return
                }
            } else {
                // not fetching
                if (fetchingState.compareAndSet(value, FetchingState.Fetching(searchSession.query))) {
                    // set fetching
                    break
                } else continue
            }
        }

        fetchJob = scope.launch {
            try {
                do {
                    val fetchedNewPage = fetchAndUpdatePage(searchSession, topicsFlow)
                } while (fetchedNewPage && continuous)
                fetchingState.value = FetchingState.Succeed
            } catch (e: Throwable) {
                fetchingState.value = FetchingState.Failed(e)
            }
        }
    }
}