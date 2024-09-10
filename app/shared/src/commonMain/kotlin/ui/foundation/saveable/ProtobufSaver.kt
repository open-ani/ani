package me.him188.ani.app.ui.foundation.saveable

import androidx.compose.runtime.saveable.SaverScope
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf

fun <T> protobufSaver(serializer: KSerializer<T>): ProtobufSaver<T> = ProtobufSaver(serializer, serializer)

fun <T> protobufSaver(
    serializationStrategy: SerializationStrategy<T>,
    deserializationStrategy: DeserializationStrategy<T>,
): ProtobufSaver<T> = ProtobufSaver(serializationStrategy, deserializationStrategy)

class ProtobufSaver<T>(
    private val serializationStrategy: SerializationStrategy<T>,
    private val deserializationStrategy: DeserializationStrategy<T>,
    private val shouldSave: (T) -> Boolean = { true }
) : androidx.compose.runtime.saveable.Saver<T, ByteArray> {
    private val proto = ProtoBuf {
        encodeDefaults = false
    }

    override fun restore(value: ByteArray): T? {
        if (value.isEmpty()) {
            return null
        }
        return proto.decodeFromByteArray(deserializationStrategy, value)
    }

    override fun SaverScope.save(value: T): ByteArray {
        if (!shouldSave(value)) {
            return byteArrayOf()
        }
        return proto.encodeToByteArray(serializationStrategy, value)
    }
}
