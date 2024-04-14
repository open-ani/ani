package me.him188.ani.app.ui.subject.cache

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.api.EpisodeSort

@Preview
@Composable
private fun PreviewEditEpisodeCachePopup() {
    ProvideCompositionLocalsForPreview {
        EditEpisodeCache(
            EpisodeSort(1),
            "第一集的标题",
            onDismissRequest = {},
        )
    }
}