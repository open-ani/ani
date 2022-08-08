package me.him188.animationgarden.desktop.ui

import androidx.compose.animation.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import me.him188.animationgarden.api.model.*
import me.him188.animationgarden.api.model.FileSize.Companion.megaBytes
import me.him188.animationgarden.desktop.AppTheme
import me.him188.animationgarden.desktop.i18n.LocalI18n
import java.awt.Desktop
import java.net.URI
import java.time.LocalDateTime

sealed class FetchingState {
    object Idle : FetchingState()
    object Fetching : FetchingState()
    sealed class Completed : FetchingState()
    object Succeed : Completed()
    class Failed(
        val exception: Throwable
    ) : Completed()
}

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
            topicsFlow.asFlow().collect {
                println("Topics changed: $it")
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
        topicsFlow.value = listOf()
        session.value = client.startSearchSession(searchFilter)
        launchFetchNextPage()
    }

    private suspend fun fetchAndUpdatePage(session: SearchSession, topicsFlow: KeyedMutableListFlow<String, Topic>) {
        val nextPage = session.nextPage()
        if (!nextPage.isNullOrEmpty()) {
            nextPage.forEach { it.details } // init
            val old = topicsFlow.value
            val new = sequenceOf(topicsFlow.value, nextPage).flatten().distinctBy { it.id }.toList()
            if (old.size == new.size) {
                hasMorePages.value = false
            } else {
                topicsFlow.value = new
            }
        } else {
            hasMorePages.value = false
        }
    }

    fun launchFetchNextPage() {
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
                fetchAndUpdatePage(session.value, topicsFlow)
                fetchingState.value = FetchingState.Succeed
            } catch (e: Throwable) {
                fetchingState.value = FetchingState.Failed(e)
            }
        }
    }
}

fun ApplicationState.doSearch(keywords: String) {
    updateSearchFilter(SearchFilter(keywords, null, null))
}

@Composable
fun MainPage(
    app: ApplicationState
) {
    val appState by rememberUpdatedState(app)
    val topics by remember { app.topicsFlow.asFlow() }.collectAsState()
    val lazyListState = rememberLazyListState()

    val appliedKeywordState = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var appliedKeyword by appliedKeywordState
    val workState by remember {
        val myWork = WorkState()
        derivedStateOf {
            myWork.apply {
                coroutineScope.launch {
                    lazyListState.scrollToItem(0, 0)
                }
                setTopics(topics, appState.searchFilter.value.keywords)
            }
        }
    }

    val backgroundColor = AppTheme.colorScheme.background
    Column(Modifier.background(color = backgroundColor).padding(PaddingValues(all = 16.dp))) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Row {
                val (keywordsInput, onKeywordsInputChange) = remember { mutableStateOf(appliedKeyword) }
                OutlinedTextField(
                    keywordsInput,
                    onKeywordsInputChange,
                    Modifier.height(48.dp).defaultMinSize(minWidth = 96.dp).weight(0.8f).onKeyEvent {
                        if (it.key == Key.Enter || it.key == Key.NumPadEnter) {
                            appliedKeyword = keywordsInput
                            appState.doSearch(appliedKeyword)
                            true
                        } else false
                    },
                    placeholder = {
                        Text(
                            LocalI18n.current.getString("search.keywords"),
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
                AnimatedSearchButton {
                    appliedKeyword = keywordsInput
                    appState.doSearch(appliedKeyword)
                }
            }
        }

        LiveList(
            app = app,
            workState = workState,
            lazyListState = lazyListState,
            topics = topics,
            onClickCard = {
                app.applicationScope.launch(Dispatchers.IO) {
                    Desktop.getDesktop().browse(URI.create(it.magnetLink.value))
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun AnimatedSearchButton(onClick: () -> Unit) {

    BoxWithConstraints(
        Modifier.wrapContentWidth(), contentAlignment = Alignment.Center
    ) {
        val showBigButton = maxWidth > 512.dp
//        val smallButtonWidth = 48.dp + 8.dp
//        val bigButtonWidth = 144.dp
//        val boxWidth by animateDpAsState(if (showBigButton) bigButtonWidth else smallButtonWidth)
        Box {
            Button(
                onClick = onClick,
                Modifier.padding(start = 16.dp).height(48.dp),
                shape = AppTheme.shapes.medium,
                contentPadding = if (showBigButton) ButtonDefaults.ContentPadding else PaddingValues(0.dp),
            ) {
                Image(
                    painterResource("drawable/magnify.svg"),
                    LocalI18n.current.getString("search.button"),
                    Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(color = AppTheme.colorScheme.primary.contrastTextColor())
                )
                AnimatedVisibility(showBigButton) {
                    Text(
                        LocalI18n.current.getString("search.button"),
                        Modifier.padding(start = 4.dp),
                        style = AppTheme.typography.bodyMedium
                    )
                }
            }

            // big-small switching button implementation
//            val enter = scaleIn() + fadeIn()
//            val exit = scaleOut() + fadeOut()
//            AnimatedVisibility(
//                showBigButton, enter = enter, exit = exit, label = "Big Search Button"
//            ) {
//                Button(
//                    onClick = onClick,
//                    Modifier.width(bigButtonWidth).padding(start = 16.dp).height(48.dp),
//                    shape = AppTheme.shapes.medium,
//                ) {
//                    Image(
//                        painterResource("drawable/magnify.svg"),
//                        LocalI18n.current.getString("search.button"),
//                        Modifier.size(24.dp),
//                        colorFilter = ColorFilter.tint(color = Color.White) // TODO: 2022/8/6 adjust color
//                    )
//                    Text(
//                        LocalI18n.current.getString("search.button"),
//                        Modifier.padding(start = 4.dp),
//                        style = AppTheme.typography.bodyMedium
//                    )
//                }
//            }
//            AnimatedVisibility(
//                !showBigButton, enter = enter, exit = exit, label = "Small Search Button"
//            ) {
//                Box(
//                    Modifier.width(smallButtonWidth).padding(start = 8.dp).size(48.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    val shape = AppTheme.shapes.small
//                    Box(
//                        Modifier.size(48.dp).border(1.dp, color = Color.Gray, shape = shape).clip(shape)
//                            .background(color = AppTheme.colorScheme.primary).clickable(
//                                remember { MutableInteractionSource() }, rememberRipple(), onClick = onClick
//                            ),
//
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Image(
//                            painterResource("drawable/magnify.svg"),
//                            LocalI18n.current.getString("search.button"),
//                            Modifier.size(24.dp),
//                            colorFilter = ColorFilter.tint(
//                                Color.White
//                            )
//                        )
//                    }
//                }
//            }
        }
    }
}

@Composable
private fun LiveList(
    app: ApplicationState,
    workState: WorkState,
    lazyListState: LazyListState = rememberLazyListState(),
    topics: List<Topic>,
    onClickCard: (topic: Topic) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnClickCard by rememberUpdatedState(onClickCard)
    val currentWorkState by rememberUpdatedState(workState)
    val enter = remember { expandVertically(expandFrom = Alignment.Top) + fadeIn() }
    val exit = remember { shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut() }
    val visibleTopics: List<Topic> by remember(topics) {
        derivedStateOf {
            topics.filter { currentWorkState.matchesQuery(it) }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        val isEmpty = topics.isEmpty()
        AnimatedVisibility(
            isEmpty,
            enter = enter,
            exit = exit,
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppTheme.colorScheme.primary)
            }
        }
        AnimatedVisibility(
            !isEmpty,
            enter = enter,
            exit = exit,
            // Actually when exiting, `topics` would be empty, so lazy column contains no item and size is zero. You won't see the animation.
        ) {

            LazyColumn(state = lazyListState, modifier = modifier) {
                item("organized", "organized") {
                    AnimatedVisibility(
                        topics.isNotEmpty() && app.searchFilter.value.keywords != null,
                        enter = enter,
                        exit = exit,
                    ) {
                        OrganizedWorkView(
                            workState = workState,
                            visibleTopics = visibleTopics,
                            onClickAlliance = { currentWorkState.selectedAlliance.updateSelected(it) },
                            onClickEpisode = { currentWorkState.selectedEpisode.updateSelected(it) },
                            onClickResolution = { currentWorkState.selectedResolution.updateSelected(it) },
                            onClickSubtitleLanguage = { currentWorkState.selectedSubtitleLanguage.updateSelected(it) })
                    }
                }

                items(topics, { it.id }, { it.details?.tags?.isNotEmpty() }) { topic ->
                    AnimatedVisibility(
                        visibleTopics.contains(topic),
                        enter = enter,
                        exit = exit,
                    ) {
                        TopicItemCard(topic) { currentOnClickCard(topic) }
                    }

                }
                // dummy footer. When footer gets into visible area, `LaunchedEffect` comes with its composition.
                item("refresh footer", contentType = "refresh footer") {
                    val hasMorePages by app.hasMorePages.collectAsState()
                    val fetching by app.fetchingState.collectAsState()
                    Box(
                        Modifier.padding(vertical = 16.dp).fillMaxWidth().wrapContentHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasMorePages) {
                            if (fetching != FetchingState.Fetching) {
                                LaunchedEffect(true) { // when this footer is 'seen', the list must have reached the end.
                                    app.launchFetchNextPage()
                                }
                            }
                        }

                        when (val localFetching = fetching) {
                            FetchingState.Idle, FetchingState.Fetching -> {
                                CircularProgressIndicator(color = AppTheme.colorScheme.primary)
                            }

                            FetchingState.Succeed -> {
                                Text(
                                    LocalI18n.current.getString("search.end"),
                                    style = AppTheme.typography.bodyMedium
                                )
                            }

                            is FetchingState.Failed -> {
                                Text(
                                    String.format(
                                        LocalI18n.current.getString("search.failed"),
                                        localFetching.exception.localizedMessage
                                    ),
                                    style = AppTheme.typography.bodyMedium.run { copy(color = color.copy(alpha = 0.5f)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

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
    LiveList(remember { ApplicationState(client = AnimationGardenClient.Factory.create()) },
        remember { WorkState() },
        topics = mutableListOf<Topic>().apply {
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

        })
}