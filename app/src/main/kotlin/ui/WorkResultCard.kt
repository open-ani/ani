package me.him188.animationgarden.desktop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import me.him188.animationgarden.api.model.Alliance
import me.him188.animationgarden.api.model.Topic
import me.him188.animationgarden.api.tags.Episode
import me.him188.animationgarden.api.tags.Resolution
import me.him188.animationgarden.api.tags.SubtitleLanguage
import me.him188.animationgarden.desktop.AppTheme
import me.him188.animationgarden.desktop.i18n.LocalI18n

@Composable
fun OrganizedWorkView(
    workState: WorkState,
    visibleTopics: List<Topic>,
    onClickEpisode: (Episode) -> Unit,
    onClickSubtitleLanguage: (SubtitleLanguage) -> Unit,
    onClickResolution: (Resolution) -> Unit,
    onClickAlliance: (Alliance) -> Unit,
) {
    val currentWorkState by rememberUpdatedState(workState)
    val currentVisibleTopics by rememberUpdatedState(visibleTopics)
    val shape = AppTheme.shapes.extraLarge

    OutlinedCard(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
        Column(Modifier.padding(16.dp)) {
            AnimatedTitles(
                chineseTitle = workState.chineseName.value,
                otherTitles = workState.otherNames.value,
                episode = null,
                rawTitle = {
                    // This actually shouldn't happen, since we always provide chineseName not null.
                    workState.otherNames.value.firstOrNull() ?: ""
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
                    content = { Text(it.raw) },
                )

                val visibleLanguages: List<SubtitleLanguage> by remember {
                    derivedStateOf {
                        currentWorkState.subtitleLanguages.value
                            .filter { item ->
                                currentWorkState.selectedSubtitleLanguage.value == item || currentVisibleTopics.any { topic ->
                                    topic.details?.subtitleLanguages?.contains(item) == false
                                }
                            }
                            .ifEmpty { listOf(SubtitleLanguage.Other) }
                    }
                }
                FilterChipRow(
                    list = currentWorkState.subtitleLanguages.value,
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

}

@Composable
private fun <T> FilterChipRow(
    list: List<T>,
    key: (item: T) -> Any,
    isSelected: @Composable (T) -> Boolean,
    onClick: (T) -> Unit,
    enabled: @Composable (T) -> Boolean,
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
//                modifier = Modifier.animateItemPlacement(tween(200, 100)),
            )
        }
    }
}