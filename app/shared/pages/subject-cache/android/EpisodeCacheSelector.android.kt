package me.him188.ani.app.ui.subject.cache

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.episode.details.rememberTestMediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.rememberTestMediaSourceResults

@Preview
@Composable
private fun PreviewEpisodeCacheMediaSelector() {
    ProvideCompositionLocalsForPreview {
        EpisodeCacheMediaSelector(
            rememberTestMediaSelectorPresentation(),
            onSelect = {},
            onCancel = {},
            sourceResults = rememberTestMediaSourceResults(),
        )
    }
}
