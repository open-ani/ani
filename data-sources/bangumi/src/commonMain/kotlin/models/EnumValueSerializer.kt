package me.him188.ani.datasources.bangumi.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

open class EnumValueSerializer<T>(
    name: String,
    private val getValue: (Int) -> T,
    private val toInt: (T) -> Int
) : KSerializer<T> {
    constructor(name: String, entries: List<T>, toInt: (T) -> Int) : this(
        name,
        { value ->
            entries.firstOrNull { toInt(it) == value }
                ?: throw IllegalArgumentException("Unknown value $value for enum $name")
        },
        { it: T -> toInt(it) },
    )

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(name, PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): T {
        return getValue(decoder.decodeInt())
    }

    override fun serialize(encoder: Encoder, value: T) {
        return toInt(value).let(encoder::encodeInt)
    }
}