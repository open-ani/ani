/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.torrent

import me.him188.ani.app.torrent.api.pieces.Piece

class PiecesBuilder(
    private val initialOffset: Long = 0
) {
    private val pieces = mutableListOf<Piece>()

    fun build(): List<Piece> = pieces.toList()
}

inline fun buildPieceList(
    block: PiecesBuilder.() -> Unit
): List<Piece> = buildPieceList(0, block)

inline fun buildPieceList(
    initialOffset: Long = 0,
    block: PiecesBuilder.() -> Unit
): List<Piece> {
    val builder = PiecesBuilder(initialOffset)
    builder.block()
    return builder.build()
}
