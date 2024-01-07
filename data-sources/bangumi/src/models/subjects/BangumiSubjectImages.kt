package me.him188.ani.datasources.bangumi.models.subjects

import kotlinx.serialization.Serializable

@Serializable
data class BangumiSubjectImages(
    val large: String = "",
    val common: String = "",
    val medium: String = "",
    val small: String = "",
    val grid: String = "",
)