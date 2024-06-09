package me.him188.ani.app.data.subject

import androidx.compose.ui.util.fastAll
import me.him188.ani.datasources.bangumi.processing.isOnAir
import org.openapitools.client.models.Episode
import kotlin.time.Duration.Companion.days

object EpisodeCollections {
    fun isSubjectCompleted(episodes: List<Episode>, now: PackedDate = PackedDate.now()): Boolean {
        val allEpisodesFinished = episodes.fastAll { it.isOnAir() == false }
        if (allEpisodesFinished) return true
        return isSubjectCompleted(episodes.asSequence().map { PackedDate.parseFromDate(it.airdate) }, now)
    }

    fun isSubjectCompleted(dates: Sequence<PackedDate>, now: PackedDate = PackedDate.now()): Boolean {
        val maxAirDate = dates
            .filter { it.isValid }
            .maxOrNull()

        return maxAirDate != null && now - maxAirDate >= 14.days
    }
}