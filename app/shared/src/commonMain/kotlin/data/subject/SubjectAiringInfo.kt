package me.him188.ani.app.data.subject

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import me.him188.ani.datasources.api.EpisodeSort

@Immutable
data class SubjectAiringInfo(
    val kind: SubjectAiringKind,
    /**
     * 总集数
     */
    val episodeCount: Int,
    /**
     * 首播日期
     */
    val airDate: PackedDate,
    /**
     * 不考虑连载情况的第一集序号, 只有当这个条目没有任何剧集时为 `null`.
     */
    val firstSort: EpisodeSort?,
    /**
     * 连载至的最新一集序号. 当还未开播时为 `null`, 当已经完结时为最后一集序号.
     */
    val latestSort: EpisodeSort?,
    /**
     * 即将要播出的序号. 当还未开播时为第一集的序号, 当已经完结时为 `null`.
     */
    val upcomingSort: EpisodeSort?,
) {
    companion object {
        @Stable
        val EmptyCompleted = SubjectAiringInfo(
            SubjectAiringKind.COMPLETED,
            episodeCount = 0,
            airDate = PackedDate.Invalid,
            firstSort = null,
            latestSort = null,
            upcomingSort = null,
        )

        fun computeFromEpisodeList(
            list: List<EpisodeInfo>,
            airDate: PackedDate,
        ): SubjectAiringInfo {
            return SubjectAiringInfo(
                kind = when {
                    list.isEmpty() -> SubjectAiringKind.UPCOMING
                    list.all { it.isKnownCompleted } -> SubjectAiringKind.COMPLETED
                    list.all { it.isKnownOnAir } -> SubjectAiringKind.UPCOMING
                    else -> SubjectAiringKind.ON_AIR
                },
                episodeCount = list.size,
                airDate = airDate.ifInvalid { list.firstOrNull()?.airDate ?: PackedDate.Invalid },
                firstSort = list.firstOrNull()?.sort,
                latestSort = list.lastOrNull { it.isKnownCompleted }?.sort,
                upcomingSort = list.firstOrNull { it.isKnownOnAir }?.sort,
            )
        }
    }
}

@Stable
val SubjectAiringInfo.isOnAir: Boolean
    get() = kind == SubjectAiringKind.ON_AIR

@Stable
val SubjectAiringInfo.isCompleted: Boolean
    get() = kind == SubjectAiringKind.COMPLETED

@Stable
val SubjectAiringInfo.isUpcoming: Boolean
    get() = kind == SubjectAiringKind.UPCOMING

@Immutable
enum class SubjectAiringKind {
    /**
     * 即将开播 (已经播出了第一集, 但还未播出最后一集)
     */
    UPCOMING,

    /**
     * 正在播出 (第一集还未开播, 将在未来开播)
     */
    ON_AIR,

    /**
     * 已完结 (最后一集已经播出)
     */
    COMPLETED,
}
