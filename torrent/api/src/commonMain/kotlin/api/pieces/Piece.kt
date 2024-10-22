/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.torrent.api.pieces

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.jvm.JvmInline

@JvmInline
value class Piece(
    /**
     * 在一个 [PieceList] 中的 index
     */
    val listIndex: Int,
) {
    init {
        require(listIndex >= 0) { "listIndex < 0" }
    }

    override fun toString(): String = "Piece($listIndex)"
}

class PieceListImpl(
    @PublishedApi
    override val sizes: LongArray, // immutable
    @PublishedApi
    override val dataOffsets: LongArray, // immutable
    private val states: Array<PieceState>, // mutable
    @PublishedApi
    override val initialPieceIndex: Int, // 第 0 个元素的 piece index
) : PieceList(
) {
    init {
        require(sizes.size == dataOffsets.size) { "sizes.size != dataOffsets.size" }
        require(sizes.size == states.size) { "sizes.size != states.size" }
    }

    override var Piece.state: PieceState
        get() = states[absolutePieceIndex]
        set(value) {
            states[absolutePieceIndex] = value
        }

    private val lock = SynchronizedObject()

    override fun Piece.compareAndSetState(expect: PieceState, update: PieceState): Boolean {
        synchronized(lock) {
            if (state == expect) {
                state = update
                return true
            }
            return false
        }
    }
}

class PieceListSlice(
    private val delegate: PieceList,
    private val startIndex: Int,
    endIndex: Int,
) : PieceList() {
    init {
        require(startIndex >= 0) { "startIndex < 0" }
        require(endIndex <= delegate.sizes.size) { "endIndex > list.sizes.size" }
        require(startIndex < endIndex) { "startIndex >= endIndex" }
    }

    override val sizes: LongArray = delegate.sizes.copyOfRange(startIndex, endIndex)
    override val dataOffsets: LongArray = delegate.dataOffsets.copyOfRange(startIndex, endIndex)
    override val initialPieceIndex: Int = delegate.initialPieceIndex + startIndex
    override var Piece.state: PieceState
        get() = with(delegate) { delegate.getByAbsolutePieceIndex(startIndex + listIndex).state }
        set(value) {
            with(delegate) {
                delegate.getByAbsolutePieceIndex(startIndex + listIndex).state = value
            }
        }

    override fun Piece.compareAndSetState(expect: PieceState, update: PieceState): Boolean {
        with(delegate) {
            return delegate.getByAbsolutePieceIndex(startIndex + listIndex).compareAndSetState(expect, update)
        }
    }
}

fun PieceList.slice(startIndex: Int, endIndex: Int): PieceList {
    require(startIndex >= 0) { "startIndex < 0" }
    require(endIndex <= sizes.size) { "endIndex > sizes.size" }
    require(startIndex < endIndex) { "startIndex >= endIndex" }
    return PieceListSlice(this, startIndex, endIndex)
}

abstract class PieceList protected constructor(
) {
    @PublishedApi
    internal abstract val sizes: LongArray // immutable

    @PublishedApi
    internal abstract val dataOffsets: LongArray // immutable

    @PublishedApi
    internal abstract val initialPieceIndex: Int // 第 0 个元素的 piece index

    val count get() = sizes.size

    fun isEmpty() = sizes.isEmpty()

    val totalSize: Long get() = sizes.sum()

    abstract var Piece.state: PieceState
    abstract fun Piece.compareAndSetState(expect: PieceState, update: PieceState): Boolean

    inline val Piece.absolutePieceIndex get() = initialPieceIndex + listIndex

    /**
     * 该 piece 的数据长度 bytes
     */
    inline val Piece.size get() = sizes[absolutePieceIndex]

    /**
     * 在种子所能下载的所有文件数据中的 offset bytes
     */
    inline val Piece.dataOffset get() = dataOffsets[absolutePieceIndex]

    // extensions

    val Piece.dataStartOffset: Long get() = dataOffset
    val Piece.dataLastOffset: Long get() = dataOffset + size - 1
    inline val Piece.offsetRange: LongRange get() = dataStartOffset..dataLastOffset

    fun getByAbsolutePieceIndex(pieceIndex: Int): Piece = Piece(pieceIndex - initialPieceIndex)
    fun getByListIndex(listIndex: Int): Piece = Piece(listIndex)

    suspend inline fun Piece.awaitFinished() {
        val piece = this
        TODO("Piece.awaitFinished")
//        if (piece.state.value != PieceState.FINISHED) {
//            piece.state.takeWhile { it != PieceState.FINISHED }.collect()
//        }
    }


    class Subscription(
        val pieceIndex: Int,
        val onStateChange: PieceList.(Piece) -> Unit,
    )

    internal val subscriptions: MutableStateFlow<List<Subscription>> = MutableStateFlow(emptyList())

    companion object {
        fun buildPieces(
            numPieces: Int,
            initialDataOffset: Long = 0L,
            getPieceSize: (index: Int) -> Long,
        ): PieceList {
            val sizes = LongArray(numPieces) { getPieceSize(it) }
            val offsets = LongArray(numPieces)
            val states = Array(numPieces) { PieceState.READY }

            var pieceOffset = initialDataOffset
            for (i in 0 until numPieces) {
                offsets[i] = pieceOffset
                pieceOffset += sizes[i]
            }

            return PieceListImpl(sizes, offsets, states, 0)
        }

        fun buildPieces(
            totalSize: Long,
            pieceSize: Long,
            initial: Long = 0L,
        ): PieceList {
            if (totalSize % pieceSize == 0L) {
                return buildPieces((totalSize / pieceSize).toInt(), initial) { pieceSize }
            }

            val numPieces = (totalSize / pieceSize).toInt() + 1
            val sizes = LongArray(numPieces) { pieceSize }
            val offsets = LongArray(numPieces)
            val states = Array(numPieces) { PieceState.READY }

            var pieceOffset = initial
            for (i in 0 until numPieces - 1) {
                offsets[i] = pieceOffset
                pieceOffset += sizes[i]
            }

            sizes[numPieces - 1] = totalSize % pieceSize
            offsets[numPieces - 1] = totalSize - (totalSize % pieceSize) + initial

            return PieceListImpl(sizes, offsets, states, 0)
        }

    }
}

fun PieceList.asSequence(): Sequence<Piece> = object : Sequence<Piece> {
    override fun iterator(): Iterator<Piece> = object : Iterator<Piece> {
        private var index = 0
        override fun hasNext(): Boolean = index < sizes.size
        override fun next(): Piece = Piece(index++)
    }
}

fun PieceList.containsListIndex(listIndex: Int): Boolean = listIndex in sizes.indices
fun PieceList.containsAbsolutePieceIndex(absolutePieceIndex: Int): Boolean =
    absolutePieceIndex in initialPieceIndex until initialPieceIndex + sizes.size

fun PieceList.first(): Piece {
    if (isEmpty()) throw NoSuchElementException()
    return Piece(0)
}

fun PieceList.last(): Piece {
    if (isEmpty()) throw NoSuchElementException()
    return Piece(sizes.size - 1)
}

inline fun PieceList.forEach(block: PieceList.(Piece) -> Unit) {
    for (i in sizes.indices) {
        block(Piece(i))
    }
}

inline fun PieceList.sumOf(block: PieceList.(Piece) -> Long): Long {
    var sum = 0L
    for (i in sizes.indices) {
        sum += block(Piece(i))
    }
    return sum
}

inline fun PieceList.maxOf(block: PieceList.(Piece) -> Long): Long {
    val sizes = sizes
    if (sizes.isEmpty()) {
        throw NoSuchElementException()
    }
    var max = Long.MIN_VALUE
    for (i in sizes.indices) {
        val value = block(Piece(i))
        if (value > max) {
            max = value
        }
    }
    return max
}

inline fun PieceList.minOf(block: PieceList.(Piece) -> Long): Long {
    val sizes = sizes
    if (sizes.isEmpty()) {
        throw NoSuchElementException()
    }
    var min = Long.MAX_VALUE
    for (i in sizes.indices) {
        val value = block(Piece(i))
        if (value < min) {
            min = value
        }
    }
    return min
}

inline fun PieceList.indexOfFirst(predicate: PieceList.(Piece) -> Boolean): Int {
    for (i in sizes.indices) {
        if (predicate(Piece(i))) {
            return i
        }
    }
    return -1
}

inline fun PieceList.dropWhile(predicate: PieceList.(Piece) -> Boolean): List<Piece> {
    val list = mutableListOf<Piece>()
    var found = false
    for (i in sizes.indices) {
        if (!found && predicate(Piece(i))) {
            continue
        }
        found = true
        list.add(Piece(i))
    }
    return list
}

inline fun PieceList.takeWhile(predicate: PieceList.(Piece) -> Boolean): List<Piece> {
    val list = mutableListOf<Piece>()
    for (i in sizes.indices) {
        if (predicate(Piece(i))) {
            list.add(Piece(i))
        } else {
            break
        }
    }
    return list
}

inline fun PieceList.binarySearch(predicate: PieceList.(Piece) -> Int): Int {
    // TODO: check, this is written by Copilot
    var low = 0
    var high = sizes.size - 1
    while (low <= high) {
        val mid = (low + high).ushr(1)
        val result = predicate(Piece(mid))
        when {
            result < 0 -> high = mid - 1
            result > 0 -> low = mid + 1
            else -> return mid
        }
    }
    return -1
}

enum class PieceState {
    READY,
    DOWNLOADING,
    FINISHED,
    NOT_AVAILABLE,
    ;
}
