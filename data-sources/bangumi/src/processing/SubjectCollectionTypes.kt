package me.him188.ani.datasources.bangumi.processing

import me.him188.ani.datasources.api.UnifiedCollectionType
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.SubjectCollectionType


fun UnifiedCollectionType.toSubjectCollectionType(): SubjectCollectionType? {
    return when (this) {
        UnifiedCollectionType.WISH -> SubjectCollectionType.Wish
        UnifiedCollectionType.DOING -> SubjectCollectionType.Doing
        UnifiedCollectionType.DONE -> SubjectCollectionType.Done
        UnifiedCollectionType.ON_HOLD -> SubjectCollectionType.OnHold
        UnifiedCollectionType.DROPPED -> SubjectCollectionType.Dropped
        UnifiedCollectionType.NOT_COLLECTED -> null
    }
}

fun SubjectCollectionType?.toCollectionType(): UnifiedCollectionType {
    return when (this) {
        SubjectCollectionType.Wish -> UnifiedCollectionType.WISH
        SubjectCollectionType.Doing -> UnifiedCollectionType.DOING
        SubjectCollectionType.Done -> UnifiedCollectionType.DONE
        SubjectCollectionType.OnHold -> UnifiedCollectionType.ON_HOLD
        SubjectCollectionType.Dropped -> UnifiedCollectionType.DROPPED
        null -> UnifiedCollectionType.NOT_COLLECTED
    }
}

fun EpisodeCollectionType.toCollectionType(): UnifiedCollectionType {
    return when (this) {
        EpisodeCollectionType.NOT_COLLECTED -> UnifiedCollectionType.NOT_COLLECTED
        EpisodeCollectionType.WATCHLIST -> UnifiedCollectionType.WISH
        EpisodeCollectionType.WATCHED -> UnifiedCollectionType.DONE
        EpisodeCollectionType.DISCARDED -> UnifiedCollectionType.DROPPED
    }
}

fun UnifiedCollectionType.toEpisodeCollectionType(): EpisodeCollectionType {
    return when (this) {
        UnifiedCollectionType.NOT_COLLECTED -> EpisodeCollectionType.NOT_COLLECTED
        UnifiedCollectionType.WISH -> EpisodeCollectionType.WATCHLIST
        UnifiedCollectionType.DOING -> EpisodeCollectionType.WATCHLIST
        UnifiedCollectionType.DONE -> EpisodeCollectionType.WATCHED
        UnifiedCollectionType.ON_HOLD -> EpisodeCollectionType.WATCHLIST
        UnifiedCollectionType.DROPPED -> EpisodeCollectionType.DISCARDED
    }
}