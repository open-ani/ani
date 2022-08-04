package me.him188.animationgarden.desktop.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.animationgarden.api.model.DATE_FORMAT
import me.him188.animationgarden.api.model.Topic
import me.him188.animationgarden.desktop.AppTheme

@Composable
fun TopicItemCard(topic: Topic, onClick: () -> Unit) {
    Box(Modifier.fillMaxHeight()) {
        OutlinedCard(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(elevation = 2.dp, shape = AppTheme.shapes.large)
                .clip(AppTheme.shapes.large)
                .clickable(
                    remember { MutableInteractionSource() },
                    rememberRipple(color = AppTheme.colorScheme.surfaceTint),
                ) { onClick() }
//                .border(1.dp, AppTheme.colorScheme.outline, shape = AppTheme.shapes.large)
                .wrapContentSize(),
            shape = AppTheme.shapes.large,
        ) {
            Box(Modifier.padding(16.dp)) {
                Row {
                    val details = remember(topic.id) { topic.details }
                    Column {

                        // titles
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val chineseTitle = details?.chineseTitle
                            Text(
                                chineseTitle ?: topic.rawTitle,
                                style = AppTheme.typography.titleMedium,
                                fontWeight = FontWeight.W600
                            )

                            val otherTitle = details?.otherTitle
                            if (chineseTitle != null && otherTitle != null) {
                                Text(
                                    otherTitle,
                                    Modifier.padding(start = 12.dp),
                                    style = AppTheme.typography.titleMedium.run { copy(color = color.copy(alpha = 0.5f)) },
                                    fontWeight = FontWeight.W400,
                                )
                            }
                        }

                        val tags = details?.tags
                        if (!tags.isNullOrEmpty()) {
                            Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)) {
                                TagsView(tags)
                            }
                        }

                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row {
                                topic.alliance?.let { alliance ->
                                    Text(
                                        alliance.name,
                                        style = AppTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.W600
                                    )
                                }
                                Text(
                                    topic.author.name,
                                    Modifier.padding(start = 8.dp),
                                    style = AppTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.W400
                                )
                            }

                            Text(
                                topic.date.format(DATE_FORMAT),
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.typography.bodyMedium.color.copy(alpha = 0.5f),
                                modifier = Modifier.padding(start = 4.dp),
                                fontWeight = FontWeight.W400
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun TagsView(tags: List<String>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tags, { it }) {
            TagButton({ Text(it) }, null)
        }
    }
}

@Composable
private fun TagButton(text: @Composable () -> Unit, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    val onClickState by rememberUpdatedState(onClick)
    val elevation = ButtonDefaults.buttonElevation()
    val interactionSource = remember { MutableInteractionSource() }
    val shadowElevation by elevation.shadowElevation(true, interactionSource)
    val containerColor = AppTheme.colorScheme.surfaceColorAtElevation(shadowElevation)
    ElevatedButton(
        onClick = { onClickState?.invoke() },
        enabled = onClick != null,
        shape = AppTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = modifier.height(24.dp).wrapContentWidth(),
        colors = ButtonDefaults.elevatedButtonColors(),
        elevation = elevation,
        interactionSource = interactionSource,
    ) {
        ProvideTextStyle(
            AppTheme.typography.bodySmall.copy(
                color = AppTheme.colorScheme.contentColorFor(containerColor),
                lineHeight = 16.sp,
            )
        ) {
            text()
        }
    }
}


@Composable
@Preview
private fun PreviewTags() {
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        TagButton({ Text("HEVC-10bit") }, null)
        //     TagsView(listOf("HEVC-10bit", "AAC"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (s in listOf("HEVC-10bit", "AAC")) {
                TagButton({ Text(s) }, null)
            }
        }
    }

}