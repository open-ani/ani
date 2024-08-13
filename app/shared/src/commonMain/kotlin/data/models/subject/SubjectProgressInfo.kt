package me.him188.ani.app.data.models.subject

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.data.models.episode.episode
import me.him188.ani.app.data.models.episode.isKnownCompleted
import me.him188.ani.app.data.models.episode.type
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

@Immutable
data class SubjectProgressInfo(
    val continueWatchingStatus: ContinueWatchingStatus?,
    val nextEpisodeIdToPlay: Int?,
) {
    /**
     * 仅供 [calculate]
     */
    class Episode(
        val id: Int,
        val type: UnifiedCollectionType,
        val sort: EpisodeSort,
        /**
         * 是否一定已经播出了
         * @see EpisodeInfo.isKnownCompleted
         */
        val isKnownCompleted: Boolean,
    )

    companion object {
        @Stable
        val Empty = SubjectProgressInfo(
            null,
            null,
        )

        fun calculate(
            collection: SubjectCollection,
        ): SubjectProgressInfo {
            return calculate(
                collection.hasStarted,
                collection.episodes.map {
                    Episode(
                        it.episode.id,
                        it.type,
                        it.episode.sort,
                        it.episode.isKnownCompleted,
                    )
                },
            )
        }

        fun calculate(
            subjectStarted: Boolean,
            episodes: List<Episode>,
        ): SubjectProgressInfo {
            val lastWatchedEpIndex = episodes.indexOfLast {
                it.type == UnifiedCollectionType.DONE || it.type == UnifiedCollectionType.DROPPED
            }

            val continueWatchingStatus = kotlin.run {
                val latestEp = kotlin.run {
                    episodes.lastOrNull { it.isKnownCompleted }
                }

                val latestEpIndex: Int? =
                    episodes.indexOfFirst { it == latestEp }
                        .takeIf { it != -1 }
                        ?: episodes.lastIndex.takeIf { it != -1 }

                when (lastWatchedEpIndex) {
                    // 还没看过
                    -1 -> {
                        if (subjectStarted) {
                            ContinueWatchingStatus.Start
                        } else {
                            ContinueWatchingStatus.NotOnAir
                        }
                    }

                    // 看了第 n 集并且还有第 n+1 集
                    in 0..<episodes.size - 1 -> {
                        if (latestEpIndex != null && lastWatchedEpIndex < latestEpIndex) {
                            // 更新了 n+1 集
                            ContinueWatchingStatus.Continue(
                                lastWatchedEpIndex + 1,
                                episodes.getOrNull(lastWatchedEpIndex + 1)?.sort,
                            )
                        } else {
                            // 还没更新
                            ContinueWatchingStatus.Watched(
                                lastWatchedEpIndex,
                                episodes.getOrNull(lastWatchedEpIndex)?.sort,
                            )
                        }
                    }

                    else -> {
                        ContinueWatchingStatus.Done
                    }
                }
            }

            val episodeToPlay = kotlin.run {
                if (continueWatchingStatus is ContinueWatchingStatus.Watched) {
                    return@run episodes[continueWatchingStatus.episodeIndex]
                } else {
                    if (lastWatchedEpIndex != -1) {
                        episodes.getOrNull(lastWatchedEpIndex + 1)?.let { return@run it }
                        episodes.getOrNull(lastWatchedEpIndex)?.let { return@run it }
                    }

                    episodes.firstOrNull()?.let {
                        return@run it
                    }
                }

                null
            }

            return SubjectProgressInfo(
                continueWatchingStatus,
                episodeToPlay?.id,
            )
        }
    }
}

sealed class ContinueWatchingStatus {
    data object Start : ContinueWatchingStatus()

    /**
     * 还未开播
     */
    data object NotOnAir : ContinueWatchingStatus()

    /**
     * 继续看
     */
    data class Continue(
        val episodeIndex: Int,
        val episodeSort: EpisodeSort?, // "12.5"
    ) : ContinueWatchingStatus()

    /**
     * 看到了, 但是下一集还没更新
     */
    data class Watched(
        val episodeIndex: Int,
        val episodeSort: EpisodeSort?, // "12.5"
    ) : ContinueWatchingStatus()

    data object Done : ContinueWatchingStatus()
}
