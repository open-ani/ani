package me.him188.animationgarden.desktop.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlow
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlowImpl
import me.him188.animationgarden.api.impl.model.mutate
import me.him188.animationgarden.api.model.*
import me.him188.animationgarden.api.model.FileSize.Companion.megaBytes
import me.him188.animationgarden.desktop.AppTheme
import java.awt.Desktop
import java.net.URI
import java.time.LocalDateTime

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
    val isFetching = MutableStateFlow(false)

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
            topicsFlow.asFlow().collect {
                println("Topics changed: $it")
            }
        }
        applicationScope.launch {
            isFetching.collect {
                println("isFetching changed: $it")
            }
        }
    }

    fun updateSearchFilter(searchFilter: SearchFilter) {
        this.searchFilter.value = searchFilter
        topicsFlow.value = listOf()
        session.value = client.startSearchSession(searchFilter)
        launchFetchNextPage()
    }

    private suspend fun fetchAndUpdatePage(session: SearchSession, topicsFlow: KeyedMutableListFlow<String, Topic>) {
        val nextPage = session.nextPage()
        if (!nextPage.isNullOrEmpty()) {
            nextPage.forEach { it.details } // init
            topicsFlow.mutate { it + nextPage }
        } else {
            hasMorePages.value = false
        }
    }

    fun launchFetchNextPage() {
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


inline fun doSearch(app: ApplicationState, keywords: String) {
    app.updateSearchFilter(SearchFilter(keywords, null, null))
}

@Composable
fun MainPage(
    app: ApplicationState
) {
    val appState by rememberUpdatedState(app)
    val topics by remember { app.topicsFlow.asFlow() }.collectAsState()

    val (keywords, onKeywrodsChange) = remember { mutableStateOf("") }
    val (alliance, onAllianceChange) = remember { mutableStateOf("") }


    val backgroundColor = AppTheme.colorScheme.background
    Column(Modifier.background(color = backgroundColor).padding(PaddingValues(all = 16.dp))) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row {
                OutlinedTextField(
                    keywords,
                    onKeywrodsChange,
                    Modifier.height(48.dp).defaultMinSize(minWidth = 96.dp).weight(0.8f)
                        .onKeyEvent {
                            if (it.key == Key.Enter || it.key == Key.NumPadEnter) {
                                doSearch(appState, keywords)
                                true
                            } else false
                        },
                    placeholder = {
                        Text(
                            "keywords",
                            style = AppTheme.typography.bodyMedium.copy(
                                color = AppTheme.typography.bodyMedium.color.copy(0.3f),
                                lineHeight = 16.sp
                            )
                        )
                    },
                    singleLine = true,
                    shape = AppTheme.shapes.medium,
                    maxLines = 1,
                )
//                OutlinedTextField(
//                    alliance,
//                    onAllianceChange,
//                    Modifier.padding(start = 16.dp).height(48.dp),
//                    placeholder = {
//                        Text(
//                            "alliance",
//                            style = AppTheme.typography.bodyMedium.copy(
//                                color = AppTheme.typography.bodyMedium.color.copy(0.3f)
//                            )
//                        )
//                    },
//                    singleLine = true,
//                    shape = AppTheme.shapes.medium,
//                    maxLines = 1,
//                )
                AnimatedSearchButton(appState, keywords)
            }
        }

        LiveList(
            app,
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
private fun AnimatedSearchButton(appState: ApplicationState, keywords: String) {
    BoxWithConstraints(
        Modifier.wrapContentWidth(),
        contentAlignment = Alignment.Center
    ) {
        val showBigButton = maxWidth > 512.dp
        val enter = scaleIn() + fadeIn()
        val exit = scaleOut() + fadeOut()
        val smallButtonWidth = 48.dp + 8.dp
        val bigButtonWidth = 144.dp
        val boxWidth by animateDpAsState(if (showBigButton) bigButtonWidth else smallButtonWidth)
        Box(Modifier.width(boxWidth)) {
            AnimatedVisibility(
                showBigButton,
                enter = enter,
                exit = exit,
                label = "Big Search Button"
            ) {
                Button(
                    onClick = {
                        doSearch(appState, keywords)
                    },
                    Modifier
                        .width(bigButtonWidth)
                        .padding(start = 16.dp)
                        .height(48.dp),
                    shape = AppTheme.shapes.medium,
                ) {
                    Image(
                        painterResource("drawable/magnify.svg"),
                        "Search",
                        Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(color = Color.White) // TODO: 2022/8/6 adjust color
                    )
                    Text("Search", Modifier.padding(start = 4.dp), style = AppTheme.typography.bodyMedium)
                }
            }
            AnimatedVisibility(
                !showBigButton,
                enter = enter,
                exit = exit,
                label = "Small Search Button"
            ) {
                Box(
                    Modifier.width(smallButtonWidth).padding(start = 8.dp).size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .border(1.dp, color = Color.Gray, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable(
                                remember { MutableInteractionSource() },
                                rememberRipple(),
                                onClick = {
                                    doSearch(appState, keywords)
                                }),

                        contentAlignment = Alignment.Center
                    ) {
                        Image(painterResource("drawable/magnify.svg"), "Search", Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveList(
    app: ApplicationState,
    topics: List<Topic>,
    onClickCard: (topic: Topic) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val isEmpty = topics.isEmpty()
        AnimatedVisibility(
            isEmpty,
            enter = fadeIn() + expandVertically(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            CircularProgressIndicator(color = AppTheme.colorScheme.primary)
        }
        AnimatedVisibility(
            !isEmpty,
            enter = fadeIn() + expandVertically(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
            // Actually when exiting, `topics` would be empty, so lazy column contains no item and size is zero. You won't see the animation.
        ) {
            val state = rememberLazyListState()
            LazyColumn(state = state, modifier = modifier) {
                items(topics, { it.id }, { it.details?.tags?.isNotEmpty() }) { topic ->
                    TopicItemCard(topic) { onClickCard(topic) }
                }
                // dummy footer. When footer gets into visible area, `LaunchedEffect` comes with its composition.
                item("refresh footer", contentType = "refresh footer") {
                    val hasMorePages by app.hasMorePages.collectAsState()
                    val fetching by app.isFetching.collectAsState()
                    if (hasMorePages && !fetching) { // when this footer is 'seen', the list must have reached the end.
                        LaunchedEffect(true) {
                            app.launchFetchNextPage()
                        }
                    }
                    if (fetching) {
                        Box(Modifier.fillMaxWidth().wrapContentHeight(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AppTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
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
        remember { ApplicationState(client = AnimationGardenClient.Factory.create()) },
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