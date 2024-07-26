package me.him188.ani.app.data.models.subject

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.app.data.models.episode.EpisodeCollection
import me.him188.ani.app.data.models.episode.episode
import me.him188.ani.app.ui.subject.details.components.renderSubjectSeason
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

/**
 * 包含用户的观看进度的条目及其剧集信息
 */
@Immutable
@Serializable
data class SubjectCollection(
    // TODO: this is very shit, do not store them all together
    val info: SubjectInfo,
    val episodes: List<EpisodeCollection>, // must be sorted by sort
    val collectionType: UnifiedCollectionType,
    /**
     * 如果未收藏, 此属性为 `null`
     */
    val selfRatingInfo: SelfRatingInfo,
) {
    val displayName: String get() = info.displayName
    val subjectId: Int get() = info.id
    val date get() = renderSubjectSeason(info.airDate)

    override fun toString(): String = "SubjectCollectionItem($displayName)"

    @Transient
    val airingInfo: SubjectAiringInfo = SubjectAiringInfo.computeFromEpisodeList(
        episodes.map { it.episode },
        airDate = info.airDate,
    )

    companion object {
        val Empty = SubjectCollection(
            SubjectInfo.Empty,
            emptyList(),
            UnifiedCollectionType.NOT_COLLECTED,
            SelfRatingInfo.Empty,
        )
    }
}

/**
 * 是否已经开播了第一集
 */
val SubjectCollection.hasStarted get() = airingInfo.isOnAir || airingInfo.isCompleted
