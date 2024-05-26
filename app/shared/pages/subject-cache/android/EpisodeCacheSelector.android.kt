package me.him188.ani.app.ui.subject.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorState
import me.him188.ani.app.ui.subject.episode.mediaFetch.rememberTestMediaSourceResults
import me.him188.ani.app.ui.subject.episode.mediaFetch.testMediaList
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.topic.ResourceLocation

@Preview
@Composable
private fun PreviewEpisodeCacheMediaSelector() {
    ProvideCompositionLocalsForPreview {
        EpisodeCacheMediaSelector(
            remember { createState() },
            onSelect = {},
            onCancel = {},
            sourceResults = rememberTestMediaSourceResults(),
        )
    }
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
            subtitleLanguageId = "CHS"
        )
    },
)