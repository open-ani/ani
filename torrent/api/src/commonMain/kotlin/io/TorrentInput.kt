package me.him188.ani.app.torrent.io

import kotlinx.io.IOException
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.utils.io.BufferedInput.Companion.DEFAULT_BUFFER_PER_DIRECTION
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.length

/**
 * @param pieces The corresponding pieces of the [file], must contain all bytes in the [file]. 不需要排序.
 * @param logicalStartOffset 逻辑上的偏移量, 也就是当 seek `k` 时, 实际上是在 `logicalStartOffset + k` 处.
 */
@Suppress("FunctionName")
@Throws(IOException::class)
expect fun TorrentInput(
    file: SystemPath,
    pieces: List<Piece>, // must support random access
    logicalStartOffset: Long = pieces.minOf { it.offset }, // 默认为第一个 piece 开头
    onWait: suspend (Piece) -> Unit = { },
    bufferSize: Int = DEFAULT_BUFFER_PER_DIRECTION,
    size: Long = file.length()
): SeekableInput
