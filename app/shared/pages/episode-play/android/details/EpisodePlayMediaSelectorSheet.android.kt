package me.him188.ani.app.ui.subject.episode.details

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorState
import me.him188.ani.app.ui.subject.episode.mediaFetch.testMediaList
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.topic.ResourceLocation

@Preview(name = "progress = null")
@Composable
private fun PreviewEpisodePlayMediaSelectorSheet() = ProvideCompositionLocalsForPreview {
    EpisodePlayMediaSelector(
        remember { createState() },
        onDismissRequest = {},
        Modifier.background(MaterialTheme.colorScheme.surface),
        progressProvider = { null },
    )
}

@Preview(name = "progress = 0.7f")
@Composable
private fun PreviewEpisodePlayMediaSelectorSheet2() = ProvideCompositionLocalsForPreview {
    EpisodePlayMediaSelector(
        remember { createState() },
        onDismissRequest = {},
        Modifier.background(MaterialTheme.colorScheme.surface),
        progressProvider = { 0.7f },
    )
}

@Preview(name = "progress = 1f")
@Composable
private fun PreviewEpisodePlayMediaSelectorSheet3() = ProvideCompositionLocalsForPreview {
    EpisodePlayMediaSelector(
        remember { createState() },
        onDismissRequest = {},
        Modifier.background(MaterialTheme.colorScheme.surface),
        progressProvider = { 1f },
    )
}

private fun createState() = MediaSelectorState(
    mediaListProvider = {
        listOf(
            CachedMedia(
                origin = testMediaList[0],
                cacheMediaSourceId = MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID,
                download = ResourceLocation.LocalFile("file://test.txt"),
            )
        ) + testMediaList
    },
    defaultPreferenceProvider = {
        MediaPreference(
            subtitleLanguage = "CHS"
        )
    },
)