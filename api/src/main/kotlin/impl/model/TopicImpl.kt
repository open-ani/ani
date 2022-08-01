package me.him188.animationgarden.api.impl.model

import me.him188.animationgarden.api.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class TopicImpl(
    override val id: String,
    override val date: LocalDateTime,
    override val category: TopicCategory,
    override val alliance: Alliance?,
    override val title: String,
    override val commentsCount: Int,
    override val magnetLink: MagnetLink,
    override val size: FileSize,
    override val author: User
) : Topic {
    override fun toString(): String {
        return "$id " +
                "| ${date.format(formatter)} " +
                "| ${category.name} " +
                "| ${alliance?.name} " +
                "| $title " +
                "| $commentsCount " +
                "| ${magnetLink.value.truncated(10)} " +
                "| $size " +
                "| $author"
    }
}

private fun String.truncated(length: Int, truncated: String = "..."): String {
    return if (this.length > length) {
        this.take(length) + truncated
    } else {
        this
    }
}

private val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
