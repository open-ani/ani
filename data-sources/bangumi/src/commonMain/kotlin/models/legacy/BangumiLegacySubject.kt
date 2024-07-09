package me.him188.ani.datasources.bangumi.models.subjects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.him188.ani.datasources.bangumi.BangumiRating
import me.him188.ani.datasources.bangumi.models.BangumiCollection
import me.him188.ani.datasources.bangumi.models.BangumiSubjectType

@Serializable
data class BangumiLegacySubject(
    val id: Int,
    val type: BangumiSubjectType,
    @SerialName("name") val originalName: String, // 日文
    @SerialName("name_cn") val chineseName: String = originalName, // 中文
    val summary: String, // can be very long
    @SerialName("air_date") val airDate: String = "", // "2002-04-02"
    @SerialName("air_weekday") val airWeekday: Int = 0,
//    @SerialName("images") val images: BangumiSubjectImages? = null,
    val eps: Int = 1, // 话数
    @SerialName("eps_count") val epsCount: Int = 1, // 话数
    val rating: BangumiRating? = null,
    val collection: BangumiCollection? = null,

    // small fields
//    val tags: List<BangumiSubjectTag>? = null,

    // medium fields
    val url: String? = "",       // 条目地址
    val rank: Int? = 0,         // 排名
    val crt: String? = "",       // 角色信息
    val staff: String? = "",     // 制作人员信息

    // large fields
    val topic: String? = "",     // 讨论版
    val blog: String? = "",      // 评论日志
)