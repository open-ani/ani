package me.him188.ani.danmaku.api

import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.EpisodeSort
import kotlin.time.Duration

/**
 * A [DanmakuProvider] provides a stream of danmaku for a specific episode.
 */
interface DanmakuProvider {
    /**
     * Matches a danmaku stream by the given filtering parameters.
     *
     * Returns `null` if not found.
     *
     * The returned [DanmakuSession] should be closed when it is no longer needed.
     */
    suspend fun startSession(
        request: DanmakuSearchRequest,
        matcher: DanmakuMatcher,
    ): DanmakuSession?
}

class DanmakuSearchRequest(
    val subjectId: Int,
    val subjectName: String,
    val episodeId: Int,
    val episodeSort: EpisodeSort,
    val episodeName: String,

    val filename: String,
    val fileHash: String?,
    val fileSize: Long,
    val videoDuration: Duration,
)

fun interface DanmakuMatcher {
    fun match(list: List<DanmakuEpisode>): DanmakuEpisode?
}

@Serializable
data class DanmakuEpisode(
    val id: String,
    val subjectName: String,
    val episodeName: String
)

object DanmakuMatchers {
    fun first() = DanmakuMatcher { it.firstOrNull() }

    fun mostRelevant(targetSubjectName: String, targetEpisodeName: String): DanmakuMatcher = DanmakuMatcher { list ->
        list.minByOrNull {
            levenshteinDistance(it.subjectName, targetSubjectName) + levenshteinDistance(
                it.episodeName,
                targetEpisodeName
            )
        }
    }

    // Thanks to ChatGPT :)
    // Helper function to calculate the Levenshtein distance between two strings
    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length

        var cost = Array(lhsLength + 1) { it }
        var newCost = Array(lhsLength + 1) { 0 }

        for (i in 1..rhsLength) {
            newCost[0] = i

            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = minOf(costReplace, costInsert, costDelete)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLength]
    }
}
