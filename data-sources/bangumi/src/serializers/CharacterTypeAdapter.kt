package me.him188.animationgarden.datasources.bangumi.serializers

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.openapitools.client.models.CharacterType
import java.lang.reflect.Type

class CharacterTypeAdapter : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != CharacterType::class.java) return null
        return object : JsonAdapter<CharacterType>() {
            override fun fromJson(reader: com.squareup.moshi.JsonReader): CharacterType? {
                val value = reader.nextString()
                return CharacterType.decode(value)
            }

            override fun toJson(writer: com.squareup.moshi.JsonWriter, value: CharacterType?) {
                writer.value(CharacterType.encode(value))
            }
        }
    }
}