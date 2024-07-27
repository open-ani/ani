package me.him188.ani.app.data.models.episode

import me.him188.ani.app.data.source.media.EpisodeCacheStatus
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

class EpisodeProgressInfo(
    val episode: EpisodeInfo,
    val collectionType: UnifiedCollectionType,
    val cacheStatus: EpisodeCacheStatus,
)
