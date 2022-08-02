package me.him188.animationgarden.desktop.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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

@OptIn(ExperimentalFoundationApi::class)
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

    val keywordsFocus = remember { FocusRequester() }
    val allianceFocus = remember { FocusRequester() }

    val (keywords, onTitleChange) = remember { mutableStateOf("") }
    val (alliance, onAllianceChange) = remember { mutableStateOf("") }

    Column(Modifier.padding(PaddingValues(all = 16.dp))) {
        Row(Modifier
            .padding(16.dp)
            .focusProperties {
                canFocus = true
                next = allianceFocus
            }
            .focusGroup()
        ) {
            TextField(
                keywords,
                onTitleChange,
                Modifier.height(48.dp),
                keyboardActions = KeyboardActions(onDone = {
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
                shape = RoundedCornerShape(8.dp),
                maxLines = 1,
            )

            TextField(
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
                shape = RoundedCornerShape(8.dp),
                maxLines = 1,
            )

            Button(onClick = {
                app.updateSearchFilter(SearchFilter(keywords, null, null))
            }, Modifier.padding(start = 16.dp)) {
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
    LazyColumn(state = state, modifier = modifier.simpleVerticalScrollbar(state)) {
        items(topics, { it.id }) { topic ->
            TopicItemCard(topic) { onClickCard(topic) }
        }
    }
}

@Composable
fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
    width: Dp = 8.dp
): Modifier {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration)
    )

    return drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
        val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f

        // Draw scrollbar if scrolling or if the animation is still running and lazy column has content
        if (needDrawScrollbar && firstVisibleElementIndex != null) {
            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

            drawRect(
                color = Color.Red,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopicItemCard(item: Topic, onClick: () -> Unit) {
    Box(Modifier.fillMaxHeight()) {
        Card(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(
                    remember { MutableInteractionSource() },
                    rememberRipple(),
                ) { onClick() },
            shape = RoundedCornerShape(16.dp)
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