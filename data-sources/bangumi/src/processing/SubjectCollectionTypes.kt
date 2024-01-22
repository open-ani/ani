package me.him188.ani.datasources.bangumi.processing

import me.him188.ani.datasources.api.CollectionType
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.SubjectCollectionType


fun CollectionType.toSubjectCollectionType(): SubjectCollectionType? {
    return when (this) {
        CollectionType.Wish -> SubjectCollectionType.Wish
        CollectionType.Doing -> SubjectCollectionType.Doing
        CollectionType.Done -> SubjectCollectionType.Done
        CollectionType.OnHold -> SubjectCollectionType.OnHold
        CollectionType.Dropped -> SubjectCollectionType.Dropped
        CollectionType.NotCollected -> null
    }
}

fun SubjectCollectionType?.toCollectionType(): CollectionType {
    return when (this) {
        SubjectCollectionType.Wish -> CollectionType.Wish
        SubjectCollectionType.Doing -> CollectionType.Doing
        SubjectCollectionType.Done -> CollectionType.Done
        SubjectCollectionType.OnHold -> CollectionType.OnHold
        SubjectCollectionType.Dropped -> CollectionType.Dropped
        null -> CollectionType.NotCollected
    }
}

fun EpisodeCollectionType.toCollectionType(): CollectionType {
    return when (this) {
        EpisodeCollectionType.NOT_COLLECTED -> CollectionType.NotCollected
        EpisodeCollectionType.WATCHLIST -> CollectionType.Wish
        EpisodeCollectionType.WATCHED -> CollectionType.Done
        EpisodeCollectionType.DISCARDED -> CollectionType.Dropped
    }
}

fun CollectionType.toEpisodeCollectionType(): EpisodeCollectionType {
    return when (this) {
        CollectionType.NotCollected -> EpisodeCollectionType.NOT_COLLECTED
        CollectionType.Wish -> EpisodeCollectionType.WATCHLIST
        CollectionType.Doing -> EpisodeCollectionType.WATCHLIST
        CollectionType.Done -> EpisodeCollectionType.WATCHED
        CollectionType.OnHold -> EpisodeCollectionType.WATCHLIST
        CollectionType.Dropped -> EpisodeCollectionType.DISCARDED
    }
}