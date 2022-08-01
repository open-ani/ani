package me.him188.animationgarden.desktop.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlow
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlowImpl
import me.him188.animationgarden.api.impl.model.mutate
import me.him188.animationgarden.api.model.SearchFilter
import me.him188.animationgarden.api.model.SearchSession
import me.him188.animationgarden.api.model.Topic

@Stable
class ApplicationState(
    val client: AnimationGardenClient,
    var searchFilter: SearchFilter,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    app: ApplicationState
) {
    val topicsFlow: KeyedMutableListFlow<String, Topic> = remember {
        KeyedMutableListFlowImpl { it.id }
    }
    val topics by topicsFlow.asFlow().collectAsState()
    var fetching by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val session = remember(app.searchFilter) {
        app.client.startSearchSession(app.searchFilter).apply {
            coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                fetching = true
                fetchAndUpdatePage(topicsFlow)
                fetching = false
            }
        }
    }

    val state = rememberLazyListState()

    LaunchedEffect(
        fetching,
        state.isScrollInProgress,
        state.layoutInfo.visibleItemsInfo,
        state.layoutInfo.totalItemsCount
    ) {
        if (!fetching && !state.isScrollInProgress && reachedEnd(state)) {
            coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                fetching = true
                session.fetchAndUpdatePage(topicsFlow)
                fetching = false
            }
        }
    }

    LazyColumn(state = state, contentPadding = PaddingValues(all = 16.dp)) {
        items(topics.size, { topics[it].id }) { index ->
            val item = topics[index]
            Card {
                Text(item.title)
            }
        }
    }
}

private suspend fun SearchSession.fetchAndUpdatePage(topics: KeyedMutableListFlow<String, Topic>) {
    val nextPage = nextPage()
    if (!nextPage.isNullOrEmpty()) {
        topics.mutate { it + nextPage }
    }
}

private fun reachedEnd(state: LazyListState) =
    state.layoutInfo.visibleItemsInfo.lastOrNull()?.index == state.layoutInfo.totalItemsCount - 1

@Preview
@Composable
private fun PreviewMainPage() {
    val app = remember {
        ApplicationState(AnimationGardenClient.Factory.create(), SearchFilter())
    }
    MainPage(app)
}