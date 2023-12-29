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

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.him188.animationgarden.api.model.*
import me.him188.animationgarden.api.tags.Episode
import me.him188.animationgarden.api.tags.Resolution
import me.him188.animationgarden.api.tags.SubtitleLanguage
import me.him188.animationgarden.app.AppTheme
import me.him188.animationgarden.app.i18n.LocalI18n
import me.him188.animationgarden.app.i18n.loadResourceBundle
import me.him188.animationgarden.app.platform.LocalContext
import me.him188.animationgarden.app.ui.AnimatedTitles
import me.him188.animationgarden.app.ui.theme.darken
import me.him188.animationgarden.app.ui.widgets.ToggleStarButton
import me.him188.animationgarden.shared.models.FileSize.Companion.megaBytes
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