/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.model.*
import me.him188.animationgarden.api.model.FileSize.Companion.megaBytes
import me.him188.animationgarden.app.AppTheme
import me.him188.animationgarden.app.app.*
import me.him188.animationgarden.app.app.data.*
import me.him188.animationgarden.app.app.settings.LocalSyncSettings
import me.him188.animationgarden.app.i18n.LocalI18n
import me.him188.animationgarden.app.platform.LocalContext
import me.him188.animationgarden.app.platform.Res
import me.him188.animationgarden.app.platform.browse
import me.him188.animationgarden.app.ui.interaction.onEnterKeyEvent
import me.him188.animationgarden.app.ui.widgets.OutlinedTextFieldEx
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

@Composable
fun MainPage(
    app: ApplicationState, innerPadding: Dp = 16.dp,
) {
    // properties starting with "current" means it is delegated by a State, so they are observable in 'derived state',
    // and is safe to be used in callbacks.

    val currentApp by rememberUpdatedState(app)
    val currentTopics by app.topicsFlowState.value.collectAsState()

    val appliedKeywordState = rememberSaveable { mutableStateOf("") }
    var currentAppliedKeyword by appliedKeywordState
    val (keywordsInput, onKeywordsInputChange) = rememberSaveable { mutableStateOf(currentAppliedKeyword) }
    val currentOnKeywordsInputChange by rememberUpdatedState(onKeywordsInputChange)

    val currentStarredAnime by app.rememberCurrentStarredAnimeState()

    Column(Modifier.background(color = AppTheme.colorScheme.background)) {
        var starListMode by rememberSaveable { mutableStateOf(false) }

        // Search bar, fixed height
        Row(
            Modifier.padding(top = 16.dp, bottom = 16.dp, start = innerPadding, end = innerPadding)
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
                            Res.painter.star_outline,
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

            val keyboard by rememberUpdatedState(LocalSoftwareKeyboardController.current)

            fun doSearch() {
                keyboard?.hide()
                currentAppliedKeyword = keywordsInput.trim()
                currentApp.doSearch(currentAppliedKeyword)
            }

            // keywords(search query) input
            SearchTextField(
                keywordsInput,
                onKeywordsInputChange,
                Modifier
                    .padding(start = 8.dp)
                    .defaultMinSize(minWidth = if (starListMode) 0.dp else 96.dp)
                    .weight(0.8f),
                doSearch = { doSearch() }
            )

            // Resizable button for initializing search
            AnimatedSearchButton {
                doSearch()
            }
        }

        // Topics search result or Starred List
        Row(
            Modifier
                .padding(horizontal = innerPadding)
                .fillMaxSize()
        ) {

            // Starred List, expand from/shrink towards start.
            val springIntSize = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntSize.VisibilityThreshold
            )
            val list by app.starredAnimeListState
            AnimatedVisibility(
                starListMode,
                enter = expandHorizontally(
                    springIntSize,
                    expandFrom = Alignment.Start,
                ),
                exit = shrinkHorizontally(
                    springIntSize,
                    shrinkTowards = Alignment.Start
                )
            ) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list, key = { it.id }) { anime ->
                        val currentAnime by rememberUpdatedState(anime)
                        LaunchedEffect(anime.id) {
                            delay(1.seconds) // ignore if user is quickly scrolling
                            currentApp.updateStarredAnimeEpisodes(anime, currentAnime)
                        }
                        StarredAnimeCard(
                            anime = anime,
                            onStarRemove = {
                                currentApp.launchDataSynchronization {
                                    commit(StarredAnimeMutations.Remove(currentAnime.id))
                                }
                            },
                            onClick = {
                                currentApp.updateSearchQuery(SearchQuery(keywords = currentAnime.searchQuery))
                                starListMode = false
                                currentAppliedKeyword = currentAnime.searchQuery
                                currentApp.organizedViewState.selectedEpisode.value = it
                                currentOnKeywordsInputChange(currentAnime.searchQuery)
                            }
                        )
                    }
                }
            }

            // Topic Search Result, slide in from/out towards end.
            val springIntOffset = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold
            )
            AnimatedVisibility(
                !starListMode,
                enter = fadeIn() + slideInHorizontally(
                    springIntOffset,
                    initialOffsetX = { it },
                ),
                exit = fadeOut() + slideOutHorizontally(
                    springIntOffset,
                    targetOffsetX = { it }
                )
            ) {
                // topic searching mode

                TopicsSearchResult(
                    app,
                    currentTopics,
                    isStarred = currentStarredAnime != null
                )
            }
        }
    }
}

@Composable
fun SearchTextField(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier,
    doSearch: () -> Unit,
) {
    val currentDoSearch by rememberUpdatedState(doSearch)
    ProvideTextStyle(AppTheme.typography.bodyMedium.copy(lineHeight = 16.sp)) {
        OutlinedTextFieldEx(
            text,
            onTextChange,
            modifier
                .height(48.dp)
                .onEnterKeyEvent {
                    currentDoSearch()
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { currentDoSearch() }),
            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp)
        )
    }
}

@Composable
fun TopicsSearchResult(
    app: ApplicationState,
    topics: List<Topic>,
    isStarred: Boolean,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    // no vertical padding
    val currentContext by rememberUpdatedState(LocalContext.current)
    val currentApp by rememberUpdatedState(app)
    val organizedViewState = app.organizedViewState
    LiveTopicList(
        modifier = Modifier,
        app = app,
        organizedViewState = organizedViewState,
        lazyListState = lazyListState,
        topics = topics,
        onClickCard = { topic ->
            currentApp.launchDataSynchronization {
                topic.details?.episode?.let { currentApp.markEpisodeWatched(it) }
                withContext(Dispatchers.Main) {
                    browse(currentContext, topic.magnetLink.value)
                }
            }
        },
        starred = isStarred,
        onUpdateFilter = {
            currentApp.launchDataSynchronization {
                val id = searchQuery.value.keywords ?: return@launchDataSynchronization
                commit(StarredAnimeMutations.UpdateRefreshed(id, id, organizedViewState))
            }
        },
        onStarredChange = { starred ->
            currentApp.launchDataSynchronization {
                val searchQuery = searchQuery.value.keywords ?: return@launchDataSynchronization
                if (starred) {
                    commit(StarredAnimeMutations.Add(searchQuery, organizedViewState))
                } else {
                    commit(StarredAnimeMutations.Remove(searchQuery))
                }
            }
        },
    )
}

/**
 * Fetch all topics and update known available episodes, until we are sure that it's already up-to-date.
 */
context(CoroutineScope) suspend fun ApplicationState.updateStarredAnimeEpisodes(
    anime: StarredAnime,
    currentAnime: StarredAnime,
) {
    val session =
        client.value.startSearchSession(SearchQuery(keywords = anime.searchQuery))
    val allTopics = mutableListOf<Topic>()

    dataSynchronizer.commit(StarredAnimeMutations.ChangeRefreshState(anime.id, RefreshState.Refreshing))
    try {
        while (isActive) {
            // always fetch the newest page, which contain the newer episodes.
            val nextPage = session.nextPage() ?: break
            allTopics.addAll(nextPage)
            // if all exising episodes are contained, means we have reached the last updated point.
            if (allEpisodesContained(currentAnime, allTopics)) {
                break
            }
        }
    } catch (e: CancellationException) {
        dataSynchronizer.commit(StarredAnimeMutations.ChangeRefreshState(anime.id, RefreshState.Cancelled))
        throw e
    } catch (e: Throwable) {
        dataSynchronizer.commit(StarredAnimeMutations.ChangeRefreshState(anime.id, RefreshState.Failed(e)))
        throw e
    }

    val time = System.currentTimeMillis()
    if (allEpisodesContained(currentAnime, allTopics)) {
        dataSynchronizer.commit(
            StarredAnimeMutations.AddEpisodes(anime.id, allTopics.mapNotNullTo(HashSet()) { it.details?.episode })
                    then StarredAnimeMutations.ChangeRefreshState(anime.id, RefreshState.Success(time))
        )
    } else {
        dataSynchronizer.commit(StarredAnimeMutations.ChangeRefreshState(anime.id, RefreshState.Success(time)))
    }
}

private fun allEpisodesContained(
    currentAnime: StarredAnime,
    allTopics: MutableList<Topic>
) = currentAnime.episodes.all { episode -> allTopics.any { it.details?.episode == episode } }

@Composable
fun AnimatedSearchButton(onClick: () -> Unit) {

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
                    Res.painter.magnify,
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
                SearchResultField(fetchingState) {
                    Text(
                        LocalI18n.current.getString("search.empty"),
                        style = AppTheme.typography.bodyMedium,
                    )
                }
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

                items(topics, { it.id }, { it.details?.otherTitles?.isEmpty() }) { topic ->
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
                        Modifier
                            .padding(top = spacedBy + 8.dp, bottom = spacedBy + 8.dp) // extra 8.dp padding
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

                        SearchResultField(fetchingState) {
                            Text(
                                LocalI18n.current.getString("search.end"),
                                style = AppTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultField(
    fetchingState: FetchingState,
    emptyResult: @Composable () -> Unit,
) {
    when (fetchingState) {
        FetchingState.Idle, is FetchingState.Fetching -> {
            CircularProgressIndicator(color = AppTheme.colorScheme.primary)
        }

        FetchingState.Succeed -> {
            emptyResult()
        }

        is FetchingState.Failed -> {
            Text(
                String.format(
                    LocalI18n.current.getString("search.failed"),
                    fetchingState.exception.localizedMessage
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

fun createTestAppDataSynchronizer(scope: CoroutineScope): AppDataSynchronizer {
    return AppDataSynchronizerImpl(
        scope.coroutineContext,
        remoteSynchronizer = null,
        backingStorage = InMemoryMutableProperty { "" },
        localSyncSettingsFlow = flowOf(LocalSyncSettings()),
        promptSwitchToOffline = {
            it.printStackTrace()
            true
        }
    )
}

@Preview
@Composable
private fun PreviewTopicList() {
    val (starred, onStarredChange) = remember { mutableStateOf(false) }

    LiveTopicList(
        remember {
            ApplicationState(
                initialClient = AnimationGardenClient.Factory.create {},
                appDataSynchronizer = { scope -> createTestAppDataSynchronizer(scope) })
        },
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