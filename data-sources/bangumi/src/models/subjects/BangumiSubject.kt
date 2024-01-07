package me.him188.ani.datasources.bangumi.models.subjects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BangumiSubject(
    val id: Int,
    val type: BangumiSubjectType,
    @SerialName("date") val airDate: String, // "2002-04-02"
    @SerialName("image") val image: String, // cover
    val summary: String,
    val name: String, // 日文
    @SerialName("name_cn") val nameCN: String, // 中文
    val tags: List<BangumiSubjectTag>,
    val score: Double,
    val rank: Int,
)