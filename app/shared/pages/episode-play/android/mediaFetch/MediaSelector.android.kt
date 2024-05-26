package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.acgrip.AcgRipMediaSource
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseSimplified
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseTraditional
import me.him188.ani.datasources.dmhy.DmhyMediaSource

private const val SOURCE_DMHY = DmhyMediaSource.ID
private const val SOURCE_ACG = AcgRipMediaSource.ID

internal val testMediaList = listOf(
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
    ),
)

@PreviewLightDark
@Composable
private fun PreviewMediaSelector() {
    ProvideCompositionLocalsForPreview {
        MediaSelector(
            state = remember {
                MediaSelectorState(
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
            },
        )
    }
}