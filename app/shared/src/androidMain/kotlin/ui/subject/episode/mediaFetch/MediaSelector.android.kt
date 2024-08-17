package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.preference.MediaSelectorSettings
import me.him188.ani.app.data.source.media.MediaCacheManager
import me.him188.ani.app.data.source.media.TestMediaList
import me.him188.ani.app.data.source.media.fetch.FilteredMediaSourceResults
import me.him188.ani.app.data.source.media.fetch.MediaSourceFetchResult
import me.him188.ani.app.data.source.media.fetch.MediaSourceFetchState
import me.him188.ani.app.data.source.media.selector.DefaultMediaSelector
import me.him188.ani.app.data.source.media.selector.MediaSelectorContext
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.mikan.MikanCNMediaSource
import me.him188.ani.datasources.mikan.MikanMediaSource
import me.him188.ani.utils.platform.annotations.TestOnly
import kotlin.coroutines.EmptyCoroutineContext

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
    val mediaSelector = rememberMediaSelectorPresentation {
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

@TestOnly
@Composable
internal fun rememberTestMediaSourceResults(): MediaSourceResultsPresentation = remember {
    MediaSourceResultsPresentation(
        FilteredMediaSourceResults(
            results = flowOf(
                listOf(
                    TestMediaSourceResult(
                        MikanMediaSource.ID,
                        MediaSourceKind.BitTorrent,
                        initialState = MediaSourceFetchState.Working,
                        results = previewMediaList,
                    ),
                    TestMediaSourceResult(
                        "dmhy",
                        MediaSourceKind.BitTorrent,
                        initialState = MediaSourceFetchState.Succeed(1),
                        results = previewMediaList,
                    ),
                    TestMediaSourceResult(
                        "acg.rip",
                        MediaSourceKind.BitTorrent,
                        initialState = MediaSourceFetchState.Disabled,
                        results = previewMediaList,
                    ),
                    TestMediaSourceResult(
                        "nyafun",
                        MediaSourceKind.WEB,
                        initialState = MediaSourceFetchState.Succeed(1),
                        results = previewMediaList,
                    ),
                    TestMediaSourceResult(
                        MikanCNMediaSource.ID,
                        MediaSourceKind.BitTorrent,
                        initialState = MediaSourceFetchState.Failed(IllegalStateException(), 1),
                        results = emptyList(),
                    ),
                ),
            ),
            settings = flowOf(MediaSelectorSettings.Default),
        ),
        EmptyCoroutineContext,
    )
}

private class TestMediaSourceResult(
    override val mediaSourceId: String,
    override val kind: MediaSourceKind,
    initialState: MediaSourceFetchState,
    results: List<Media>,
) : MediaSourceFetchResult {
    override val state: MutableStateFlow<MediaSourceFetchState> = MutableStateFlow(initialState)
    override val results: SharedFlow<List<Media>> = MutableStateFlow(results)
    private val restartCount = atomic(0)

    @OptIn(DelicateCoroutinesApi::class)
    override fun restart() {
        state.value = MediaSourceFetchState.Working
        GlobalScope.launch {
            delay(3000)
            state.value = MediaSourceFetchState.Succeed(restartCount.incrementAndGet())
        }
    }

    override fun enable() {
        if (state.value is MediaSourceFetchState.Disabled) {
            if (restartCount.compareAndSet(0, 1)) {
                state.value = MediaSourceFetchState.Idle
            }
        }
    }
}

