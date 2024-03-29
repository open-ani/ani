package me.him188.ani.app.ui.subject.episode.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle


@Composable
internal fun PlaySourceSheet(
    viewModel: EpisodeViewModel
) {
    ModalBottomSheet(
        onDismissRequest = { viewModel.setShowPlaySourceSheet(false) },
        Modifier
    ) {
        val playSourceSelector = viewModel.playSourceSelector
        val resolutions by playSourceSelector.resolutions.collectAsStateWithLifecycle()
        val subtitleLanguages by playSourceSelector.subtitleLanguages.collectAsStateWithLifecycle()
        val alliances by playSourceSelector.candidates.collectAsStateWithLifecycle(null)
        val preferredResolution by playSourceSelector.preferredResolution.collectAsStateWithLifecycle()
        val preferredLanguage by playSourceSelector.preferredSubtitleLanguage.collectAsStateWithLifecycle()
        val preferredAlliance by playSourceSelector.finalSelectedAllianceMangled.collectAsStateWithLifecycle(null)

        Column(
            Modifier
                .navigationBarsPadding()
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlaySourceFilterRow(
                resolutions,
                label = { Text("清晰度", overflow = TextOverflow.Visible) },
                key = { it.id },
                eachItem = { item ->
                    InputChip(
                        item == preferredResolution,
                        onClick = { playSourceSelector.setPreferredResolution(item) },
                        label = { Text(remember(item) { item.toString() }) }
                    )
                },
                Modifier.height(32.dp)
            )

            PlaySourceFilterRow(
                subtitleLanguages,
                label = { Text("字幕语言", overflow = TextOverflow.Visible) },
                key = { it },
                eachItem = { item ->
                    InputChip(
                        item == preferredLanguage,
                        onClick = { playSourceSelector.setPreferredSubtitleLanguage(item) },
                        label = { Text(item) }
                    )
                },
                Modifier.height(32.dp)
            )

            PlaySourceFilterFlowRow(
                alliances.orEmpty(),
                label = { Text("字幕组", overflow = TextOverflow.Visible) },
                eachItem = { item ->
                    InputChip(
                        item.allianceMangled == preferredAlliance,
                        onClick = { viewModel.launchInBackground { playSourceSelector.setPreferredCandidate(item) } },
                        label = { Text(item.allianceMangled) },
                        Modifier.height(32.dp)
                    )
                },
            )

            TextButton(
                { viewModel.setShowPlaySourceSheet(false) },
                Modifier.align(Alignment.End).padding(horizontal = 8.dp)
            ) {
                Text("完成")
            }
        }

        Spacer(Modifier.navigationBarsPadding())
    }
}

private val PLAY_SOURCE_LABEL_WIDTH = 68.dp // 正好放得下四个字

@Composable
private fun <T> PlaySourceFilterFlowRow(
    items: List<T>,
    label: @Composable () -> Unit,
    eachItem: @Composable (item: T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.Top) {
        ProvideTextStyle(MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)) {
            Box(Modifier.padding(top = 4.dp).width(PLAY_SOURCE_LABEL_WIDTH)) {
                label()
            }
        }

        Box(
            Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (item in items) {
                    eachItem(item)
                }
            }
        }
    }
}

@Composable
private fun <T> PlaySourceFilterRow(
    items: List<T>,
    label: @Composable () -> Unit,
    key: (item: T) -> Any,
    eachItem: @Composable (item: T) -> Unit,
    modifier: Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        ProvideTextStyle(MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)) {
            Box(Modifier.width(PLAY_SOURCE_LABEL_WIDTH)) {
                label()
            }
        }

        Box(
            Modifier.padding(start = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { }
                items(items, key) { item ->
                    eachItem(item)
                }
                item { }
                item { }
            }
        }
    }
}
