package me.him188.ani.danmaku.server.data.mongodb

import kotlinx.serialization.StringFormat
import kotlinx.serialization.serializerOrNull
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

@Suppress("FunctionName")
fun ServerCodecRegistry(format: StringFormat): CodecRegistry {
    return CodecRegistries.fromProviders(KotlinxSerializationCodecProvider(format))
}

class KotlinxSerializationCodecProvider(
    private val format: StringFormat,
) : CodecProvider {
    override fun <T> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? {
        val serializer = format.serializersModule.serializerOrNull(clazz) ?: return null

        return object : Codec<T> {
            override fun encode(writer: BsonWriter, value: T, encoderContext: EncoderContext?) {
                if (value == null) {
                    writer.writeNull()
                    return
                }
                val str = format.encodeToString(serializer, value)
                writer.writeString(str)
            }

            override fun getEncoderClass(): Class<T> {
                return clazz
            }

            override fun decode(reader: BsonReader, decoderContext: DecoderContext?): T {
                @Suppress("UNCHECKED_CAST") val str =
                    reader.readString() ?: return serializer.descriptor.getElementDescriptor(0).getElementName(0) as T
                val decoded = format.decodeFromString(serializer, str)
                return clazz.cast(decoded)
            }
        }
    }
}