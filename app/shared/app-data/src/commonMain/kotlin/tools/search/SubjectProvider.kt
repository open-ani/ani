/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.tools.search

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.utils.serialization.BigNum

/**
 * 提供番剧名称索引的数据源. 支持使用关键字搜索正式名称.
 */
interface SubjectProvider {
    /**
     * Unique ID. Can be name of the provider.
     */
    val id: String

    suspend fun testConnection(): ConnectionStatus

    fun startSearch(query: SubjectSearchQuery): PagedSource<SubjectInfo>
//
//    suspend fun getSubjectDetails(id: String): SubjectDetails?
}

data class Subject(
    val id: Int,
    /**
     * 条目官方原名称, 例如番剧为日文名称
     */
    val originalName: String,
    /**
     * 条目中文名称
     */
    val chineseName: String,
    val score: BigNum,
    val rank: Int,
    val tags: List<Pair<String, Int>>,
    val sourceUrl: String, // 数据源
    val images: SubjectImages,
    val summary: String,
)

@Serializable(with = RatingScore.AsStringSerializer::class)
enum class RatingScore(
    val id: String,
) {
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    TEN("10"),
    ;

    internal object AsStringSerializer : KSerializer<RatingScore> {
        override val descriptor: SerialDescriptor = String.serializer().descriptor

        override fun deserialize(decoder: Decoder): RatingScore {
            val raw = String.serializer().deserialize(decoder)
            return entries.firstOrNull { it.id == raw }
                ?: throw IllegalStateException("Unknown rating: $raw")
        }

        override fun serialize(encoder: Encoder, value: RatingScore) {
            return String.serializer().serialize(encoder, value.id)
        }
    }
}


@Serializable
data class SubjectImages(
    val landscapeCommon: String,
    val largePoster: String,
)

class SubjectSearchQuery(
    val keyword: String,
    val type: SubjectType = SubjectType.ANIME,
    val useOldSearchApi: Boolean = true,
    val tags: List<String> = listOf(),
    val airDate: Pair<String?, String?> = Pair(null, null),
    val rating: Pair<String?, String?> = Pair(null, null),
    val rank: Pair<String?, String?> = Pair(null, null),
    val nsfw: Boolean? = null,
)

enum class SubjectType {
    ANIME,

    /*
    bangumi supports
            条目类型
            - `1` 为 书籍
            - `2` 为 动画
            - `3` 为 音乐
            - `4` 为 游戏
            - `6` 为 三次元
            
            没有 `5`
     */
}
