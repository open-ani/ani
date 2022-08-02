package me.him188.animationgarden.desktop.ui

import androidx.compose.animation.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlow
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlowImpl
import me.him188.animationgarden.api.impl.model.mutate
import me.him188.animationgarden.api.model.*
import me.him188.animationgarden.api.model.FileSize.Companion.megaBytes
import java.awt.Desktop
import java.net.URI
import java.time.LocalDateTime

@Stable
class ApplicationState(
    val client: AnimationGardenClient,
) {
    val applicationScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
            throwable.printStackTrace()
        })


    val isFetching = MutableStateFlow(false)
    val searchFilter: MutableState<SearchFilter> = mutableStateOf(SearchFilter())
    val topicsFlow: KeyedMutableListFlow<String, Topic> = KeyedMutableListFlowImpl { it.id }

    val session: MutableState<SearchSession> by lazy {
        mutableStateOf(client.startSearchSession(SearchFilter()))
    }


    fun updateSearchFilter(searchFilter: SearchFilter) {
        this.searchFilter.value = searchFilter
        topicsFlow.value = listOf()
        session.value = client.startSearchSession(searchFilter)
        fetchNextPage()
    }

    private suspend fun fetchAndUpdatePage(session: SearchSession, topicsFlow: KeyedMutableListFlow<String, Topic>) {
        val nextPage = session.nextPage()
        if (!nextPage.isNullOrEmpty()) {
            topicsFlow.mutate { it + nextPage }
        }
    }

    fun fetchNextPage() {
        if (!isFetching.compareAndSet(expect = false, update = true)) return
        applicationScope.launch {
            try {
                fetchAndUpdatePage(session.value, topicsFlow)
            } finally {
                isFetching.value = false
            }
        }
    }
}

@Composable
fun MainPage(
    app: ApplicationState
) {
    val topicsFlow: KeyedMutableListFlow<String, Topic> = app.topicsFlow
    val topics by topicsFlow.asFlow().collectAsState()
    val fetching by app.isFetching.collectAsState()
    var searchFilter by app.searchFilter

    val state = rememberLazyListState()

    if (!fetching && !state.isScrollInProgress && state.reachedEnd()) {
        app.fetchNextPage()
    }

    val (keywords, onTitleChange) = remember { mutableStateOf("") }
    val (alliance, onAllianceChange) = remember { mutableStateOf("") }

    Column(Modifier.background(color = MaterialTheme.colorScheme.background).padding(PaddingValues(all = 16.dp))) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row {
                OutlinedTextField(
                    keywords,
                    onTitleChange,
                    Modifier.height(48.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = {
                        searchFilter = SearchFilter(keywords)
                    }),
                    placeholder = {
                        Text(
                            "keywords",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.typography.bodySmall.color.copy(0.3f)
                            )
                        )
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    maxLines = 1,
                )

                OutlinedTextField(
                    alliance,
                    onAllianceChange,
                    Modifier.padding(start = 16.dp).height(48.dp),
                    keyboardActions = KeyboardActions(onDone = {
                        searchFilter = SearchFilter(alliance)
                    }),
                    placeholder = {
                        Text(
                            "alliance",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.typography.bodySmall.color.copy(0.3f)
                            )
                        )
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    maxLines = 1,
                )
            }

            Button(
                onClick = {
                    app.updateSearchFilter(SearchFilter(keywords, null, null))
                },
                Modifier.padding(start = 16.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text("Search")
            }
        }

        LiveList(
            state,
            topics,
            onClickCard = {
                app.applicationScope.launch(Dispatchers.IO) {
                    Desktop.getDesktop().browse(URI.create(it.magnetLink.value))
                }
            },
            Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun LiveList(
    state: LazyListState,
    topics: List<Topic>,
    onClickCard: (topic: Topic) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            topics.isEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        AnimatedVisibility(
            topics.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
        ) {
            LazyColumn(state = state, modifier = modifier) {
                items(topics, { it.id }) { topic ->
                    TopicItemCard(topic) { onClickCard(topic) }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopicItemCard(item: Topic, onClick: () -> Unit) {
    Box(Modifier.fillMaxHeight()) {
        OutlinedCard(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(MaterialTheme.shapes.large)
                .clickable(
                    remember { MutableInteractionSource() },
                    rememberRipple(color = MaterialTheme.colorScheme.surfaceTint),
                ) { onClick() }
//                .border(1.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.large)
                .shadow(elevation = 2.dp, shape = MaterialTheme.shapes.large)
                .wrapContentSize(),
            shape = MaterialTheme.shapes.large,
        ) {
            Box(Modifier.padding(16.dp)) {
                Row {
                    Column {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)

                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row {
                                item.alliance?.let { alliance ->
                                    Text(
                                        alliance.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.W600
                                    )
                                }
                                Text(
                                    item.author.name,
                                    Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.W400
                                )
                            }

                            Text(
                                item.date.format(DATE_FORMAT),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.typography.bodyMedium.color.copy(alpha = 0.5f),
                                modifier = Modifier.padding(start = 4.dp),
                                fontWeight = FontWeight.W400
                            )
                        }
                    }
                }
            }
        }

    }
}

private fun LazyListState.reachedEnd() =
    layoutInfo.totalItemsCount == 0 || layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1

@Preview
@Composable
private fun PreviewMainPage() {
    val app = remember {
        ApplicationState(AnimationGardenClient.Factory.create())
    }
    MaterialTheme {
        MainPage(app)
    }
}

@Preview
@Composable
private fun PreviewTopicList() {
    LiveList(
        rememberLazyListState(),
        mutableListOf<Topic>().apply {
            repeat(10) {
                add(
                    // NC-Raws [NC-Raws] OVERLORD IV / Overlord S4 - 04 (B-Global 3840x2160 HEVC AAC MKV)
                    Topic(
                        it.toString(),
                        LocalDateTime.from(DATE_FORMAT.parse("2022/07/26 22:01")),
                        TopicCategory("1", "動畫"),
                        Alliance("111", "NC-Raws"),
                        "[NC-Raws] OVERLORD IV / Overlord S4 - 04 (B-Global 3840x2160 HEVC AAC MKV)",
                        1,
                        MagnetLink("11111"),
                        457.6.megaBytes,
                        UserImpl("22", "九十九朔夜")
                    )
                )
            }
        },
        onClickCard = {

        }
    )
}