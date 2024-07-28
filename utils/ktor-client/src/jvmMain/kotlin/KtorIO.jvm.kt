package me.him188.ani.utils.ktor

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.io.RawSource
import kotlinx.io.asSource

actual fun ByteReadChannel.toRawSource(): RawSource = toInputStream().asSource()
