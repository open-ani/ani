package me.him188.animationgarden.datasources.bangumi.serializers

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.openapitools.client.models.PersonType
import java.lang.reflect.Type


// Should be added to org.openapitools.client.infrastructure.Serializer.getMoshiBuilder
class PersonTypeAdapter : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != PersonType::class.java) return null
        return object : JsonAdapter<PersonType>() {
            override fun fromJson(reader: com.squareup.moshi.JsonReader): PersonType? {
                val value = reader.nextString()
                return PersonType.decode(value)
            }

            override fun toJson(writer: com.squareup.moshi.JsonWriter, value: PersonType?) {
                writer.value(PersonType.encode(value))
            }
        }
    }
}
