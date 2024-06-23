package me.him188.ani.app.ui.subject.episode.video.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.text.ProvideTextStyleContentColor
import me.him188.ani.app.ui.foundation.theme.aniDarkColorTheme

@Composable
fun EpisodeVideoSettingsSideSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    navigationButton: @Composable () -> Unit = { },
    closeButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    // Compose does not yet support side sheets
    // https://m3.material.io/components/side-sheets/overview

    MaterialTheme(aniDarkColorTheme()) {
        BoxWithConstraints(
            Modifier.fillMaxSize()
                .windowInsetsPadding(BottomSheetDefaults.windowInsets)
                .clickable(
                    onClick = onDismissRequest,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ),
            contentAlignment = Alignment.TopEnd,
        ) {
            // Layout guideline:
            // https://m3.material.io/components/side-sheets/guidelines#96245186-bae4-4a33-b41f-17833bb2e2d7

            Surface(
                modifier
                    .clickable(
                        onClick = { }, // just to intercept clicks
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    )
                    .fillMaxHeight()
                    .widthIn(min = 300.dp, max = 400.dp)
                    .width((maxWidth * 0.28f)),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            Modifier.padding(start = 16.dp, end = 12.dp).padding(vertical = 16.dp).weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.outline) {
                                navigationButton()
                            }

                            Row(Modifier.weight(1f)) {
                                ProvideTextStyleContentColor(
                                    MaterialTheme.typography.titleLarge,
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                ) {
                                    title()
                                }
                            }
                        }

                        Box(Modifier.padding(start = 12.dp)) {
                            closeButton()
                        }
                    }

                    content()
                }
            }
        }
    }
}
