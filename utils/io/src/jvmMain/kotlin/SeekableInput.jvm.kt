package me.him188.ani.utils.io

actual fun SystemPath.toSeekableInput(
    bufferSize: Int,
    onFillBuffer: (() -> Unit)?,
): SeekableInput = this.toFile().toSeekableInput(bufferSize, onFillBuffer)
