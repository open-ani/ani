package me.him188.ani.utils.ktor

import io.ktor.utils.io.ByteReadChannel
import kotlinx.io.RawSource
import kotlinx.io.buffered

/**
 * Unbuffered
 */
expect fun ByteReadChannel.toRawSource(): RawSource

fun ByteReadChannel.toSource() = toRawSource().buffered()

@Deprecated("For migration. Use toSource instead", ReplaceWith("toSource()"), level = DeprecationLevel.ERROR)
fun ByteReadChannel.toInputStream() = toSource()
