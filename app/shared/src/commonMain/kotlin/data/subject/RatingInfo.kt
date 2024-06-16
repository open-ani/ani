package me.him188.ani.app.data.subject

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class RatingInfo(
    val rank: Int,
    val total: Int,
    val count: Map<Int, Int>, // key is `1..10`
    val score: Float,
)
