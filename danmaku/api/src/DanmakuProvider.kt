package me.him188.ani.danmaku.api

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.utils.ktor.createDefaultHttpClient
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import kotlin.time.Duration

/**
 * A [DanmakuProvider] provides a stream of danmaku for a specific episode.
 *
 * @see DanmakuProviderFactory
 */
interface DanmakuProvider : AutoCloseable {
    val id: String

    /**
     * Matches a danmaku stream by the given filtering parameters.
     *
     * Returns `null` if not found.
     *
     * The returned [DanmakuSession] should be closed when it is no longer needed.
     */
    suspend fun fetch(
        request: DanmakuSearchRequest,
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


abstract class AbstractDanmakuProvider(
    config: DanmakuProviderConfig,
) : DanmakuProvider {
    protected val logger = logger(this::class)

    protected val client = createDefaultHttpClient {
        applyDanmakuProviderConfig(config)
        Logging {
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    this@AbstractDanmakuProvider.logger.info { message }
                }
            }
            level = LogLevel.INFO
        }
        configureClient()
    }

    protected open fun HttpClientConfig<*>.configureClient() {}

    override fun close() {
        client.close()
    }
}
