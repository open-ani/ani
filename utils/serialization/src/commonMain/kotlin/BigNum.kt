package me.him188.ani.utils.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 尽量防止丢精度的数字类型
 * @see BigNumAsDoubleStringSerializer
 */
sealed class BigNum : Number() {
    abstract override fun toString(): String
    abstract fun isNegative(): Boolean

    companion object {
        val ZERO = BigNum(0)
        val ONE = BigNum(1)
    }
}

fun BigNum(value: Int): BigNum = BigNumByInt(value)
fun BigNum(value: Long): BigNum = BigNumByLong(value)
fun BigNum(value: Float): BigNum = BigNumByFloat(value)
fun BigNum(value: Double): BigNum = BigNumByDouble(value)
fun BigNum(value: String): BigNum = BigNumByString(value)

internal class BigNumByString(
    private val value: String,
) : BigNum() {
    override fun toString(): String = value
    override fun isNegative(): Boolean {
        if (value.isEmpty()) return false
        return value[0] == '-'
    }

    override fun toByte(): Byte = value.toLong().toByte()
    override fun toDouble(): Double = value.toDouble()
    override fun toFloat(): Float = value.toFloat()
    override fun toInt(): Int = value.toLong().toInt()
    override fun toLong(): Long = value.toLong()
    override fun toShort(): Short = value.toLong().toShort()
}

internal class BigNumByInt(
    private val value: Int,
) : BigNum() {
    override fun toString(): String = value.toString()
    override fun isNegative(): Boolean = value < 0
    override fun toByte(): Byte = value.toByte()
    override fun toDouble(): Double = value.toDouble()
    override fun toFloat(): Float = value.toFloat()
    override fun toInt(): Int = value
    override fun toLong(): Long = value.toLong()
    override fun toShort(): Short = value.toShort()
}

internal class BigNumByLong(
    private val value: Long,
) : BigNum() {
    override fun toString(): String = value.toString()
    override fun isNegative(): Boolean = value < 0
    override fun toByte(): Byte = value.toByte()
    override fun toDouble(): Double = value.toDouble()
    override fun toFloat(): Float = value.toFloat()
    override fun toInt(): Int = value.toInt()
    override fun toLong(): Long = value
    override fun toShort(): Short = value.toShort()
}

internal class BigNumByDouble(
    private val value: Double,
) : BigNum() {
    override fun toString(): String = value.toString()
    override fun isNegative(): Boolean = value < 0
    override fun toByte(): Byte = value.toInt().toByte()
    override fun toDouble(): Double = value
    override fun toFloat(): Float = value.toFloat()
    override fun toInt(): Int = value.toInt()
    override fun toLong(): Long = value.toLong()
    override fun toShort(): Short = value.toInt().toShort()
}

internal class BigNumByFloat(
    private val value: Float,
) : BigNum() {
    override fun toString(): String = value.toString()
    override fun isNegative(): Boolean = value < 0
    override fun toByte(): Byte = value.toInt().toByte()
    override fun toDouble(): Double = value.toDouble()
    override fun toFloat(): Float = value
    override fun toInt(): Int = value.toInt()
    override fun toLong(): Long = value.toLong()
    override fun toShort(): Short = value.toInt().toShort()
}

/**
 * 当做 string 读; 写为 double
 */
object BigNumAsDoubleStringSerializer : KSerializer<BigNum> {
    override val descriptor = Double.serializer().descriptor

    override fun serialize(encoder: Encoder, value: BigNum) = encoder.encodeDouble(value.toDouble())
    override fun deserialize(decoder: Decoder): BigNum = BigNum(decoder.decodeString())
}
