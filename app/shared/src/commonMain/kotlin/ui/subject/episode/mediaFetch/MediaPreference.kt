package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.app.data.models.preference.MediaSelectorSettings
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.SubtitleLanguage
import me.him188.ani.utils.platform.annotations.SerializationOnly

/**
 * @see MediaSelectorSettings
 */
@Immutable
@Serializable
data class MediaPreference
@SerializationOnly
constructor(
    /**
     * 精确匹配字幕组
     */
    val alliance: String? = null,
    /**
     * 若精确匹配失败, 则使用正则表达式匹配, 将会选择首个匹配
     */
    val alliancePatterns: List<String>? = null,

    val resolution: String? = null,
    val fallbackResolutions: List<String>? = listOf(
        Resolution.R2160P,
        Resolution.R1440P,
        Resolution.R1080P,
        Resolution.R720P,
    ).map { it.id },

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
     * 是否显示没有解析到字幕的资源, 这可能是本身是生肉, 也可能是字幕未匹配到. 是生肉的可能性更高.
     */
    val showWithoutSubtitle: Boolean = false,

    /**
     * 优先使用的媒体源
     */
    val mediaSourceId: String? = null,
    @Deprecated("Only for migration") // since 3.1.0-beta03
    val fallbackMediaSourceIds: List<String>? = null,
    @Suppress("PropertyName") @Transient val _placeholder: Int = 0,
) {
    @OptIn(SerializationOnly::class)
    companion object {
        /**
         * With default values
         * @see Empty
         */
        val PlatformDefault = MediaPreference()

        /**
         * No preference
         */
        val Empty = MediaPreference(
            mediaSourceId = null,
            fallbackSubtitleLanguageIds = null,
            fallbackResolutions = null,
        )
    }

    fun merge(other: MediaPreference): MediaPreference {
        if (other == Empty) return this
        if (this == Empty) return other
        @OptIn(SerializationOnly::class)
        return MediaPreference(
            alliance = other.alliance ?: alliance,
            alliancePatterns = other.alliancePatterns ?: alliancePatterns,
            resolution = other.resolution ?: resolution,
            subtitleLanguageId = other.subtitleLanguageId ?: subtitleLanguageId,
            fallbackSubtitleLanguageIds = other.fallbackSubtitleLanguageIds ?: fallbackSubtitleLanguageIds,
            mediaSourceId = other.mediaSourceId ?: mediaSourceId,
            fallbackResolutions = other.fallbackResolutions ?: fallbackResolutions,
        )
    }
}
