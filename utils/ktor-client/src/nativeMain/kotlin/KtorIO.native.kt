package me.him188.ani.utils.ktor

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import me.him188.ani.utils.io.DEFAULT_BUFFER_SIZE

actual fun ByteReadChannel.toRawSource(): RawSource {
    return object : RawSource {
        override fun close() {
            this@toRawSource.cancel()
        }

        private val buf = ByteArray(DEFAULT_BUFFER_SIZE)
        override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
            val read = runBlocking { this@toRawSource.readAvailable(buf, 0, buf.size) }
            if (read == -1) return -1
            sink.write(buf, 0, read)
            return read.toLong()
        }
    }
}
