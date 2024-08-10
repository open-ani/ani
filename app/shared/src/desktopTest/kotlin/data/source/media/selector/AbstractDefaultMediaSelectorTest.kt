package me.him188.ani.app.data.source.media.selector

import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.data.models.preference.MediaSelectorSettings
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.SubtitleKind
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseSimplified
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseTraditional
import me.him188.ani.utils.platform.collections.copyPut
import me.him188.ani.utils.platform.collections.toImmutable

sealed class AbstractDefaultMediaSelectorTest {
    protected val mediaList: MutableStateFlow<MutableList<DefaultMedia>> = MutableStateFlow(mutableListOf())
    protected fun addMedia(vararg media: DefaultMedia) {
        mediaList.value.addAll(media)
    }

    protected val savedUserPreference = MutableStateFlow(DEFAULT_PREFERENCE)
    protected val savedDefaultPreference = MutableStateFlow(DEFAULT_PREFERENCE)
    protected val mediaSelectorSettings = MutableStateFlow(MediaSelectorSettings.Default)
    protected val mediaSelectorContext = MutableStateFlow(
        MediaSelectorContext(
            subjectFinished = false,
            mediaSourcePrecedence = emptyList(),
            subtitlePreferences = MediaSelectorSubtitlePreferences.AllNormal,
        ),
    )

    protected fun setSubtitlePreferences(
        preferences: MediaSelectorSubtitlePreferences = getCurrentSubtitlePreferences()
    ) {
        mediaSelectorContext.value = mediaSelectorContext.value.run {
            copy(subtitlePreferences = preferences)
        }
    }

    private fun getCurrentSubtitlePreferences() = (mediaSelectorContext.value.subtitlePreferences
        ?: MediaSelectorSubtitlePreferences.AllNormal)

    protected fun setSubtitlePreference(
        key: SubtitleKind,
        value: SubtitleKindPreference
    ) {
        setSubtitlePreferences(
            MediaSelectorSubtitlePreferences(getCurrentSubtitlePreferences().values.copyPut(key, value).toImmutable()),
        )
    }

    protected val selector = DefaultMediaSelector(
        mediaSelectorContextNotCached = mediaSelectorContext,
        mediaListNotCached = mediaList,
        savedUserPreference = savedUserPreference,
        savedDefaultPreference = savedDefaultPreference,
        enableCaching = false,
        mediaSelectorSettings = mediaSelectorSettings,
    )

    companion object {
        val DEFAULT_PREFERENCE = MediaPreference.Empty.copy(
            fallbackResolutions = listOf(
                Resolution.R2160P,
                Resolution.R1440P,
                Resolution.R1080P,
                Resolution.R720P,
            ).map { it.id },
            fallbackSubtitleLanguageIds = listOf(
                ChineseSimplified,
                ChineseTraditional,
            ).map { it.id },
        )

        const val SOURCE_DMHY = "dmhy"
        const val SOURCE_MIKAN = "mikan"

        @Suppress("SameParameterValue")
        fun createMediaSelectorContextFromEmpty(
            subjectCompleted: Boolean = false,
            mediaSourcePrecedence: List<String> = emptyList(),
            subtitleKindFilters: MediaSelectorSubtitlePreferences = MediaSelectorSubtitlePreferences.AllNormal
        ) =
            MediaSelectorContext(
                subjectFinished = subjectCompleted,
                mediaSourcePrecedence = mediaSourcePrecedence,
                subtitlePreferences = subtitleKindFilters,
            )
    }

    private var mediaId: Int = 0
    fun media(
        sourceId: String = SOURCE_DMHY,
        resolution: String = "1080P",
        alliance: String = "字幕组",
        size: FileSize = 1.megaBytes,
        publishedTime: Long = 0,
        subtitleLanguages: List<String> = listOf(ChineseSimplified, ChineseTraditional).map { it.id },
        location: MediaSourceLocation = MediaSourceLocation.Online,
        kind: MediaSourceKind = MediaSourceKind.BitTorrent,
        episodeRange: EpisodeRange = EpisodeRange.single(EpisodeSort(1)),
        subtitleKind: SubtitleKind? = null,
    ): DefaultMedia {
        val id = mediaId++
        return DefaultMedia(
            mediaId = "$sourceId.$id",
            mediaSourceId = sourceId,
            originalTitle = "[字幕组] 孤独摇滚 $id",
            download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:$id"),
            originalUrl = "https://example.com/$id",
            publishedTime = publishedTime,
            episodeRange = episodeRange,
            properties = MediaProperties(
                subtitleLanguageIds = subtitleLanguages,
                resolution = resolution,
                alliance = alliance,
                size = size,
                subtitleKind = subtitleKind,
            ),
            location = location,
            kind = kind,
        )
    }
}