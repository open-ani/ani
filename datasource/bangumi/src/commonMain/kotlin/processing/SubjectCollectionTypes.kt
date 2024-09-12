package me.him188.ani.datasources.bangumi.processing

import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.bangumi.models.BangumiEpisodeCollectionType
import me.him188.ani.datasources.bangumi.models.BangumiSubjectCollectionType


fun UnifiedCollectionType.toSubjectCollectionType(): BangumiSubjectCollectionType? {
    return when (this) {
        UnifiedCollectionType.WISH -> BangumiSubjectCollectionType.Wish
        UnifiedCollectionType.DOING -> BangumiSubjectCollectionType.Doing
        UnifiedCollectionType.DONE -> BangumiSubjectCollectionType.Done
        UnifiedCollectionType.ON_HOLD -> BangumiSubjectCollectionType.OnHold
        UnifiedCollectionType.DROPPED -> BangumiSubjectCollectionType.Dropped
        UnifiedCollectionType.NOT_COLLECTED -> null
    }
}

fun BangumiSubjectCollectionType?.toCollectionType(): UnifiedCollectionType {
    return when (this) {
        BangumiSubjectCollectionType.Wish -> UnifiedCollectionType.WISH
        BangumiSubjectCollectionType.Doing -> UnifiedCollectionType.DOING
        BangumiSubjectCollectionType.Done -> UnifiedCollectionType.DONE
        BangumiSubjectCollectionType.OnHold -> UnifiedCollectionType.ON_HOLD
        BangumiSubjectCollectionType.Dropped -> UnifiedCollectionType.DROPPED
        null -> UnifiedCollectionType.NOT_COLLECTED
    }
}

fun BangumiEpisodeCollectionType.toCollectionType(): UnifiedCollectionType {
    return when (this) {
        BangumiEpisodeCollectionType.NOT_COLLECTED -> UnifiedCollectionType.NOT_COLLECTED
        BangumiEpisodeCollectionType.WATCHLIST -> UnifiedCollectionType.WISH
        BangumiEpisodeCollectionType.WATCHED -> UnifiedCollectionType.DONE
        BangumiEpisodeCollectionType.DISCARDED -> UnifiedCollectionType.DROPPED
    }
}

fun UnifiedCollectionType.toEpisodeCollectionType(): BangumiEpisodeCollectionType {
    return when (this) {
        UnifiedCollectionType.NOT_COLLECTED -> BangumiEpisodeCollectionType.NOT_COLLECTED
        UnifiedCollectionType.WISH -> BangumiEpisodeCollectionType.WATCHLIST
        UnifiedCollectionType.DOING -> BangumiEpisodeCollectionType.WATCHLIST
        UnifiedCollectionType.DONE -> BangumiEpisodeCollectionType.WATCHED
        UnifiedCollectionType.ON_HOLD -> BangumiEpisodeCollectionType.WATCHLIST
        UnifiedCollectionType.DROPPED -> BangumiEpisodeCollectionType.DISCARDED
    }
}