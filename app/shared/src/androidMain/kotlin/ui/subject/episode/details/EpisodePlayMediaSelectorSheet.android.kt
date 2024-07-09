package me.him188.ani.app.ui.subject.episode.details

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.persistent.preference.MediaSelectorSettings
import me.him188.ani.app.data.source.media.selector.DefaultMediaSelector
import me.him188.ani.app.data.source.media.selector.MediaSelectorContext
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberBackgroundScope
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.emptyMediaSourceResultsPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.previewMediaList

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

@Composable
fun rememberTestMediaSelectorPresentation(): MediaSelectorPresentation {
    val backgroundScope = rememberBackgroundScope()
    return remember(backgroundScope) { createState(backgroundScope.backgroundScope) }
}

private fun createState(backgroundScope: CoroutineScope) =
    MediaSelectorPresentation(
        DefaultMediaSelector(
            mediaSelectorContextNotCached = flowOf(MediaSelectorContext.EmptyForPreview),
            mediaListNotCached = MutableStateFlow(previewMediaList),
            savedUserPreference = flowOf(MediaPreference.Empty),
            savedDefaultPreference = flowOf(MediaPreference.Empty),
            mediaSelectorSettings = flowOf(MediaSelectorSettings.Default),
        ),
        backgroundScope.coroutineContext,
    )
