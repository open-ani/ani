package me.him188.ani.app.videoplayer.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import me.him188.ani.app.tools.torrent.TorrentMediaCacheProgressState
import me.him188.ani.app.torrent.api.files.PieceState
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.theme.aniDarkColorTheme
import kotlin.time.Duration.Companion.milliseconds

// Try interactive preview to see cache progress change
@Preview
@Composable
fun PreviewMediaProgressSliderInteractive() = ProvideCompositionLocalsForPreview() {
    var currentPositionMillis by remember { mutableLongStateOf(2000) }
    val totalDurationMillis by remember { mutableLongStateOf(30_000) }
    val pieces = remember {
        Piece.buildPieces(16, 0) {
            1_000
        }
    }
    var isFinished by remember {
        mutableStateOf(false)
    }

    val cacheProgress = remember {
        TorrentMediaCacheProgressState(
            pieces,
            isFinished = { isFinished },
        )
    }
    LaunchedEffect(true) {
        for (piece in pieces) {
            delay(200.milliseconds)
            piece.state.value = PieceState.DOWNLOADING
            cacheProgress.update()
            delay(200.milliseconds)
            piece.state.value = PieceState.FINISHED
            cacheProgress.update()
        }
        isFinished = true
    }
    MaterialTheme(aniDarkColorTheme()) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            MediaProgressSlider(
                remember {
                    MediaProgressSliderState(
                        { currentPositionMillis }, { totalDurationMillis },
                        onPreview = {},
                        onPreviewFinished = { currentPositionMillis = it },
                    )
                },
                cacheProgress,
            )
        }
    }
}

private fun buildPiecesWithStep(
    state: PieceState
): List<Piece> = Piece.buildPieces(16, 0) { 1_000 }.apply {
    // simulate non-consecutive cache
    for (i in indices step 2) {
        this[i].state.value = state
    }
}

@Composable
fun PreviewMediaProgressSliderNonConsecutiveCacheImpl(
    pieces: List<Piece>,
) = ProvideCompositionLocalsForPreview {
    val cacheProgress = remember {
        TorrentMediaCacheProgressState(
            pieces,
            isFinished = { false },
        )
    }
    MaterialTheme(aniDarkColorTheme()) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            MediaProgressSlider(
                remember {
                    MediaProgressSliderState(
                        currentPositionMillis = { 2000 },
                        totalDurationMillis = { 30_000 },
                        onPreview = {},
                        onPreviewFinished = { },
                    )
                },
                cacheProgress,
            )
        }
    }
}


@Preview
@Composable
fun PreviewMediaProgressSliderDownloading() =
    PreviewMediaProgressSliderNonConsecutiveCacheImpl(buildPiecesWithStep(PieceState.DOWNLOADING))

@Preview
@Composable
fun PreviewMediaProgressSliderDone() =
    PreviewMediaProgressSliderNonConsecutiveCacheImpl(buildPiecesWithStep(PieceState.FINISHED))

@Preview
@Composable
fun PreviewMediaProgressSliderNotAvailable() =
    PreviewMediaProgressSliderNonConsecutiveCacheImpl(buildPiecesWithStep(PieceState.NOT_AVAILABLE))

@Preview
@Composable
fun PreviewMediaProgressSlider() =
    PreviewMediaProgressSliderNonConsecutiveCacheImpl(
        Piece.buildPieces(16, 0) { 1_000 }.apply {
            // simulate non-consecutive cache
            for (i in 0..3) {
                this[i].state.value = PieceState.FINISHED
            }
            for (i in 4..5) {
                this[i].state.value = PieceState.DOWNLOADING
            }
            for (i in 7..7) {
                this[i].state.value = PieceState.DOWNLOADING
            }
            for (i in 8..9) {
                this[i].state.value = PieceState.FINISHED
            }
            for (i in 10..13) {
                this[i].state.value = PieceState.DOWNLOADING
            }
        },
    )
