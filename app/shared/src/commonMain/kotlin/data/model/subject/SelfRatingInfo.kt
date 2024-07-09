package me.him188.ani.app.data.model.subject

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class SelfRatingInfo(
    /**
     * 0 表示未评分
     */
    val score: Int,
    /**
     * `null` 表示未评价
     */
    val comment: String?,
    val tags: List<String>,
    val isPrivate: Boolean,
) {
    companion object {
        @Stable
        val Empty = SelfRatingInfo(0, null, emptyList(), false)
    }
}
