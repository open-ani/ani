package me.him188.animationgarden.api.model

import java.time.LocalDateTime

public interface Topic {
    public val id: String
    public val date: LocalDateTime
    public val category: TopicCategory
    public val alliance: Alliance?
    public val title: String
    public val commentsCount: Int
    public val magnetLink: MagnetLink
    public val size: FileSize
    public val author: User
}


@JvmInline
public value class MagnetLink(
    public val value: String
)

public interface TopicCategory {
    public val id: String
    public val name: String
}

public interface Alliance {
    public val id: String
    public val name: String
}
