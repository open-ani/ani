package me.him188.ani.datasources.bangumi.models.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BangumiSort(
    val id: String,
) {
    /**
     * 按照匹配程度
     */
    @SerialName("match")
    MATCH("match"),

    /**
     * 收藏人数
     */
    @SerialName("heat")
    HEAT("heat"),

    @SerialName("rank")
    RANK("rank"),

    @SerialName("score")
    SCORE("score"),
    ;
}