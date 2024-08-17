@file:OptIn(TestOnly::class)

package me.him188.ani.app.ui.subject.episode.details

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.episode.mediaFetch.emptyMediaSourceResultsPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.rememberTestMediaSelectorPresentation
import me.him188.ani.utils.platform.annotations.TestOnly

@Preview(name = "progress = null")
@Composable
private fun PreviewEpisodePlayMediaSelectorSheet() = ProvideCompositionLocalsForPreview {
    EpisodePlayMediaSelector(
        rememberTestMediaSelectorPresentation(),
        emptyMediaSourceResultsPresentation(),
        onDismissRequest = {},
        Modifier.background(MaterialTheme.colorScheme.surface),
    )
}

@Preview(name = "progress = 0.7f")
@Composable
private fun PreviewEpisodePlayMediaSelectorSheet2() = ProvideCompositionLocalsForPreview {
    EpisodePlayMediaSelector(
        rememberTestMediaSelectorPresentation(),
        emptyMediaSourceResultsPresentation(),
        onDismissRequest = {},
        Modifier.background(MaterialTheme.colorScheme.surface),
    )
}

@Preview(name = "progress = 1f")
@Composable
private fun PreviewEpisodePlayMediaSelectorSheet3() = ProvideCompositionLocalsForPreview {
    EpisodePlayMediaSelector(
        rememberTestMediaSelectorPresentation(),
        emptyMediaSourceResultsPresentation(),
        onDismissRequest = {},
        Modifier.background(MaterialTheme.colorScheme.surface),
    )
}
