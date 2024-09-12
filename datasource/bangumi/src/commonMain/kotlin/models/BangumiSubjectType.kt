@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package me.him188.ani.datasources.bangumi.models


import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 条目类型 - `1` 为 书籍 - `2` 为 动画 - `3` 为 音乐 - `4` 为 游戏 - `6` 为 三次元  没有 `5`
 *
 * Values: Book,Anime,Music,Game,Real
 */
@Serializable(BangumiSubjectTypeSerializer::class)
enum class BangumiSubjectType(val value: kotlin.Int) {

    @SerialName(value = "1")
    Book(1),

    @SerialName(value = "2")
    Anime(2),

    @SerialName(value = "3")
    Music(3),

    @SerialName(value = "4")
    Game(4),

    @SerialName(value = "6")
    Real(6);

    override fun toString(): kotlin.String = value.toString()
}

object BangumiSubjectTypeSerializer : KSerializer<BangumiSubjectType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BangumiSubjectType", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: BangumiSubjectType) {
        encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): BangumiSubjectType {
        return when (val value = decoder.decodeInt()) {
            1 -> BangumiSubjectType.Book
            2 -> BangumiSubjectType.Anime
            3 -> BangumiSubjectType.Music
            4 -> BangumiSubjectType.Game
            6 -> BangumiSubjectType.Real
            else -> throw SerializationException("Unknown value $value for BangumiSubjectType")
        }
    }
}
