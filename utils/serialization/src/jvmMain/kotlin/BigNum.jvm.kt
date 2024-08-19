package me.him188.ani.utils.serialization

import java.math.BigDecimal

fun BigNum(value: BigDecimal): BigNum = BigNumByBigDecimal(value)

private class BigNumByBigDecimal(
    private val value: BigDecimal,
) : BigNum() {
    override fun toString(): String = value.toString()
    override fun isNegative(): Boolean = value < BigDecimal.ZERO
    override fun toByte(): Byte = value.toInt().toByte()
    override fun toDouble(): Double = value.toDouble()
    override fun toFloat(): Float = value.toFloat()
    override fun toInt(): Int = value.toInt()
    override fun toLong(): Long = value.toLong()
    override fun toShort(): Short = value.toInt().toShort()
}
