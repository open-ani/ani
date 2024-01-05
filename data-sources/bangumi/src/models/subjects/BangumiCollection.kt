package me.him188.animationgarden.datasources.bangumi.models.subjects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BangumiCollection(
    val wish: Int = 0,
    val collect: Int = 0,
    val doing: Int = 0,
    @SerialName("on_hold") val onHold: Int = 0,
    val dropped: Int = 0,
)