package me.him188.ani.datasources.bangumi.serializers

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.openapitools.client.models.SubjectCollectionType
import java.lang.reflect.Type

class SubjectCollectionTypeAdapter : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != SubjectCollectionType::class.java) return null
        return object : JsonAdapter<SubjectCollectionType>() {
            override fun fromJson(reader: com.squareup.moshi.JsonReader): SubjectCollectionType? {
                val value = reader.nextString()
                return SubjectCollectionType.decode(value)
            }

            override fun toJson(writer: com.squareup.moshi.JsonWriter, value: SubjectCollectionType?) {
                writer.value(value?.value)
            }
        }
    }
}
