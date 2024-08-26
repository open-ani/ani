package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.models.preference.MediaSelectorSettings
import me.him188.ani.app.data.source.media.TestMediaList
import me.him188.ani.app.data.source.media.cache.MediaCacheManager
import me.him188.ani.app.data.source.media.selector.DefaultMediaSelector
import me.him188.ani.app.data.source.media.selector.MediaSelectorContext
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.utils.platform.annotations.TestOnly

@TestOnly
internal val previewMediaList = TestMediaList.run {
    listOf(
        CachedMedia(
            origin = this[0],
            cacheMediaSourceId = MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID,
            download = ResourceLocation.LocalFile("file://test.txt"),
        ),
    ) + this
}

@OptIn(TestOnly::class)
@PreviewLightDark
@Composable
private fun PreviewMediaSelector() {
    val mediaSelector = rememberMediaSelectorPresentation(rememberTestMediaSourceInfoProvider()) {
        DefaultMediaSelector(
            mediaSelectorContextNotCached = flowOf(MediaSelectorContext.EmptyForPreview),
            mediaListNotCached = MutableStateFlow(
                listOf(
                    CachedMedia(
                        origin = previewMediaList[0],
                        cacheMediaSourceId = MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID,
                        download = ResourceLocation.LocalFile("file://test.txt"),
                    ),
                ) + previewMediaList,
            ),
            savedUserPreference = flowOf(MediaPreference.Empty),
            savedDefaultPreference = flowOf(
                MediaPreference.PlatformDefault.copy(
                    subtitleLanguageId = "CHS",
                ),
            ),
            mediaSelectorSettings = flowOf(MediaSelectorSettings.Default),
        )
    }
    ProvideCompositionLocalsForPreview {
        MediaSelectorView(
            state = mediaSelector,
            sourceResults = {
                MediaSourceResultsView(
                    rememberTestMediaSourceResults(),
                    mediaSelector,
                )
            },
        )
    }
}

