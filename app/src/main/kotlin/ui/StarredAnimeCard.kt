package me.him188.animationgarden.desktop.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.him188.animationgarden.api.model.DATE_FORMAT
import me.him188.animationgarden.api.tags.Episode
import me.him188.animationgarden.desktop.AppTheme
import me.him188.animationgarden.desktop.app.StarredAnime
import me.him188.animationgarden.desktop.ui.widgets.ToggleStarButton
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun StarredAnimeCard(
    anime: StarredAnime,
    onStarRemove: () -> Unit,
    onClick: (episode: Episode?) -> Unit,
) {
    val currentAnime by rememberUpdatedState(anime)
    val currentOnStarRemove by rememberUpdatedState(onStarRemove)
    val currentOnClick by rememberUpdatedState(onClick)
    Box(Modifier.fillMaxHeight()) {
        val shape = AppTheme.shapes.large
        OutlinedCard(
            Modifier
                .shadow(elevation = 2.dp, shape = shape)
                .clip(shape)
                .clickable(
                    remember { MutableInteractionSource() },
                    rememberRipple(color = AppTheme.colorScheme.surfaceTint),
                ) { currentOnClick(null) }
//                .border(1.dp, AppTheme.colorScheme.outline, shape = AppTheme.shapes.large)
                .wrapContentSize(),
            shape = shape,
        ) {
            Column(Modifier.padding(all = 16.dp)) { // padding for inner content
                // titles
                AnimatedTitles(
                    chineseTitle = currentAnime.primaryName,
                    otherTitles = currentAnime.secondaryNames,
                    episode = null,
                    rawTitle = { "" },
                    rowModifier = Modifier.height(26.dp),
                    topEnd = {
                        Box(
                            Modifier
                                .padding(end = 16.dp)
                                .requiredWidth(IntrinsicSize.Max)
                                .requiredHeight(IntrinsicSize.Max)
                        ) {
                            ToggleStarButton(true, { currentOnStarRemove() }, modifier = Modifier.size(26.dp))
                        }
                    }
                )

                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    FilterChipRow(
                        list = currentAnime.episodes,
                        key = { it.raw },
                        isSelected = { false },
                        onClick = onClick,
                        enabled = { true },
                        content = {
                            Text(
                                it.raw,
                                color = if (currentAnime.watchedEpisodes.contains(it)) {
                                    LocalTextStyle.current.color.copy(alpha = 0.3f)
                                } else {
                                    LocalTextStyle.current.color
                                }
                            )
                        },
                    )


                    val dateFormatted by remember {
                        derivedStateOf {
                            LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(anime.starTimeMillis),
                                ZoneId.systemDefault()
                            ).format(DATE_FORMAT)
                        }
                    }
                    Text(
                        dateFormatted,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.typography.bodyMedium.color.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 4.dp),
                        fontWeight = FontWeight.W400,
                        maxLines = 1,
                    )
                }
            }
        }

    }
}
