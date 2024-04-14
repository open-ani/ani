package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.topic.SubtitleLanguage
import me.him188.ani.datasources.mikan.MikanMediaSource

@Immutable
@Serializable
data class MediaPreference(
    /**
     * 精确匹配字幕组
     */
    val alliance: String? = null,
    /**
     * 若精确匹配失败, 则使用正则表达式匹配, 将会选择首个匹配
     */
    val alliancePatterns: List<String>? = null,

    val resolution: String? = null,

    /**
     * 优先使用的字幕语言
     */
    val subtitleLanguageId: String? = null,
    /**
     * 在线播放时, 若 [subtitleLanguageId] 未匹配, 则按照此列表的顺序选择字幕语言.
     * 缓存时则只会缓存此列表中的字幕语言.
     *
     * 为 `null` 表示任意.
     */
    val fallbackSubtitleLanguageIds: List<String>? = listOf(
        SubtitleLanguage.ChineseSimplified,
        SubtitleLanguage.ChineseTraditional,
    ).map { it.id },

    /**
     * 优先使用的媒体源
     */
    val mediaSourceId: String? = MikanMediaSource.ID,
    /**
     * @see fallbackSubtitleLanguageIds
     */
    val fallbackMediaSourceIds: List<String>? = null,
) {
    companion object {
        val Empty = MediaPreference()
    }

    fun merge(other: MediaPreference): MediaPreference {
        if (other == Empty) return this
        if (this == Empty) return other
        return MediaPreference(
            alliance = other.alliance ?: alliance,
            resolution = other.resolution ?: resolution,
            subtitleLanguageId = other.subtitleLanguageId ?: subtitleLanguageId,
            mediaSourceId = other.mediaSourceId ?: mediaSourceId,
        )
    }
}