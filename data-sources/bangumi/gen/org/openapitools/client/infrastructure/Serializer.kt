package org.openapitools.client.infrastructure

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import me.him188.ani.datasources.bangumi.serializers.CharacterTypeAdapter
import me.him188.ani.datasources.bangumi.serializers.PersonTypeAdapter
import me.him188.ani.datasources.bangumi.serializers.SubjectTypeAdapter
import me.him188.ani.datasources.bangumi.serializers.UserGroupAdapter

object Serializer {
    @JvmStatic
    val moshiBuilder: Moshi.Builder = Moshi.Builder()
        .add(OffsetDateTimeAdapter())
        .add(LocalDateTimeAdapter())
        .add(LocalDateAdapter())
        .add(UUIDAdapter())
        .add(ByteArrayAdapter())
        .add(URIAdapter())
        .add(KotlinJsonAdapterFactory())
        .add(BigDecimalAdapter())
        .add(BigIntegerAdapter())
        .add(PersonTypeAdapter())
        .add(SubjectTypeAdapter())
        .add(UserGroupAdapter())
        .add(CharacterTypeAdapter())

    @JvmStatic
    val moshi: Moshi by lazy {
        moshiBuilder.build()
    }
}
