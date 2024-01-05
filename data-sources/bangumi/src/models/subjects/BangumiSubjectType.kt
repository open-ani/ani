package me.him188.animationgarden.datasources.bangumi.models.subjects

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// Legacy_SubjectType
@Serializable(BangumiSubjectType.AsIntSerializer::class)
enum class BangumiSubjectType(val id: Int) {
    BOOK(1),
    ANIME(2),
    MUSIC(3),
    GAME(4),
    REAL(6),
    ;

    internal object AsIntSerializer : KSerializer<BangumiSubjectType> {
        override val descriptor: SerialDescriptor = Int.serializer().descriptor

        override fun deserialize(decoder: Decoder): BangumiSubjectType {
            val raw = Int.serializer().deserialize(decoder)
            return entries.firstOrNull { it.id == raw }
                ?: throw IllegalStateException("Unknown BangumiSubjectType: $raw")
        }

        override fun serialize(encoder: Encoder, value: BangumiSubjectType) {
            return Int.serializer().serialize(encoder, value.id)
        }
    }
}