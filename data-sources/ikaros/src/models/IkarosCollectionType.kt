package me.him188.ani.datasources.ikaros.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(IkarosCollectionType.AsIntSerializer::class)
enum class IkarosCollectionType(val id: Int) {
    /**
     * Wist watch.
     */
    WISH(1),
    /**
     * Watching.
     */
    DOING(2),
    /**
     * Watch done.
     */
    DONE(3),
    /**
     * No time to watch it.
     */
    SHELVE(4),
    /**
     * Discard it.
     */
    DISCARD(5);
    ;


    internal object AsIntSerializer : KSerializer<IkarosCollectionType> {
        override val descriptor: SerialDescriptor = Int.serializer().descriptor

        override fun deserialize(decoder: Decoder): IkarosCollectionType {
            val raw = Int.serializer().deserialize(decoder)
            return entries.firstOrNull { it.id == raw }
                ?: throw IllegalStateException("Unknown IkarosCollectionType: $raw")
        }

        override fun serialize(encoder: Encoder, value: IkarosCollectionType) {
            return Int.serializer().serialize(encoder, value.id)
        }
    }
}