package me.him188.ani.app.data.models.episode

import androidx.compose.ui.util.fastAll
import me.him188.ani.app.data.models.PackedDate
import me.him188.ani.app.data.models.minus
import kotlin.time.Duration.Companion.days

object EpisodeCollections {
    fun isSubjectCompleted(episodes: List<EpisodeInfo>, now: PackedDate = PackedDate.now()): Boolean {
        val allEpisodesFinished = episodes.fastAll { it.isKnownCompleted }
        if (!allEpisodesFinished) return false // 如果无法肯定已经完结, 则认为未完结
        return isSubjectCompleted(episodes.asSequence().map { it.airDate }, now)
    }

    fun isSubjectCompleted(dates: Sequence<PackedDate>, now: PackedDate = PackedDate.now()): Boolean {
        val maxAirDate = dates
            .filter { it.isValid }
            .maxOrNull()

        return maxAirDate != null && now - maxAirDate >= 14.days
    }
}