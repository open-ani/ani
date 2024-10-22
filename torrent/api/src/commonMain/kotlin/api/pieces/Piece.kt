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

/**
 * 表示 torrent 任务中的一个 piece.
 *
 * [Piece] 本质上 [pieceIndex] 的包装, 即它只表示一个 piece 的位置, 而不包含其大小和数据等信息.
 *
 * [PieceList] 才包含每个 piece 的数据范围, 大小等信息. [Piece] 需要在一个 [PieceList] 中使用, 才可以获取到更多的信息, 用法示例:
 * ```
 * val size: Long = with(pieceList) {
 *     piece.size
 * }
 * ```
 */
@JvmInline
value class Piece
/**
 * 你通常不应该直接构造 [Piece] 实例.
 * 一般使用 [PieceList.first], [PieceList.forEach], [PieceList.getByPieceIndex] 等方法来获取.
 *
 * 如果你在实现 helper functions, 可以考虑 [PieceList.createPieceByListIndexUnsafe].
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
         * Replacement for `null` to avoid boxing.
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
