package me.him188.ani.app.data.models.subject

import androidx.compose.runtime.Immutable
import me.him188.ani.app.data.models.UserInfo

@Immutable
data class SubjectComment(
    /**
     * This [id] is calculated by [creator], [content] and [updatedAt], not provided by Bangumi API.
     */
    val id: Int,
    val updatedAt: Int,
    val content: String,
    val creator: UserInfo?,
    val rating: Int
)