package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.data.media.fetch.FilteredMediaSourceResults
import me.him188.ani.app.data.media.fetch.MediaSourceFetchResult
import me.him188.ani.app.data.media.fetch.MediaSourceFetchState
import me.him188.ani.app.data.media.selector.DefaultMediaSelector
import me.him188.ani.app.data.media.selector.MediaSelectorContext
import me.him188.ani.app.data.models.MediaSelectorSettings
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.acgrip.AcgRipMediaSource
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseSimplified
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseTraditional
import me.him188.ani.datasources.dmhy.DmhyMediaSource
import me.him188.ani.datasources.mikan.MikanCNMediaSource
import me.him188.ani.datasources.mikan.MikanMediaSource
import kotlin.coroutines.EmptyCoroutineContext

private const val SOURCE_DMHY = DmhyMediaSource.ID
private const val SOURCE_ACG = AcgRipMediaSource.ID

internal val previewMediaList = listOf(
    DefaultMedia(
        mediaId = "$SOURCE_DMHY.1",
        mediaSourceId = SOURCE_DMHY,
        originalTitle = "[桜都字幕组] 孤独摇滚 ABC ABC ABC ABC ABC ABC ABC ABC ABC ABC",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        publishedTime = System.currentTimeMillis(),
        episodeRange = EpisodeRange.single(EpisodeSort(1)),
        properties = MediaProperties(
            subtitleLanguageIds = listOf(ChineseSimplified, ChineseTraditional).map { it.id },
            resolution = "1080P",
            alliance = "桜都字幕组",
            size = 122.megaBytes,
        ),
        kind = MediaSourceKind.BitTorrent,
        location = MediaSourceLocation.Online,
    ),
    // exactly same properties as the first one, except for the ids.
    DefaultMedia(
        mediaId = "$SOURCE_ACG.1",
        mediaSourceId = SOURCE_ACG,
        originalTitle = "[桜都字幕组] 孤独摇滚 ABC ABC ABC ABC ABC ABC ABC ABC ABC ABC",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        publishedTime = System.currentTimeMillis(),
        episodeRange = EpisodeRange.single(EpisodeSort(1)),
        properties = MediaProperties(
            subtitleLanguageIds = listOf(ChineseSimplified, ChineseTraditional).map { it.id },
            resolution = "1080P",
            alliance = "桜都字幕组",
            size = 122.megaBytes,
        ),
        kind = MediaSourceKind.BitTorrent,
        location = MediaSourceLocation.Online,
    ),

    DefaultMedia(
        mediaId = "$SOURCE_DMHY.2",
        mediaSourceId = SOURCE_DMHY,
        originalTitle = "夜晚的水母不会游泳",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        publishedTime = System.currentTimeMillis(),
        episodeRange = EpisodeRange.single(EpisodeSort(2)),
        properties = MediaProperties(
            subtitleLanguageIds = listOf(ChineseTraditional).map { it.id },
            resolution = "1080P",
            alliance = "北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组",
            size = 233.megaBytes,
        ),
        kind = MediaSourceKind.BitTorrent,
        location = MediaSourceLocation.Online,
    ),
    DefaultMedia(
        mediaId = "$SOURCE_ACG.2",
        mediaSourceId = SOURCE_ACG,
        originalTitle = "葬送的芙莉莲",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        publishedTime = System.currentTimeMillis(),
        episodeRange = EpisodeRange.single(EpisodeSort(2)),
        properties = MediaProperties(
            subtitleLanguageIds = listOf(ChineseSimplified).map { it.id },
            resolution = "1080P",
            alliance = "桜都字幕组",
            size = 0.bytes,
        ),
        kind = MediaSourceKind.BitTorrent,
        location = MediaSourceLocation.Online,
    ),
    DefaultMedia(
        mediaId = "$SOURCE_ACG.3",
        mediaSourceId = SOURCE_ACG,
        originalTitle = "某个生肉",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        publishedTime = System.currentTimeMillis(),
        episodeRange = EpisodeRange.single(EpisodeSort(3)),
        properties = MediaProperties(
            subtitleLanguageIds = listOf(),
            resolution = "1080P",
            alliance = "Lilith-Raws",
            size = 702.megaBytes,
        ),
        kind = MediaSourceKind.BitTorrent,
        location = MediaSourceLocation.Online,
    ),
).run {
    listOf(
        CachedMedia(
            origin = this[0],
            cacheMediaSourceId = MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID,
            download = ResourceLocation.LocalFile("file://test.txt"),
        ),
    ) + this
}

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
                        initialState = MediaSourceFetchState.Succeed,
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
                        initialState = MediaSourceFetchState.Succeed,
                        results = previewMediaList,
                    ),
                    TestMediaSourceResult(
                        MikanCNMediaSource.ID,
                        MediaSourceKind.BitTorrent,
                        initialState = MediaSourceFetchState.Failed(IllegalStateException()),
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

    @OptIn(DelicateCoroutinesApi::class)
    override fun restart() {
        state.value = MediaSourceFetchState.Working
        GlobalScope.launch {
            delay(3000)
            state.value = MediaSourceFetchState.Succeed
        }
    }
}

