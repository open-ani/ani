/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.torrent.api.pieces

import kotlin.jvm.JvmField
import kotlin.jvm.JvmInline

@JvmInline
value class Piece
/**
 * You should always prefer [PieceList.createPieceByListIndexUnsafe]
 */
@RawPieceConstructor constructor(
    /**
     * 在一个 torrent file 中的 index.
     */
    @JvmField val pieceIndex: Int,
) {
    override fun toString(): String = "Piece($pieceIndex)"

    companion object {
        /**
         * Replacement for `null`.
         */
        @OptIn(RawPieceConstructor::class)
        val Invalid = Piece(-1)
    }
}

@RequiresOptIn(
    "This is a raw constructor, don't use it",
    level = RequiresOptIn.Level.ERROR,
)
annotation class RawPieceConstructor

enum class PieceState {
    READY,
    DOWNLOADING,
    FINISHED,
    NOT_AVAILABLE,
    ;
}
