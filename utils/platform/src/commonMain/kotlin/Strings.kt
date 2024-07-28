package me.him188.ani.utils.platform

import kotlin.math.round

/**
 * Equivalent to `String.format("%.1f", value)`
 */
fun String.Companion.format1f(value: Double): String {
    return (round(value * 10) / 10.0).toString()
}

fun String.Companion.format1f(value: Float): String {
    return (round(value * 10) / 10.0).toString()
}

/**
 * Equivalent to `String.format("%.2f", value)`
 */
fun String.Companion.format2f(value: Double): String {
    return (round(value * 100) / 100.0).toString()
}

/**
 * Equivalent to `String.format("%.2f", value)`
 */
fun String.Companion.format2f(value: Float): String {
    return (round(value * 100) / 100.0).toString()
}
