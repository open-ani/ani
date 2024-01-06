package me.him188.animationgarden.datasources.bangumi.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.him188.animationgarden.datasources.api.Paged

interface BangumiClientEpisodes {
    suspend fun getEpisodes(
        subjectId: Long,
        type: BangumiEpType, // 不能请求 MAD
        limit: Int? = null,
        offset: Int? = null,
    ): Paged<BangumiEpisode>
}

/*
BangumiEpisode(id=1227087, type=MAIN, originalName=冒険の終わり, chineseName=冒险结束, sort=1, ep=1, 
airdate=2023-09-29, comment=268, duration=00:26:00, desc=魔王を倒し王都へ凱旋した勇者ヒンメル一行。
各々が冒険した10年を振り返りながらこれからの人生に想いを馳せる中、エルフのフリーレンは感慨にふけることもなく、
また魔法探求へと旅立っていく。50年後、皆との約束のためフリーレンは再び王都へ。その再会をきっかけに、
彼女は新たな旅へと向かうことに―。
 */
@Serializable
data class BangumiEpisode(
    val id: Long,
    val type: BangumiEpType,
    @SerialName("name") val originalName: String,
    @SerialName("name_cn") val chineseName: String,
    /**
     * 同类条目的排序和集数
     */
    val sort: Int,
    /**
     * 条目内的集数, 从`1`开始。非本篇剧集的此字段无意义
     */
    val ep: Int? = null,
    val airdate: String, // "2023-09-29"
    val comment: Int,
    val duration: String,
    val desc: String,
    val disc: Int,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
)

@Serializable(with = BangumiEpType.AsIntSerializer::class)
enum class BangumiEpType(
    val id: Int,
) {
    /**
     * 正片
     */
    MAIN(0),

    /**
     * 特别篇
     */
    SP(1),

    OP(2),
    ED(3),

    /**
     * 预告片
     */
    PV(4),

    MAD(5),
    OTHER(6),
    ;

    internal object AsIntSerializer : KSerializer<BangumiEpType> {
        override val descriptor: SerialDescriptor = Int.serializer().descriptor

        override fun deserialize(decoder: Decoder): BangumiEpType {
            val raw = Int.serializer().deserialize(decoder)
            return entries.firstOrNull { it.id == raw }
                ?: throw IllegalStateException("Unknown episode type: $raw")
        }

        override fun serialize(encoder: Encoder, value: BangumiEpType) {
            return Int.serializer().serialize(encoder, value.id)
        }
    }
}

/*
subject_id
required
integer (Subject ID) >= 1
条目 ID

type	
integer (EpType)
参照章节的type

Enum: 0 1 2 3 4 5 6
limit	
integer (Limit) [ 1 .. 200 ]
Default: 100
分页参数

offset	
integer (Offset) >= 0
Default: 0
分页参数
 */