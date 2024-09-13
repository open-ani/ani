package me.him188.ani.app.data.models.episode

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.him188.ani.app.data.source.media.cache.EpisodeCacheStatus
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

/**
 * Describes the progress of an episode (of a subject)
 */
@Stable
class EpisodeProgressItem(
    val episodeId: Int,
    val episodeSort: String,
    val collectionType: UnifiedCollectionType,
    val isOnAir: Boolean?,
    val cacheStatus: EpisodeCacheStatus?,
) {
    var isLoading by mutableStateOf(false)
}
