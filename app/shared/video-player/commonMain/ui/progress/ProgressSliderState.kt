package me.him188.ani.app.videoplayer.ui.progress

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import me.him188.ani.app.ui.theme.aniDarkColorTheme
import me.him188.ani.app.videoplayer.PlayerState
import me.him188.ani.datasources.bangumi.processing.fixToString
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.math.roundToLong

/**
 * @see ProgressSlider
 */
@Stable
class ProgressSliderState(
    currentPositionMillis: State<Long>,
    totalDurationMillis: State<Long>,
    private val onPreview: (positionMillis: Long) -> Unit,
    private val onPreviewFinished: (positionMillis: Long) -> Unit,
) {
    val currentPositionMillis: Long by currentPositionMillis
    val totalDurationMillis: Long by totalDurationMillis

    private var previewPositionMillis: Long by mutableStateOf(-1L)

    private fun previewPosition(positionMillis: Long) {
        previewPositionMillis = positionMillis
        onPreview(positionMillis)
    }

    /**
     * Sets the slider to move to the given position.
     * [onPreview] will be triggered.
     */
    fun previewPositionRatio(ratio: Float) {
        previewPosition((totalDurationMillis * ratio).roundToLong())
    }

    /**
     * The position to display in the progress slider. If a preview is active, this will be the preview position.
     */
    private val displayPositionMillis: Long by derivedStateOf {
        val previewPositionMillis = previewPositionMillis
        if (previewPositionMillis != -1L) {
            previewPositionMillis
        } else {
            currentPositionMillis.value
        }
    }

    /**
     * The ratio of the current display position to the total duration. Range is `0..1`
     */
    val displayPositionRatio by derivedStateOf {
        val total = this.totalDurationMillis
        if (total == 0L) {
            return@derivedStateOf 0f
        }
        displayPositionMillis.toFloat() / total
    }

    fun finishPreview() {
        onPreviewFinished(previewPositionMillis)
        previewPositionMillis = -1L
    }
}

/**
 * The slider to control the progress of a video, with a preview feature.
 */
// Preview: PreviewVideoScaffoldFullscreen
@Composable
fun ProgressSlider(
    state: ProgressSliderState,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        val text by remember {
            derivedStateOf {
                renderSeconds(state.currentPositionMillis / 1000, state.totalDurationMillis / 1000)
            }
        }
        val reserve by remember {
            derivedStateOf {
                renderSecondsReserve(state.totalDurationMillis / 1000)
            }
        }
        Box(Modifier.padding(end = 8.dp), contentAlignment = Alignment.Center) {
            Text(reserve, Modifier.alpha(0f)) // fix width
            Text(text = text)
        }

        Slider(
            value = state.displayPositionRatio,
            valueRange = 0f..1f,
            onValueChange = { state.previewPositionRatio(it) },
            track = {
                SliderDefaults.Track(
                    it,
                    colors = SliderDefaults.colors(
                        activeTrackColor = aniDarkColorTheme().secondary,
                        inactiveTrackColor = Color.DarkGray,
                    )
                )
            },
            onValueChangeFinished = {
                state.finishPreview()
            },
            modifier = Modifier.alpha(0.8f),
        )
    }
}

@Composable
fun ProgressSlider(
    playerState: PlayerState,
    modifier: Modifier = Modifier,
) {
    val currentPosition = playerState.currentPositionMillis.collectAsStateWithLifecycle()
    val totalDuration = remember(playerState) {
        playerState.videoProperties.filterNotNull().map { it.durationMillis }
    }.collectAsStateWithLifecycle(0L)

    ProgressSlider(
        remember {
            ProgressSliderState(
                currentPosition,
                totalDuration,
                onPreview = {},
                onPreviewFinished = { playerState.seekTo(it) },
            )
        },
        modifier
    )
}


/**
 * Returns the most wide text that [renderSeconds] may return for that [totalSecs]. This can be used to reserve space for the text.
 */
@Stable
private fun renderSecondsReserve(
    totalSecs: Long?
): String = when (totalSecs) { // 8 is usually the visually widest character
    null -> "88:88 / 88:88"
    in 0..59 -> "88:88 / 88:88"
    in 60..3599 -> "88:88 / 88:88"
    in 3600..Int.MAX_VALUE -> "88:88:88 / 88:88:88"
    else -> "88:88 / 88:88"
}

/**
 * Renders position into format like "888:88:88 / 888:88:88" (hours:minutes:seconds)
 * @see renderSecondsReserve
 */
@Stable
private fun renderSeconds(current: Long, total: Long?): String {
    if (total == null) {
        return "00:${current.fixToString(2)} / 00:00"
    }
    return if (current < 60 && total < 60) {
        "00:${current.fixToString(2)} / 00:${total.fixToString(2)}"
    } else if (current < 3600 && total < 3600) {
        val startM = (current / 60).fixToString(2)
        val startS = (current % 60).fixToString(2)
        val endM = (total / 60).fixToString(2)
        val endS = (total % 60).fixToString(2)
        """$startM:$startS / $endM:$endS"""
    } else {
        val startH = (current / 3600).fixToString(2)
        val startM = (current % 3600 / 60).fixToString(2)
        val startS = (current % 60).fixToString(2)
        val endH = (total / 3600).fixToString(2)
        val endM = (total % 3600 / 60).fixToString(2)
        val endS = (total % 60).fixToString(2)
        """$startH:$startM:$startS / $endH:$endM:$endS"""
    }
}
