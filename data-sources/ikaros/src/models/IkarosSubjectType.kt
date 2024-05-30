package me.him188.ani.datasources.ikaros.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// Legacy_SubjectType
@Serializable(IkarosSubjectType.AsIntSerializer::class)
enum class IkarosSubjectType(val id: Int) {
    ANIME(1),
    COMIC(2),
    GAME(3),
    MUSIC(4),
    NOVEL(6),
    REAL(6),
    OTHER(6),
    ;


    internal object AsIntSerializer : KSerializer<IkarosSubjectType> {
        override val descriptor: SerialDescriptor = Int.serializer().descriptor

        override fun deserialize(decoder: Decoder): IkarosSubjectType {
            val raw = Int.serializer().deserialize(decoder)
            return entries.firstOrNull { it.id == raw }
                ?: throw IllegalStateException("Unknown IkarosSubjectType: $raw")
        }

        override fun serialize(encoder: Encoder, value: IkarosSubjectType) {
            return Int.serializer().serialize(encoder, value.id)
        }
    }
}