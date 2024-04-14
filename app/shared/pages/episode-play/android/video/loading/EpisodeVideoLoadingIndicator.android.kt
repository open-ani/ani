package me.him188.ani.app.ui.subject.episode.video.loading

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.api.topic.FileSize.Companion.Unspecified
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes

@Preview(name = "Selecting Media")
@Composable
private fun PreviewEpisodeVideoLoadingIndicator() {
    ProvideCompositionLocalsForPreview {
        EpisodeVideoLoadingIndicator(
            EpisodeVideoLoadingState.deduceFrom(
                mediaSelected = false,
                videoDataReady = false,
            ),
            speedProvider = { 0.3.bytes },
        )
    }
}

@Preview(name = "Preparing")
@Composable
private fun PreviewEpisodeVideoLoadingIndicator2() {
    ProvideCompositionLocalsForPreview {
        EpisodeVideoLoadingIndicator(
            EpisodeVideoLoadingState.deduceFrom(
                mediaSelected = true,
                videoDataReady = false,
            ),
            speedProvider = { 0.3.bytes },
        )
    }
}

@Preview(name = "Buffering")
@Composable
private fun PreviewEpisodeVideoLoadingIndicator3() {
    ProvideCompositionLocalsForPreview {
        EpisodeVideoLoadingIndicator(
            EpisodeVideoLoadingState.deduceFrom(
                mediaSelected = true,
                videoDataReady = true,
            ),
            speedProvider = { 0.3.bytes },
        )
    }
}

@Preview(name = "Buffering - No Speed")
@Composable
private fun PreviewEpisodeVideoLoadingIndicator4() {
    ProvideCompositionLocalsForPreview {
        EpisodeVideoLoadingIndicator(
            EpisodeVideoLoadingState.deduceFrom(
                mediaSelected = true,
                videoDataReady = true,
            ),
            speedProvider = { Unspecified },
        )
    }
}
