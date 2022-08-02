package me.him188.animationgarden.api.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Topic(
    val id: String,
    val date: LocalDateTime,
    val category: TopicCategory,
    val alliance: Alliance?,
    val title: String,
    val commentsCount: Int,
    val magnetLink: MagnetLink,
    val size: FileSize,
    val author: User
) {
    override fun toString(): String {
        return "$id " +
                "| ${date.format(DATE_FORMAT)} " +
                "| ${category.name} " +
                "| ${alliance?.name} " +
                "| $title " +
                "| $commentsCount " +
                "| ${magnetLink.value.truncated(10)} " +
                "| $size " +
                "| $author"
    }
}

@JvmInline
value class MagnetLink(
    val value: String
)

class TopicCategory(
    val id: String,
    val name: String
)

class Alliance(
    val id: String,
    val name: String
)

private fun String.truncated(length: Int, truncated: String = "..."): String {
    return if (this.length > length) {
        this.take(length) + truncated
    } else {
        this
    }
}

val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
