package me.him188.ani.datasources.ikaros.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(IkarosEpisodeGroup.AsIntSerializer::class)
enum class IkarosEpisodeGroup(val id: Int) {
    MAIN(0),
    /**
     * PV.
     */
    PROMOTION_VIDEO(1),
    /**
     * OP.
     */
    OPENING_SONG(2),
    /**
     * ED.
     */
    ENDING_SONG(3),
    /**
     * SP.
     */
    SPECIAL_PROMOTION(4),
    /**
     * ST.
     */
    SMALL_THEATER(5),
    /**
     * Live.
     */
    LIVE(6),
    /**
     * commercial message, CM.
     */
    COMMERCIAL_MESSAGE(7),
    MUSIC_DIST1(8),
    MUSIC_DIST2(9),
    MUSIC_DIST3(10),
    MUSIC_DIST4(11),
    MUSIC_DIST5(12),
    OTHER(13)
    ;


    internal object AsIntSerializer : KSerializer<IkarosEpisodeGroup> {
        override val descriptor: SerialDescriptor = Int.serializer().descriptor

        override fun deserialize(decoder: Decoder): IkarosEpisodeGroup {
            val raw = Int.serializer().deserialize(decoder)
            return entries.firstOrNull { it.id == raw }
                ?: throw IllegalStateException("Unknown IkarosCollectionType: $raw")
        }

        override fun serialize(encoder: Encoder, value: IkarosEpisodeGroup) {
            return Int.serializer().serialize(encoder, value.id)
        }
    }
}