package me.him188.ani.datasources.bangumi.models.subjects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BangumiSubjectImageSize(
    val id: String,
) {
    @SerialName("small")
    SMALL("small"),

    @SerialName("medium")
    MEDIUM("medium"),

    @SerialName("large")
    LARGE("large"),

    @SerialName("grid")
    GRID("grid"),

    @SerialName("common")
    COMMON("grid"),
}