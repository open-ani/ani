package me.him188.animationgarden.desktop.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import me.him188.animationgarden.api.model.*
import me.him188.animationgarden.api.model.FileSize.Companion.megaBytes
import me.him188.animationgarden.api.tags.Episode
import me.him188.animationgarden.api.tags.Resolution
import me.him188.animationgarden.api.tags.SubtitleLanguage
import me.him188.animationgarden.desktop.AppTheme
import me.him188.animationgarden.desktop.i18n.LocalI18n
import me.him188.animationgarden.desktop.i18n.ProvideResourceBundleI18n
import me.him188.animationgarden.desktop.ui.widgets.ToggleStarButton
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
            .padding(horizontal = 16.dp, vertical = 16.dp)
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

        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                list = currentWorkState.episodes.value,
                key = { it.raw },
                isSelected = { currentWorkState.selectedEpisode.value == it },
                onClick = onClickEpisode,
                enabled = { visibleEpisodes.contains(it) },
                content = {
                    Text(
                        it.raw,
                        color = if (isEpisodeWatched(it)) {
                            LocalTextStyle.current.color.copy(alpha = 0.3f)
                        } else {
                            LocalTextStyle.current.color
                        }
                    )
                },
            )


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
                content = { Text(if (it == SubtitleLanguage.Other) LocalI18n.current.getString("subtitles.other") else it.id) },
            )

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
            )

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
            ) {
                Text(it.name)
            }
        }
    }
}

@Composable
private fun <T> FilterChipRow(
    list: List<T>,
    key: (item: T) -> Any,
    isSelected: @Composable (T) -> Boolean,
    onClick: (T) -> Unit,
    enabled: @Composable (T) -> Boolean,
    elevation: SelectableChipElevation? = FilterChipDefaults.elevatedFilterChipElevation(),
    content: @Composable (T) -> Unit = { Text(it.toString()) },
) {
    val currentOnClick by rememberUpdatedState(onClick)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        items(list, key = key) {
            ElevatedFilterChip(
                selected = isSelected(it),
                onClick = { currentOnClick.invoke(it) },
                label = { content(it) },
                enabled = enabled.invoke(it),
                elevation = elevation,
//                modifier = Modifier.animateItemPlacement(tween(200, 100)),
            )
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

    ProvideResourceBundleI18n {
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