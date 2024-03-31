package me.him188.ani.app.ui.subject.episode.comments

class Comment(
    val id: String,
    val type: Int,
    val summary: String,
    val createdAt: Long, // timestamp millis
    val authorUsername: String?,
)