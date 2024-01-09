package me.him188.ani.datasources.bangumi.serializers

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.openapitools.client.models.UserGroup
import java.lang.reflect.Type


// Should be added to org.openapitools.client.infrastructure.Serializer.getMoshiBuilder
class UserGroupAdapter : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != UserGroup::class.java) return null
        return object : JsonAdapter<UserGroup>() {
            override fun fromJson(reader: com.squareup.moshi.JsonReader): UserGroup? {
                val value = reader.nextString()
                return UserGroup.decode(value)
            }

            override fun toJson(writer: com.squareup.moshi.JsonWriter, value: UserGroup?) {
                writer.value(UserGroup.encode(value))
            }
        }
    }
}