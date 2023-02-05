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

package me.him188.animationgarden.app.ui.search

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.animationgarden.api.model.*
import me.him188.animationgarden.api.model.FileSize.Companion.megaBytes
import me.him188.animationgarden.api.tags.Episode
import me.him188.animationgarden.api.tags.Resolution
import me.him188.animationgarden.api.tags.SubtitleLanguage
import me.him188.animationgarden.app.AppTheme
import me.him188.animationgarden.app.app.RefreshState
import me.him188.animationgarden.app.i18n.LocalI18n
import me.him188.animationgarden.app.i18n.loadResourceBundle
import me.him188.animationgarden.app.platform.LocalContext
import me.him188.animationgarden.app.platform.Res
import me.him188.animationgarden.app.ui.AnimatedTitles
import me.him188.animationgarden.app.ui.theme.darken
import me.him188.animationgarden.app.ui.widgets.ToggleStarButton
import java.time.LocalDateTime

@Composable
fun OrganizedViewCard(
    organizedViewState: OrganizedViewState,
    visibleTopics: List<Topic>,
    isEpisodeWatched: @Composable (Episode) -> Boolean,
    onClickEpisode: (Episode) -> Unit,
    onClickSubtitleLanguage: (SubtitleLanguage) -> Unit,
    onClickResolution: (Resolution) -> Unit,
    onClickAlliance: (Alliance) -> Unit,
    starred: Boolean,
    onStarredChange: (Boolean) -> Unit,
) {
    val shape = AppTheme.shapes.extraLarge

    OutlinedCard(
        Modifier
            .shadow(elevation = 2.dp, shape = shape)
            .clip(shape)
//                .border(1.dp, AppTheme.colorScheme.outline, shape = AppTheme.shapes.large)
            .wrapContentHeight()
            .fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.outlinedCardColors(
            containerColor = AppTheme.colorScheme.surface,
            disabledContainerColor = AppTheme.colorScheme.surface,
        ),
    ) {
        Box(Modifier.fillMaxSize()) {
            OrganizedViewContent(
                organizedViewState = organizedViewState,
                visibleTopics = visibleTopics,
                isEpisodeWatched = isEpisodeWatched,
                onClickEpisode = onClickEpisode,
                onClickSubtitleLanguage = onClickSubtitleLanguage,
                onClickResolution = onClickResolution,
                onClickAlliance = onClickAlliance,
                starred = starred,
                onStarredChange = onStarredChange
            )
        }
    }

}


@Composable
private fun OrganizedViewContent(
    organizedViewState: OrganizedViewState,
    visibleTopics: List<Topic>,
    isEpisodeWatched: @Composable (Episode) -> Boolean,
    onClickEpisode: (Episode) -> Unit,
    onClickSubtitleLanguage: (SubtitleLanguage) -> Unit,
    onClickResolution: (Resolution) -> Unit,
    onClickAlliance: (Alliance) -> Unit,
    starred: Boolean,
    onStarredChange: (Boolean) -> Unit,
) {
    val currentWorkState by rememberUpdatedState(organizedViewState)
    val currentVisibleTopics by rememberUpdatedState(visibleTopics)

    Column(Modifier.padding(16.dp)) {
        AnimatedTitles(
            chineseTitle = organizedViewState.chineseName.value,
            otherTitles = organizedViewState.otherNames.value,
            episode = null,
            rawTitle = {
                // This actually shouldn't happen, since we always provide chineseName not null.
                organizedViewState.otherNames.value.firstOrNull() ?: ""
            },
            rowModifier = Modifier.height(26.dp),
            topEnd = {
                Box(
                    Modifier
                        .padding(end = 16.dp)
                        .requiredWidth(IntrinsicSize.Max)
                        .requiredHeight(IntrinsicSize.Max)
                ) {
                    ToggleStarButton(starred, onStarredChange, modifier = Modifier.size(26.dp))
                }
            }
        )

        Column(
            Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val visibleEpisodes by remember {
                derivedStateOf {
                    currentWorkState.episodes.value.filter { item ->
                        currentWorkState.selectedEpisode.value == item || currentVisibleTopics.any { topic ->
                            topic.details?.episode == item
                        }
                    }
                }
            }
            FilterChipRow(
                list = remember(currentWorkState.episodes.value) { currentWorkState.episodes.value.toList() },
                key = { it.raw },
                isSelected = { currentWorkState.selectedEpisode.value == it },
                onClick = onClickEpisode,
                enabled = { visibleEpisodes.contains(it) },
                isExpanded = false,
            ) {
                Text(
                    it.raw,
                    color = if (isEpisodeWatched(it)) {
                        LocalTextStyle.current.color.darken()
                    } else {
                        LocalTextStyle.current.color
                    }
                )
            }


            val subtitleLanguages by remember {
                derivedStateOf {
                    val hasAnyTopicWithUnrecognizedLanguage = currentVisibleTopics.any { topic ->
                        topic.details?.subtitleLanguages.isNullOrEmpty()
                    }
                    if (hasAnyTopicWithUnrecognizedLanguage) {
                        currentWorkState.subtitleLanguages.value + SubtitleLanguage.Other
                    } else {
                        currentWorkState.subtitleLanguages.value
                    }
                }
            }
            val visibleLanguages: List<SubtitleLanguage> by remember {
                derivedStateOf {
                    subtitleLanguages
                        .filter { item ->
                            item == SubtitleLanguage.Other ||
                                    currentWorkState.selectedSubtitleLanguage.value == item ||
                                    currentVisibleTopics.any { topic ->
                                        topic.details?.subtitleLanguages?.contains(item) == true
                                    }
                        }
                }
            }
            FilterChipRow(
                list = subtitleLanguages,
                key = { it.id },
                isSelected = { currentWorkState.selectedSubtitleLanguage.value == it },
                onClick = onClickSubtitleLanguage,
                enabled = { visibleLanguages.contains(it) },
                isExpanded = false,
            ) { Text(if (it == SubtitleLanguage.Other) LocalI18n.current.getString("subtitles.other") else it.id) }

            val visibleResolutions by remember {
                derivedStateOf {
                    currentWorkState.resolutions.value.filter { item ->
                        currentWorkState.selectedResolution.value == item || currentVisibleTopics.any { topic ->
                            topic.details?.resolution == item
                        }
                    }
                }
            }
            FilterChipRow(
                list = currentWorkState.resolutions.value,
                key = { it.id },
                isSelected = { currentWorkState.selectedResolution.value == it },
                onClick = onClickResolution,
                enabled = { visibleResolutions.contains(it) },
                isExpanded = false,
            ) {
                Text(it.id)
            }

            val visibleAlliances by remember {
                derivedStateOf {
                    currentWorkState.alliances.value.filter { item ->
                        currentWorkState.selectedAlliance.value == item || currentVisibleTopics.any { topic ->
                            topic.alliance?.id == item.id
                        }
                    }
                }
            }
            FilterChipRow(
                list = currentWorkState.alliances.value,
                key = { it.id },
                isSelected = { currentWorkState.selectedAlliance.value == it },
                onClick = onClickAlliance,
                enabled = { visibleAlliances.contains(it) },
                isExpanded = false,
            ) {
                Text(it.name)
            }
        }
    }
}

@Composable
expect fun <T> FilterChipRow(
    list: List<T>,
    key: (item: T) -> Any,
    isSelected: @Composable (T) -> Boolean,
    onClick: ((T) -> Unit)?,
    enabled: @Composable (T) -> Boolean,
    isExpanded: Boolean,
    elevation: SelectableChipElevation? = FilterChipDefaults.elevatedFilterChipElevation(),
    refreshState: RefreshState? = null,
    onClickRefreshResult: (() -> Unit)? = null,
    content: @Composable (T) -> Unit = { Text(it.toString()) },
)

@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS") // otherwise Compose compiler will complain
@Composable
fun <T> FilterChipRowByLazyRow(
    list: List<T>,
    key: (item: T) -> Any,
    isSelected: @Composable (T) -> Boolean,
    onClick: ((T) -> Unit)?,
    enabled: @Composable (T) -> Boolean,
    elevation: SelectableChipElevation? = FilterChipDefaults.elevatedFilterChipElevation(),
    refreshState: RefreshState? = null,
    onClickRefreshResult: (() -> Unit)? = null,
    content: @Composable (T) -> Unit = { Text(it.toString()) },
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnClickRefreshState by rememberUpdatedState(onClickRefreshResult)


    val showSuccessHint by animateFloatAsState(if (refreshState !is RefreshState.Success) 1f else 0f, tween(2000))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        val cardHeight = 32.dp
        val textHeight = 24.sp
        val progressSize = 18.dp
        val tickSize = 24.dp
        items(list, key = key) {
            ElevatedFilterChip(
                selected = isSelected(it),
                onClick = { currentOnClick?.invoke(it) },
                label = {
                    ProvideTextStyle(LocalTextStyle.current.copy(lineHeight = textHeight)) {
                        content(it)
                    }
                },
                enabled = enabled.invoke(it),
                elevation = elevation,
                modifier = Modifier.height(cardHeight),
//                modifier = Modifier.animateItemPlacement(tween(200, 100)),
            )
        }
        if (refreshState != null && (refreshState !is RefreshState.Success || showSuccessHint > 0)) {
            item(key = "refreshing") {
                RefreshingChip(
                    refreshState = refreshState,
                    textHeight = textHeight,
                    cardHeight = cardHeight,
                    progressSize = progressSize,
                    tickSize = tickSize,
                    elevation = elevation,
                    onClick = currentOnClickRefreshState
                )
            }
        }
    }
}

@Composable
fun RefreshingChip(
    refreshState: RefreshState,
    textHeight: TextUnit = 24.sp,
    cardHeight: Dp = 32.dp,
    progressSize: Dp = 18.dp,
    tickSize: Dp = 24.dp,
    elevation: SelectableChipElevation? = null,
    onClick: (() -> Unit)? = null,
) {
    val currentOnClick by rememberUpdatedState(onClick)
    FilterChip(
        selected = false,
        onClick = { currentOnClick?.invoke() },
        label = {
            ProvideTextStyle(LocalTextStyle.current.copy(lineHeight = textHeight)) {
                when (refreshState) {
                    is RefreshState.Failed -> Text(LocalI18n.current.getString("starred.update.failed"))
                    RefreshState.Refreshing -> CircularProgressIndicator(
                        Modifier.size(progressSize),
                        strokeWidth = 2.dp
                    )
                    is RefreshState.Success -> {
                        Icon(
                            Res.painter.check,
                            LocalI18n.current.getString("starred.update.succeed"),
                            Modifier.size(tickSize),
                            tint = AppTheme.colorScheme.primary
                        )
                    }
                    RefreshState.Cancelled -> {
                        // nop
                    }
                }
            }
        },
        enabled = false,
        elevation = elevation,
        modifier = Modifier.height(cardHeight),
        border = null,
//                modifier = Modifier.animateItemPlacement(tween(200, 100)),
    )
}

@Preview
@Composable
private fun PreviewOrganizedWorkView() {
    val topics = remember {
        listOf(
            Topic(
                "1",
                LocalDateTime.now(),
                TopicCategory("1", "Bangumi"),
                null,
                "Raw Title",
                1,
                MagnetLink(""),
                123.megaBytes,
                UserImpl("1", "tets")
            )
        )
    }
    val context = LocalContext.current
    val currentBundle = remember(Locale.current.language) { loadResourceBundle(context) }
    CompositionLocalProvider(LocalI18n provides currentBundle) {
        Box(Modifier.size(height = 600.dp, width = 400.dp)) {
            val (starred, onStarredChange) = remember { mutableStateOf(false) }

            OrganizedViewCard(
                organizedViewState = remember { OrganizedViewState().apply { setTopics(topics, "lycoris") } },
                visibleTopics = topics,
                isEpisodeWatched = { false },
                onClickEpisode = {},
                onClickSubtitleLanguage = {},
                onClickResolution = {},
                onClickAlliance = {},
                starred = starred,
                onStarredChange = onStarredChange
            )
        }
    }
}