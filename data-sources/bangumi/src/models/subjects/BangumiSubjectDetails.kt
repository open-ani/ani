package me.him188.ani.datasources.bangumi.models.subjects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.bangumi.BangumiRating

@Serializable
data class BangumiSubjectDetails(
    val id: Long,
    val type: BangumiSubjectType,
    @SerialName("date") val airDate: String, // "2002-04-02"
    @SerialName("images") val images: BangumiSubjectImages,
    val summary: String, // can be very long
    @SerialName("name") val originalName: String, // 日文
    @SerialName("name_cn") val chineseName: String, // 中文
    val tags: List<BangumiSubjectTag>,

    val nsfw: Boolean = false,
    val locked: Boolean,
    val platform: String = "",
    val infobox: List<BangumiSubjectInfo>,
    val volumes: Int = 0,
    val eps: Int = 1, // 话数
    @SerialName("total_episodes") val totalEpisodes: Int,
    val rating: BangumiRating,
    val collection: BangumiCollection,
)