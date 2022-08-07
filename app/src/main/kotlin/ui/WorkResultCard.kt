package me.him188.animationgarden.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import me.him188.animationgarden.api.model.Alliance
import me.him188.animationgarden.api.tags.Episode
import me.him188.animationgarden.api.tags.Resolution
import me.him188.animationgarden.api.tags.SubtitleLanguage
import me.him188.animationgarden.desktop.AppTheme

@Composable
fun OrganizedWorkView(
    workState: WorkState,
    onClickEpisode: (Episode) -> Unit,
    onClickSubtitleLanguage: (SubtitleLanguage) -> Unit,
    onClickResolution: (Resolution) -> Unit,
    onClickAlliance: (Alliance) -> Unit,
) {
    val currentWorkState by rememberUpdatedState(workState)
    val shape = AppTheme.shapes.extraLarge
    OutlinedCard(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(elevation = 2.dp, shape = shape)
            .clip(shape)
//                .border(1.dp, AppTheme.colorScheme.outline, shape = AppTheme.shapes.large)
            .wrapContentSize(),
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
                    workState.chineseName.value
                }
            )

            Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChipRow(
                    list = workState.episodes.value,
                    key = { it.raw },
                    isSelected = { currentWorkState.selectedEpisode.value == it },
                    onClick = onClickEpisode,
                    content = { Text(it.raw) }
                )

                FilterChipRow(
                    list = workState.subtitleLanguages.value,
                    key = { it.id },
                    isSelected = { currentWorkState.selectedSubtitleLanguage.value == it },
                    onClick = onClickSubtitleLanguage,
                    content = { Text(it.id) }
                )

                FilterChipRow(
                    list = workState.resolutions.value,
                    key = { it.id },
                    isSelected = { currentWorkState.selectedResolution.value == it },
                    onClick = onClickResolution,
                )

                FilterChipRow(
                    list = workState.alliances.value,
                    key = { it.id },
                    isSelected = { currentWorkState.selectedAlliance.value == it },
                    onClick = onClickAlliance,
                    content = { Text(it.name) }
                )
            }
        }
    }

}

@Composable
private fun <T> FilterChipRow(
    list: List<T>,
    key: (item: T) -> Any,
    isSelected: (T) -> Boolean,
    onClick: (T) -> Unit,
    content: @Composable (T) -> Unit = { Text(it.toString()) },
) {
    val currentOnClick by rememberUpdatedState(onClick)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        items(list, key = key) {
            FilterChip(isSelected(it), { currentOnClick.invoke(it) }, { content(it) })
        }
    }
}