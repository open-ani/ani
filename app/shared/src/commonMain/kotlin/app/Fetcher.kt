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

package me.him188.ani.app.app

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.him188.ani.datasources.api.SearchSession
import me.him188.ani.datasources.api.topic.Topic
import me.him188.ani.datasources.dmhy.impl.cache.KeyedMutableListFlow

class Fetcher(
    private val scope: CoroutineScope,
    private val onFetchSucceed: suspend () -> Unit,
) {
    @Stable
    val fetchingState: MutableStateFlow<FetchingState> = MutableStateFlow(FetchingState.Idle)

    init {
        scope.launch {
            fetchingState.collect { fetching ->
                if (fetching == FetchingState.Succeed) {
                    onFetchSucceed()
                }
            }
        }
    }

    @Stable
    val hasMorePages = MutableStateFlow(true)


    private suspend fun fetchAndUpdatePage(
        session: SearchSession<Topic>,
        topicsFlow: KeyedMutableListFlow<String, Topic>
    ): Boolean {
        val nextPage = session.nextPageOrNull()
        return if (!nextPage.isNullOrEmpty()) {
            nextPage.forEach { it.details } // init
//            val old = topicsFlow.value
            topicsFlow.value =
                sequenceOf(topicsFlow.value, nextPage).flatten().distinctBy { it.id }.toList()
//            val new = sequenceOf(topicsFlow.value, nextPage).flatten().distinctBy { it.id }.toList()
//            if (old.size == new.size) {
//                //                hasMorePages.value = false
//            } else {
//                topicsFlow.value = new
//            }
            true
        } else {
            hasMorePages.value = false
            false
        }
    }

    private var fetchJob: Job? = null

    fun launchFetchNextPage(
        continuous: Boolean,
        searchSession: SearchSession<Topic>,
        topicsFlow: MutableStateFlow<List<Topic>>
    ) {
        TODO()
//        // cancel old fetch and start new one
//        while (true) {
//            val value = fetchingState.value
//            if (value is FetchingState.Fetching) {
//                // is fetching
//                if (value.query != searchSession.query) {
//                    // different query, cancel old fetch and initialize a new one
//                    fetchJob?.cancel()
//                    break
//                } else {
//                    // same query, ignore this one
//                    return
//                }
//            } else {
//                // not fetching
//                if (fetchingState.compareAndSet(value, FetchingState.Fetching(searchSession.query))) {
//                    // set fetching
//                    break
//                } else continue
//            }
//        }
//
//        fetchJob = scope.launch {
//            try {
//                do {
//                    val fetchedNewPage = fetchAndUpdatePage(searchSession, topicsFlow)
//                } while (fetchedNewPage && continuous)
//                fetchingState.value = FetchingState.Succeed
//            } catch (e: Throwable) {
//                fetchingState.value = FetchingState.Failed(e)
//            }
//        }
    }
}