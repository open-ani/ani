package me.him188.ani.app.torrent.io

import kotlinx.io.IOException
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.utils.io.SeekableInput
import me.him188.ani.utils.io.SystemPath

/**
 * @param pieces The corresponding pieces of the [file], must contain all bytes in the [file]. 不需要排序.
 * @param logicalStartOffset 逻辑上的偏移量, 也就是当 seek `k` 时, 实际上是在 `logicalStartOffset + k` 处.
 */
@Suppress("FunctionName")
@Throws(IOException::class)
actual fun TorrentInput(
    file: SystemPath,
    pieces: List<Piece>,
    logicalStartOffset: Long,
    onWait: suspend (Piece) -> Unit,
    bufferSize: Int,
    size: Long,
): SeekableInput {
    TODO("Not yet implemented")
}