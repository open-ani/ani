package me.him188.ani.app.data.subject

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class RatingInfo(
    val rank: Int,
    val total: Int,
    val count: RatingCounts,
    val score: String, // big decimal
) {
    val scoreFloat get() = score.toFloat()

    companion object {
        @Stable
        val Empty = RatingInfo(0, 0, RatingCounts.Zero, "0")
    }
}

@Serializable
@Immutable
class RatingCounts(
    private val values: IntArray,
) {
    init {
        require(values.size == 10) {
            "values must have 10 elements"
        }
    }

    fun get(score: Int): Int {
        require(score in 1..10) {
            "Score must be between 0 and 10"
        }
        return values[score]
    }

    companion object {
        @Stable
        val Zero = RatingCounts(IntArray(10) { 0 })
    }
}
