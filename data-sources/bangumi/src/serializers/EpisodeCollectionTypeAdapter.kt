package me.him188.ani.datasources.bangumi.serializers

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.openapitools.client.models.EpisodeCollectionType
import java.lang.reflect.Type

class EpisodeCollectionTypeAdapter : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != EpisodeCollectionType::class.java) return null
        return object : JsonAdapter<EpisodeCollectionType>() {
            override fun fromJson(reader: com.squareup.moshi.JsonReader): EpisodeCollectionType? {
                return EpisodeCollectionType.decode(reader.nextString())
            }

            override fun toJson(writer: com.squareup.moshi.JsonWriter, value: EpisodeCollectionType?) {
                writer.value(EpisodeCollectionType.encode(value))
            }
        }
    }
}
