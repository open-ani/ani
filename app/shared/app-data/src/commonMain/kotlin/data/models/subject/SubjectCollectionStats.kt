package me.him188.ani.app.data.models.subject

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

/**
 * @see UnifiedCollectionType
 */
@Serializable
@Immutable
class SubjectCollectionStats(
    val wish: Int,
    val doing: Int,
    val done: Int,
    val onHold: Int,
    val dropped: Int,
) {
    val collect = wish + doing + done + onHold + dropped

    companion object {
        @Stable
        val Zero = SubjectCollectionStats(0, 0, 0, 0, 0)
    }
}