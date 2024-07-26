package me.him188.ani.app.ui.subject.collection.progress

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.indication.HorizontalIndicator
import me.him188.ani.app.ui.foundation.indication.IndicatedBox
import me.him188.ani.app.ui.subject.episode.list.cacheStatusIndicationColor


/**
 * @param done 当已经看完时的显示
 */
@Composable
fun PlaySubjectButton(
    state: SubjectProgressState,
    modifier: Modifier = Modifier,
) {
    val onPlay: () -> Unit = { state.episodeToPlay?.let { state.play(it.id) } }
    IndicatedBox(
        indicator = {
            state.episodeToPlay?.let { episode ->
                HorizontalIndicator(
                    6.dp,
                    CircleShape,
                    cacheStatusIndicationColor(
                        state.episodeCacheStatus(episode.id),
                        state.continueWatchingStatus is ContinueWatchingStatus.Watched,
                    ),
                    Modifier.offset(y = (-2).dp),
                )
            }
        },
    ) {
        val requiredWidth = Modifier.requiredWidth(IntrinsicSize.Max)
        when (val status = state.continueWatchingStatus) {
            is ContinueWatchingStatus.Continue -> {
                Button(onClick = onPlay, modifier) {
                    Text(
                        remember(status.episodeSort) { "继续观看 ${status.episodeSort}" },
                        requiredWidth,
                        softWrap = false,
                    )
                }
            }

            ContinueWatchingStatus.Done -> {
                TextButton({ state.episodeToPlay?.let { state.play(it.id) } }, modifier) {
                    Text("已看完", Modifier.requiredWidth(IntrinsicSize.Max), softWrap = false)
                }
            }

            ContinueWatchingStatus.NotOnAir -> {
                FilledTonalButton(onClick = onPlay, modifier) {
                    Text(
                        "还未开播", requiredWidth,
                        softWrap = false,
                    )
                }
            }

            ContinueWatchingStatus.Start -> {
                Button(onClick = onPlay, modifier) {
                    Text(
                        "开始观看", requiredWidth,
                        softWrap = false,
                    )
                }
            }

            is ContinueWatchingStatus.Watched -> {
                FilledTonalButton(onClick = onPlay, modifier) {
                    Text(
                        "看到 ${status.episodeSort}", requiredWidth,
                        softWrap = false,
                    )
                }
            }

            null -> {}
        }
    }
}
