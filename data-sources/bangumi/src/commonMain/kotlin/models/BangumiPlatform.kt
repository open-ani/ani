package me.him188.ani.datasources.bangumi.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class BangumiPlatform(
    val id: Int,
    val type: String = "",
    @JsonNames("type_cn", "typeCn", "typeCN")
    val typeCn: String = "",
    val alias: String = "",
)
