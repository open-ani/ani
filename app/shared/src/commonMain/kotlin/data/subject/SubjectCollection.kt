package me.him188.ani.app.data.subject

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
) {
    val displayName: String get() = info.displayName
    val subjectId: Int get() = info.id
    val image: String get() = info.imageCommon
    val date get() = renderSubjectSeason(info.publishDate)

    private val totalEps get() = episodes.size

    override fun toString(): String = "SubjectCollectionItem($displayName)"

    @Transient
    val isOnAir = kotlin.run {
        episodes.firstOrNull { !it.episode.isKnownBroadcast } != null
    }

    @Transient
    val lastWatchedEpIndex = kotlin.run {
        episodes.indexOfLast {
            it.type == UnifiedCollectionType.DONE || it.type == UnifiedCollectionType.DROPPED
        }.takeIf { it != -1 }
    }

    @Transient
    val latestEp = kotlin.run {
        episodes.lastOrNull { it.episode.isKnownBroadcast }
    }

    /**
     * 是否已经开播了第一集
     */
    @Transient
    val hasStarted = this.episodes.any { it.episode.isKnownBroadcast }

    @Transient
    val latestEpIndex: Int? = this.episodes.indexOfFirst { it.episode.id == latestEp?.episode?.id }
        .takeIf { it != -1 }
        ?: this.episodes.lastIndex.takeIf { it != -1 }

    @Transient
    val onAirDescription = if (isOnAir) {
        if (latestEp == null) {
            "未开播"
        } else {
            "连载至第 ${latestEp.episode.sort} 话"
        }
    } else {
        "已完结"
    }

    // TODO: remove this ui element 
    @Transient
    val serialProgress = "全 ${this.totalEps} 话"

    // TODO: remove from this 
    @Transient
    val continueWatchingStatus = kotlin.run {
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
                if (this.latestEpIndex != null && this.lastWatchedEpIndex < this.latestEpIndex) {
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