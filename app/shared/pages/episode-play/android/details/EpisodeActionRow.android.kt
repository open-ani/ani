package me.him188.ani.app.ui.subject.episode.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview

@Preview
@Composable
private fun PreviewEpisodeActionRow() {
    ProvideCompositionLocalsForPreview {
        EpisodeActionRow(
            mediaFetcherCompleted = true,
            {}, {}, {}, {},
        )
    }
}