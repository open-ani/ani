package me.him188.ani.app.data.model.subject

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.him188.ani.app.data.model.episode.EpisodeCollection
import me.him188.ani.app.data.model.episode.episode
import me.him188.ani.app.data.model.episode.isKnownCompleted
import me.him188.ani.app.data.model.episode.type
import me.him188.ani.app.ui.subject.collection.ContinueWatchingStatus
import me.him188.ani.app.ui.subject.details.components.renderSubjectSeason
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

/**
 * 包含用户的观看进度的条目及其剧集信息
 */
@Immutable
@Serializable
data class SubjectCollection(
    // TODO: this is shit, do not store them all together
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

    private val totalEps get() = episodes.size

    override fun toString(): String = "SubjectCollectionItem($displayName)"

    @Transient
    val airingInfo: SubjectAiringInfo = SubjectAiringInfo.computeFromEpisodeList(
        episodes.map { it.episode },
        airDate = info.airDate,
    )

    @Transient
    val lastWatchedEpIndex = kotlin.run {
        episodes.indexOfLast {
            it.type == UnifiedCollectionType.DONE || it.type == UnifiedCollectionType.DROPPED
        }.takeIf { it != -1 }
    }

    // TODO: remove from this 
    @Transient
    val continueWatchingStatus = kotlin.run {
        val latestEp = kotlin.run {
            episodes.lastOrNull { it.episode.isKnownCompleted }
        }

        val latestEpIndex: Int? = this.episodes.indexOfFirst { it.episode.id == latestEp?.episode?.id }
            .takeIf { it != -1 }
            ?: this.episodes.lastIndex.takeIf { it != -1 }

        when (this.lastWatchedEpIndex) {
            // 还没看过
            null -> {
                if (this.hasStarted) {
                    ContinueWatchingStatus.Start
                } else {
                    ContinueWatchingStatus.NotOnAir
                }
            }

            // 看了第 n 集并且还有第 n+1 集
            in 0..<this.totalEps - 1 -> {
                if (latestEpIndex != null && this.lastWatchedEpIndex < latestEpIndex) {
                    // 更新了 n+1 集
                    ContinueWatchingStatus.Continue(
                        this.lastWatchedEpIndex + 1,
                        this.episodes.getOrNull(this.lastWatchedEpIndex + 1)?.episode?.sort?.toString() ?: "",
                    )
                } else {
                    // 还没更新
                    ContinueWatchingStatus.Watched(
                        this.lastWatchedEpIndex,
                        this.episodes.getOrNull(this.lastWatchedEpIndex)?.episode?.sort?.toString() ?: "",
                    )
                }
            }

            else -> {
                ContinueWatchingStatus.Done
            }
        }
    }

}

/**
 * 是否已经开播了第一集
 */
val SubjectCollection.hasStarted get() = airingInfo.isOnAir || airingInfo.isCompleted

fun SubjectCollection.getEpisodeToPlay(): EpisodeCollection? {
    if (continueWatchingStatus is ContinueWatchingStatus.Watched) {
        return episodes[continueWatchingStatus.episodeIndex]
    } else {
        lastWatchedEpIndex?.let {
            episodes.getOrNull(it + 1)
        }?.let {
            return it
        }

        lastWatchedEpIndex?.let {
            episodes.getOrNull(it)
        }?.let {
            return it
        }

        episodes.firstOrNull()?.let {
            return it
        }
    }

    return null
}
