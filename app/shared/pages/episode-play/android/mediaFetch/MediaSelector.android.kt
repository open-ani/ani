package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.media.Media
import me.him188.ani.app.data.media.MediaProperties
import me.him188.ani.app.data.media.ResourceLocation
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.acgrip.AcgRipMediaSource
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.dmhy.DmhyMediaSource

private const val SOURCE_DMHY = DmhyMediaSource.ID
private const val SOURCE_ACG = AcgRipMediaSource.ID

private val testMediaList = listOf(
    Media(
        id = "$SOURCE_DMHY.1",
        mediaSourceId = SOURCE_DMHY,
        originalTitle = "[桜都字幕组] 孤独摇滚 ABC ABC ABC ABC ABC ABC ABC ABC ABC ABC",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        size = 122.megaBytes,
        publishedTime = System.currentTimeMillis(),
        properties = MediaProperties(
            subtitleLanguages = listOf("CHS", "CHT"),
            resolution = "1080P",
            alliance = "桜都字幕组",
        ),
    ),
    // exactly same properties as the first one, except for the ids.
    Media(
        id = "$SOURCE_ACG.1",
        mediaSourceId = SOURCE_ACG,
        originalTitle = "[桜都字幕组] 孤独摇滚 ABC ABC ABC ABC ABC ABC ABC ABC ABC ABC",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        size = 122.megaBytes,
        publishedTime = System.currentTimeMillis(),
        properties = MediaProperties(
            subtitleLanguages = listOf("CHS", "CHT"),
            resolution = "1080P",
            alliance = "桜都字幕组",
        ),
    ),

    Media(
        id = "$SOURCE_DMHY.2",
        mediaSourceId = SOURCE_DMHY,
        originalTitle = "夜晚的水母不会游泳",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        size = 233.megaBytes,
        publishedTime = System.currentTimeMillis(),
        properties = MediaProperties(
            subtitleLanguages = listOf("CHT"),
            resolution = "1080P",
            alliance = "北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组",
        ),
    ),
    Media(
        id = "$SOURCE_ACG.2",
        mediaSourceId = SOURCE_ACG,
        originalTitle = "葬送的芙莉莲",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        size = 0.bytes,
        publishedTime = System.currentTimeMillis(),
        properties = MediaProperties(
            subtitleLanguages = listOf("CHS"),
            resolution = "1080P",
            alliance = "桜都字幕组",
        ),
    ),
    Media(
        id = "$SOURCE_ACG.3",
        mediaSourceId = SOURCE_ACG,
        originalTitle = "某个生肉",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        size = 702.megaBytes,
        publishedTime = System.currentTimeMillis(),
        properties = MediaProperties(
            subtitleLanguages = listOf(),
            resolution = "1080P",
            alliance = "Lilith-Raws",
        ),
    ),
)

@Preview
@Composable
private fun PreviewMediaSelector() {
    ProvideCompositionLocalsForPreview {
        MediaSelector(
            state = remember {
                MediaSelectorState(
                    mediaListProvider = { testMediaList },
                    defaultPreferenceProvider = {
                        MediaPreference(
                            subtitleLanguage = "CHS"
                        )
                    },
                )
            },
            onDismissRequest = {},
        )
    }
}