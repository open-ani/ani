package me.him188.ani.app.data.subject

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.openapitools.client.models.EpType
import org.openapitools.client.models.Episode
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.UserEpisodeCollection
import java.math.BigDecimal


object UserEpisodeCollectionSerializer : KSerializer<UserEpisodeCollection> {
    @Serializable
    private class Delegate(
        val episode: @Serializable(EpisodeSerializer::class) Episode,
        val type: EpisodeCollectionType,
    )

    override val descriptor: SerialDescriptor get() = Delegate.serializer().descriptor

    override fun deserialize(decoder: Decoder): UserEpisodeCollection {
        val delegate = Delegate.serializer().deserialize(decoder)
        return UserEpisodeCollection(
            episode = delegate.episode,
            type = delegate.type,
        )
    }

    override fun serialize(encoder: Encoder, value: UserEpisodeCollection) {
        Delegate.serializer().serialize(
            encoder,
            Delegate(
                episode = value.episode,
                type = value.type,
            )
        )
    }
}

object EpisodeSerializer : KSerializer<Episode> {
    @Serializable
    class Delegate(
        val id: Int,
        val type: EpType,
        val name: String,
        val nameCn: String,
        val sort: Int,
        val airdate: String,
        val comment: Int,
        val duration: String,
        val desc: String,
        val disc: Int,
        val ep: Int?,
    )

    override val descriptor: SerialDescriptor get() = Delegate.serializer().descriptor

    override fun deserialize(decoder: Decoder): Episode {
        val delegate = Delegate.serializer().deserialize(decoder)
        return Episode(
            id = delegate.id,
            type = delegate.type.value,
            name = delegate.name,
            nameCn = delegate.nameCn,
            sort = BigDecimal(delegate.sort),
            airdate = delegate.airdate,
            comment = delegate.comment,
            duration = delegate.duration,
            desc = delegate.desc,
            disc = delegate.disc,
            ep = delegate.ep?.let { BigDecimal(it) },
        )
    }

    override fun serialize(encoder: Encoder, value: Episode) {
        Delegate.serializer().serialize(
            encoder,
            Delegate(
                id = value.id,
                type = EpType.entries.first { it.value == value.type },
                name = value.name,
                nameCn = value.nameCn,
                sort = value.sort.toInt(),
                airdate = value.airdate,
                comment = value.comment,
                duration = value.duration,
                desc = value.desc,
                disc = value.disc,
                ep = value.ep?.toInt(),
            )
        )
    }

}