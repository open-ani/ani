package me.him188.ani.app.data.models.episode

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

/**
 * 一个剧集及其收藏状态
 */
@Serializable
@Immutable
data class EpisodeCollection(
    val episodeInfo: EpisodeInfo,
    val collectionType: UnifiedCollectionType,
)

inline val EpisodeCollection.episode get() = episodeInfo
inline val EpisodeCollection.type get() = collectionType
