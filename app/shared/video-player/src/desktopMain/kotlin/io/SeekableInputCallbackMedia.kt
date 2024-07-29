package me.him188.ani.app.videoplayer.io

import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.logger
import uk.co.caprica.vlcj.media.callback.DefaultCallbackMedia

class SeekableInputCallbackMedia(
    private val input: SeekableInput
) : DefaultCallbackMedia(true) {
    override fun onGetSize(): Long = input.size
    override fun onOpen(): Boolean {
        if (ENABLE_LOGS) logger.debug { "open" }
        onSeek(0L)
        return true
    }

    override fun onRead(buffer: ByteArray, bufferSize: Int): Int {
        if (ENABLE_LOGS) logger.debug { "reading max $bufferSize" }
        return input.read(buffer, 0, bufferSize).also {
            if (ENABLE_LOGS) logger.debug { "read $it" }
        }
    }

    override fun onSeek(offset: Long): Boolean {
        if (ENABLE_LOGS) logger.debug { "seeking to $offset" }
        input.seek(offset)
        if (ENABLE_LOGS) logger.debug { "seeking to $offset: ok" }
        return true
    }

    override fun onClose() {}

    private companion object {
        private const val ENABLE_LOGS = false
        val logger = logger<SeekableInputCallbackMedia>()
    }
}