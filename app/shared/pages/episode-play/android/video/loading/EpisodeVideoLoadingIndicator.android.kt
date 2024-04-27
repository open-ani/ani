package me.him188.ani.app.ui.subject.episode.video.loading

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.episode.VideoLoadingState
import me.him188.ani.datasources.api.topic.FileSize.Companion.Unspecified
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes

@Preview(name = "Selecting Media")
@Composable
private fun PreviewEpisodeVideoLoadingIndicator() {
    ProvideCompositionLocalsForPreview {
        EpisodeVideoLoadingIndicator(
            VideoLoadingState.Initial,
            speedProvider = { 0.3.bytes },
        )
    }
}

@Preview(name = "ResolvingSource")
@Composable
private fun PreviewEpisodeVideoLoadingIndicator2() {
    ProvideCompositionLocalsForPreview {
        EpisodeVideoLoadingIndicator(
            VideoLoadingState.ResolvingSource,
            speedProvider = { 0.3.bytes },
        )
    }
}

@Preview(name = "ResolvingSource")
@Composable
private fun PreviewEpisodeVideoLoadingIndicator5() {
    ProvideCompositionLocalsForPreview {
        EpisodeVideoLoadingIndicator(
            VideoLoadingState.DecodingData,
            speedProvider = { 0.3.bytes },
        )
    }
}

@Preview(name = "Buffering")
@Composable
private fun PreviewEpisodeVideoLoadingIndicator3() {
    ProvideCompositionLocalsForPreview {
        EpisodeVideoLoadingIndicator(
            VideoLoadingState.Succeed,
            speedProvider = { 0.3.bytes },
        )
    }
}

@Preview(name = "Failed")
@Composable
private fun PreviewEpisodeVideoLoadingIndicator7() {
    ProvideCompositionLocalsForPreview {
        EpisodeVideoLoadingIndicator(
            VideoLoadingState.ResolutionTimedOut,
            speedProvider = { Unspecified },
        )
    }
}

@Preview(name = "Buffering - No Speed")
@Composable
private fun PreviewEpisodeVideoLoadingIndicator4() {
    ProvideCompositionLocalsForPreview {
        EpisodeVideoLoadingIndicator(
            VideoLoadingState.Succeed,
            speedProvider = { Unspecified },
        )
    }
}
