package me.him188.animationgarden.desktop.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.impl.model.MutableListFlow
import me.him188.animationgarden.api.impl.model.mutate
import me.him188.animationgarden.api.model.*
import me.him188.animationgarden.api.model.FileSize.Companion.megaBytes
import me.him188.animationgarden.desktop.AppTheme
import me.him188.animationgarden.desktop.ProvideCompositionLocalsForPreview
import me.him188.animationgarden.desktop.app.ApplicationState
import me.him188.animationgarden.desktop.app.FetchingState
import me.him188.animationgarden.desktop.app.StarredAnime
import me.him188.animationgarden.desktop.app.doSearch
import me.him188.animationgarden.desktop.i18n.LocalI18n
import me.him188.animationgarden.desktop.ui.interaction.onEnterKeyEvent
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

@Composable
fun MainPage(
    app: ApplicationState
) {
    // properties starting with "current" means it is delegated by a State, so they are observable in 'derived state',
    // and is safe to be used in callbacks.

    val currentApp by rememberUpdatedState(app)
    val currentTopics by remember { derivedStateOf { currentApp.topicsFlow.asFlow() } }.value.collectAsState()
    val currentLazyList = rememberLazyListState()

    val appliedKeywordState = remember { mutableStateOf("") }
    val currentCoroutineScope = rememberCoroutineScope()
    var currentAppliedKeyword by appliedKeywordState
    val (keywordsInput, onKeywordsInputChange) = remember { mutableStateOf(currentAppliedKeyword) }

    val currentStarredAnimeList by app.data.starredAnime.asFlow().collectAsState()
    val currentSearchQuery by rememberUpdatedState(app.searchQuery)
    val currentStarredAnime by remember {
        derivedStateOf {
            currentStarredAnimeList.find { it.searchQuery == currentSearchQuery.value.keywords }
        }
    }

    val currentOrganizedViewState by remember {
        val instance = OrganizedViewState()
        derivedStateOf { // observe topics
            instance.apply {
                selectedAlliance.value = currentStarredAnime?.preferredAlliance
                selectedResolution.value = currentStarredAnime?.preferredResolution
                selectedSubtitleLanguage.value = currentStarredAnime?.preferredSubtitleLanguage

                currentCoroutineScope.launch {
                    currentLazyList.scrollToItem(0, 0)
                }
                setTopics(currentTopics, currentApp.searchQuery.value.keywords)
            }
        }
    }
    SideEffect {
        currentApp.currentOrganizedViewState = currentOrganizedViewState
    }

    val backgroundColor = AppTheme.colorScheme.background
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val paddingByWindowSize by animateDpAsState(
            if (maxWidth > 400.dp) {
                16.dp
            } else {
                8.dp
            },
        )

        Column(Modifier.background(color = backgroundColor).padding(PaddingValues(all = paddingByWindowSize))) {
            var starListMode by remember { mutableStateOf(false) }

            // Search bar, fixed height
            Row(
                Modifier.padding(top = 16.dp, bottom = 16.dp, start = paddingByWindowSize, end = paddingByWindowSize)
                    .fillMaxWidth()
            ) {
                BoxWithConstraints {
                    // animated width of the Starred List button
                    val width by animateDpAsState(
                        if (starListMode) maxWidth else 48.dp,
                        spring(stiffness = Spring.StiffnessMediumLow)
                    )
                    // Starred List button
                    OutlinedIconToggleButton(
                        checked = starListMode,
                        onCheckedChange = { starListMode = it },
                        modifier = Modifier.height(48.dp).width(width),
                        shape = AppTheme.shapes.medium,
                        colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                            checkedContainerColor = AppTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        ),
                    ) {
                        Row(
                            Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painterResource("drawable/star-outline.svg"),
                                LocalI18n.current.getString("starred.list"),
                                modifier = Modifier.size(24.dp)
                            )
                            // This Text expands when switched to Starred List viewing.
                            AnimatedVisibility(starListMode) {
                                Text(
                                    LocalI18n.current.getString("starred"),
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = AppTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }

                // keywords(search query) input
                OutlinedTextField(
                    keywordsInput,
                    onKeywordsInputChange,
                    Modifier
                        .padding(start = 8.dp)
                        .height(48.dp)
                        .defaultMinSize(minWidth = if (starListMode) 0.dp else 96.dp)
                        .weight(0.8f)
                        .onEnterKeyEvent {
                            currentAppliedKeyword = keywordsInput.trim()
                            currentApp.doSearch(currentAppliedKeyword)
                            true
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

                // Resizable button for initializing search
                AnimatedSearchButton {
                    currentAppliedKeyword = keywordsInput.trim()
                    currentApp.doSearch(currentAppliedKeyword)
                }
            }

            // Topics search result or Starred List
            Row(
                Modifier
                    .padding(horizontal = paddingByWindowSize)
                    .fillMaxSize()
            ) {

                // Starred List, expand from/shrink towards start.
                AnimatedVisibility(
                    starListMode,
                    enter = expandHorizontally(
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntSize.VisibilityThreshold
                        ),
                        expandFrom = Alignment.Start,
                    ),
                    exit = shrinkHorizontally(
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntSize.VisibilityThreshold
                        ),
                        shrinkTowards = Alignment.Start
                    )
                ) {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(currentStarredAnimeList, key = { it.id }) { anime ->
                            val currentAnime by rememberUpdatedState(anime)
                            LaunchedEffect(anime.id) {
                                delay(1.seconds) // ignore if user is quickly scrolling
                                updateStarredAnimeEpisodes(currentApp, anime, currentAnime, app)
                            }
                            StarredAnimeCard(
                                anime = anime,
                                onStarRemove = {
                                    currentApp.removeStarredAnime(currentAnime)
                                },
                                onClick = {
                                    app.updateSearchQuery(SearchQuery(keywords = currentAnime.searchQuery))
                                    starListMode = false
                                    currentAppliedKeyword = currentAnime.searchQuery
                                    currentOrganizedViewState.selectedEpisode.value = it
                                    onKeywordsInputChange(currentAnime.searchQuery)
                                }
                            )
                        }
                    }
                }

                // Topic Search Result, slide in from/out towards end.
                AnimatedVisibility(
                    !starListMode,
                    enter = fadeIn() + slideInHorizontally(
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntOffset.VisibilityThreshold
                        ),
                        initialOffsetX = { it },
                    ),
                    exit = fadeOut() + slideOutHorizontally(
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntOffset.VisibilityThreshold
                        ),
                        targetOffsetX = { it }
                    )
                ) {
                    // topic searching mode

                    // no vertical padding
                    LiveTopicList(
                        modifier = Modifier,
                        app = app,
                        organizedViewState = currentOrganizedViewState,
                        lazyListState = currentLazyList,
                        topics = currentTopics,
                        onClickCard = {
                            currentApp.applicationScope.launch(Dispatchers.IO) {
                                it.details?.episode?.let { currentApp.onEpisodeDownloaded(it) }
                                Desktop.getDesktop().browse(URI.create(it.magnetLink.value))
                            }
                        },
                        starred = currentStarredAnime != null,
                        onUpdateFilter = {
                            currentApp.data.starredAnime.updateStarredAnime(
                                currentApp.searchQuery.value.keywords.orEmpty(),
                                currentOrganizedViewState
                            )
                        },
                        onStarredChange = { starred ->
                            if (starred) {
                                currentApp.data.starredAnime.mutate {
                                    it + StarredAnime(
                                        primaryName = currentOrganizedViewState.chineseName.value
                                            ?: currentOrganizedViewState.otherNames.value.firstOrNull() ?: "",
                                        secondaryNames = currentOrganizedViewState.otherNames.value,
                                        searchQuery = currentApp.searchQuery.value.keywords.orEmpty(),
                                        preferredAlliance = currentOrganizedViewState.selectedAlliance.value,
                                        preferredResolution = currentOrganizedViewState.selectedResolution.value,
                                        preferredSubtitleLanguage = currentOrganizedViewState.selectedSubtitleLanguage.value,
                                        episodes = currentOrganizedViewState.episodes.value,
                                        starTimeMillis = System.currentTimeMillis()
                                    )
                                }
                            } else {
                                currentApp.data.starredAnime.mutate { list -> list.filterNot { it.searchQuery == currentApp.searchQuery.value.keywords } }
                            }
                        },
                    )
                }
            }
        }

    }
}

/**
 * Fetch all topics and update known available episodes, until we are sure that it's already up-to-date.
 */
private suspend fun CoroutineScope.updateStarredAnimeEpisodes(
    currentApp: ApplicationState,
    anime: StarredAnime,
    currentAnime: StarredAnime,
    app: ApplicationState
) {
    val session =
        currentApp.client.value.startSearchSession(SearchQuery(keywords = anime.searchQuery))
    val allTopics = mutableListOf<Topic>()
    while (isActive) {
        // always fetch the newest page, which contain the newer episodes.
        val nextPage = session.nextPage() ?: break
        allTopics.addAll(nextPage)
        // if all exising episodes are contained, means we have reached the last updated point.
        if (allEpisodesContained(currentAnime, allTopics)) {
            break
        }
    }
    if (allEpisodesContained(currentAnime, allTopics)) {
        app.data.starredAnime.updateStarredAnime(anime.searchQuery) {
            copy(
                episodes = (episodes.asSequence() + allTopics.asSequence()
                    .mapNotNull { it.details?.episode }).distinct().toList()
            )
        }
    }
}

private fun allEpisodesContained(
    currentAnime: StarredAnime,
    allTopics: MutableList<Topic>
) = currentAnime.episodes.all { episode -> allTopics.any { it.details?.episode == episode } }

private fun ApplicationState.removeStarredAnime(
    anime: StarredAnime
) {
    data.starredAnime.mutate { list -> list.filterNot { it.id == anime.id } }
}

fun MutableListFlow<StarredAnime>.updateStarredAnime(
    searchQuery: String,
    currentOrganizedViewState: OrganizedViewState,
) {
    return updateStarredAnime(searchQuery) {
        copy(
            primaryName = currentOrganizedViewState.chineseName.value
                ?: currentOrganizedViewState.otherNames.value.firstOrNull() ?: "",
            secondaryNames = currentOrganizedViewState.otherNames.value,
            episodes = currentOrganizedViewState.episodes.value,
            preferredAlliance = currentOrganizedViewState.selectedAlliance.value,
            preferredResolution = currentOrganizedViewState.selectedResolution.value,
            preferredSubtitleLanguage = currentOrganizedViewState.selectedSubtitleLanguage.value,
        )
    }
}

fun MutableListFlow<StarredAnime>.updateStarredAnime(
    searchQuery: String,
    update: StarredAnime.() -> StarredAnime,
) {
    return mutate { list ->
        list.map { anime ->
            if (anime.searchQuery == searchQuery) update(anime) else anime
        }
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
                Modifier.padding(start = 8.dp).height(48.dp),
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
//                        colorFilter = ColorFilter.tint(color = Color.White)
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
private fun LiveTopicList(
    app: ApplicationState,
    organizedViewState: OrganizedViewState,
    lazyListState: LazyListState = rememberLazyListState(),
    topics: List<Topic>,
    onClickCard: (topic: Topic) -> Unit,
    starred: Boolean,
    onUpdateFilter: () -> Unit,
    onStarredChange: (Boolean) -> Unit,
    spacedBy: Dp = 12.dp,
    modifier: Modifier = Modifier,
) {
    val currentOnClickCard by rememberUpdatedState(onClickCard)
    val currentOnUpdateFilter by rememberUpdatedState(onUpdateFilter)
    val currentOrganizedViewState by rememberUpdatedState(organizedViewState)
    val enter = remember { expandVertically(expandFrom = Alignment.Top) + fadeIn() }
    val exit = remember { shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut() }
    val currentTopics by rememberUpdatedState(topics)
    val visibleTopics: List<Topic> by remember(currentTopics) {
        derivedStateOf {
            currentTopics.filter { currentOrganizedViewState.matchesQuery(it) }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        val fetchingState by app.fetcher.fetchingState.collectAsState()
        val showProgress = fetchingState != FetchingState.Idle && topics.isEmpty()

        // animate on changing search query
        AnimatedVisibility(
            showProgress,
            enter = enter,
            exit = exit,
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppTheme.colorScheme.primary)
            }
        }
        AnimatedVisibility(
            !showProgress,
            enter = enter,
            exit = exit,
            // Actually when exiting, `topics` would be empty, so lazy column contains no item and size is zero. You won't see the animation.
        ) {

            LazyColumn(state = lazyListState, modifier = modifier) {
                item("organized", "organized") {
                    val currentSearchQuery by app.searchQuery
                    if (topics.isNotEmpty() && !currentSearchQuery.keywords.isNullOrBlank()) {
                        Box(Modifier.padding(bottom = spacedBy + 8.dp)) { // extra 8.dp padding
                            OrganizedViewCard(
                                organizedViewState = organizedViewState,
                                visibleTopics = visibleTopics,
                                isEpisodeWatched = { app.isEpisodeWatched(it) },
                                onClickEpisode = {
                                    currentOrganizedViewState.selectedEpisode.invertSelected(it)
                                    currentOnUpdateFilter()
                                },
                                onClickSubtitleLanguage = {
                                    currentOrganizedViewState.selectedSubtitleLanguage.invertSelected(it)
                                    currentOnUpdateFilter()
                                },
                                onClickResolution = {
                                    currentOrganizedViewState.selectedResolution.invertSelected(it)
                                    currentOnUpdateFilter()
                                },
                                onClickAlliance = {
                                    currentOrganizedViewState.selectedAlliance.invertSelected(it)
                                    currentOnUpdateFilter()
                                },
                                starred = starred,
                                onStarredChange = onStarredChange
                            )
                        }
                    }
                }

                items(topics, { it.id }, { null }) { topic ->
                    // animate on selecting filter
                    AnimatedVisibility(
                        visibleTopics.contains(topic),
                        enter = enter,
                        exit = exit,
                    ) {
                        Box(Modifier.padding(bottom = spacedBy)) {
                            TopicItemCard(topic) { currentOnClickCard(topic) }
                        }
                    }
                }

                // dummy footer. When footer gets into visible area, `LaunchedEffect` comes with its composition.
                item("refresh footer", contentType = "refresh footer") {
                    val hasMorePages by app.fetcher.hasMorePages.collectAsState()

                    Box(
                        Modifier.padding(top = spacedBy + 8.dp, bottom = spacedBy) // extra 8.dp padding
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasMorePages) {
                            if (fetchingState !is FetchingState.Fetching) {
                                LaunchedEffect(true) { // when this footer is 'seen', the list must have reached the end.
                                    app.launchFetchNextPage(false)
                                }
                            }
                        }

                        when (val localFetching = fetchingState) {
                            FetchingState.Idle, is FetchingState.Fetching -> {
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
                                // TODO: 2022/8/17 add retry
//                                ClickableText(
//                                    AnnotatedString(LocalI18n.current.getString("search.retry")),
//                                    style = AppTheme.typography.bodyMedium.run { copy(color = color.copy(alpha = 0.5f)) },
//                                    onClick = {
//                                        app.launchFetchNextPage(false)
//                                    }
//                                )
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
    ProvideCompositionLocalsForPreview {
        val app = remember {
            ApplicationState(AnimationGardenClient.Factory.create {}, File("."))
        }
        MaterialTheme {
            MainPage(app)
        }
    }
}

@Preview
@Composable
private fun PreviewTopicList() {
    val (starred, onStarredChange) = remember { mutableStateOf(false) }

    LiveTopicList(
        remember { ApplicationState(initialClient = AnimationGardenClient.Factory.create {}, File(".")) },
        remember { OrganizedViewState() },
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

        },
        starred = starred,
        onUpdateFilter = {},
        onStarredChange = onStarredChange
    )
}