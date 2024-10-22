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
        get() = states[pieceIndex]
        set(value) {
            states[pieceIndex] = value
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
        get() = with(delegate) { delegate.get(pieceIndex).state }
        set(value) {
            with(delegate) {
                delegate.get(pieceIndex).state = value
            }
        }

    override fun Piece.compareAndSetState(expect: PieceState, update: PieceState): Boolean {
        with(delegate) {
            return delegate.get(pieceIndex).compareAndSetState(expect, update)
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

    /**
     * 第 0 个元素的 piece index. 如果列表为空则为 `0`.
     */
    @PublishedApi
    internal abstract val initialPieceIndex: Int

    val totalSize: Long by lazy(LazyThreadSafetyMode.PUBLICATION) { sizes.sum() }

    abstract var Piece.state: PieceState
    abstract fun Piece.compareAndSetState(expect: PieceState, update: PieceState): Boolean

    @PublishedApi
    internal inline val Piece.indexInList get() = pieceIndex

    /**
     * 该 piece 的数据长度 bytes
     */
    inline val Piece.size get() = sizes[indexInList]

    /**
     * 在种子所能下载的所有文件数据中的 offset bytes
     */
    inline val Piece.dataOffset get() = dataOffsets[indexInList]

    // extensions

    val Piece.dataStartOffset: Long get() = dataOffset
    val Piece.dataLastOffset: Long get() = dataOffset + size - 1
    inline val Piece.offsetRange: LongRange get() = dataStartOffset..dataLastOffset

    /**
     * 根据 piece 在此列表中的顺序, 计算出它的真实 [Piece.pieceIndex], 然后创建一个 [Piece] 实例.
     * 创建的实例可以在此 PieceList 或有关 slice 中使用.
     *
     * 不会检查是否越界.
     */
    @OptIn(RawPieceConstructor::class)
    @PublishedApi
    internal fun createPieceByListIndexUnsafe(listIndex: Int): Piece =
        Piece(initialPieceIndex + listIndex)

    /**
     * @throws IndexOutOfBoundsException
     */
    fun get(pieceIndex: Int): Piece {
        if (!containsAbsolutePieceIndex(pieceIndex)) {
            throw IndexOutOfBoundsException(
                "pieceIndex $pieceIndex out of bounds " +
                        "${initialPieceIndex}..${initialPieceIndex + sizes.size}",
            )
        }
        return createPieceByListIndexUnsafe(pieceIndex - initialPieceIndex)
    }

//    fun getByListIndex(listIndex: Int): Piece {
//        if (!containsListIndex(listIndex)) {
//            throw IndexOutOfBoundsException("listIndex $listIndex out of bounds")
//        }
//        return createPieceByListIndex(listIndex)
//    }

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
        fun create(
            numPieces: Int,
            initialDataOffset: Long = 0L,
            initialPieceIndex: Int = 0,
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

            return PieceListImpl(sizes, offsets, states, initialPieceIndex)
        }

        fun create(
            totalSize: Long,
            pieceSize: Long,
            initialDataOffset: Long = 0L,
            initialPieceIndex: Int = 0,
        ): PieceList {
            if (totalSize % pieceSize == 0L) {
                return create((totalSize / pieceSize).toInt(), initialDataOffset, getPieceSize = { pieceSize })
            }

            val numPieces = (totalSize / pieceSize).toInt() + 1
            val sizes = LongArray(numPieces) { pieceSize }
            val offsets = LongArray(numPieces)
            val states = Array(numPieces) { PieceState.READY }

            var pieceOffset = initialDataOffset
            for (i in 0 until numPieces - 1) {
                offsets[i] = pieceOffset
                pieceOffset += sizes[i]
            }

            sizes[numPieces - 1] = totalSize % pieceSize
            offsets[numPieces - 1] = totalSize - (totalSize % pieceSize) + initialDataOffset

            return PieceListImpl(sizes, offsets, states, initialPieceIndex)
        }

    }
}

/**
 * 此列表包含的 piece 数量
 */
val PieceList.count get() = sizes.size

/**
 * 此列表是否为空
 */
fun PieceList.isEmpty() = sizes.isEmpty()

/**
 * 提供一个将各个 [Piece] box 后的 [Sequence]. 性能很低, 仅限性能不敏感场景使用.
 */
fun PieceList.asSequence(): Sequence<Piece> = object : Sequence<Piece> {
    override fun iterator(): Iterator<Piece> = object : Iterator<Piece> {
        private var index = 0
        override fun hasNext(): Boolean = index < sizes.size
        override fun next(): Piece = createPieceByListIndexUnsafe(index++)
    }
}

//fun PieceList.containsListIndex(listIndex: Int): Boolean = listIndex in sizes.indices
fun PieceList.containsAbsolutePieceIndex(absolutePieceIndex: Int): Boolean =
    absolutePieceIndex in initialPieceIndex until initialPieceIndex + sizes.size

///////////////////////////////////////////////////////////////////////////
// Collection extensions
///////////////////////////////////////////////////////////////////////////

fun PieceList.first(): Piece {
    if (isEmpty()) throw NoSuchElementException()
    return createPieceByListIndexUnsafe(0)
}

fun PieceList.last(): Piece {
    if (isEmpty()) throw NoSuchElementException()
    return createPieceByListIndexUnsafe(sizes.size - 1)
}

inline fun PieceList.forEach(block: PieceList.(Piece) -> Unit) {
    for (i in sizes.indices) {
        block(createPieceByListIndexUnsafe(i))
    }
}

inline fun PieceList.sumOf(block: PieceList.(Piece) -> Long): Long {
    var sum = 0L
    for (i in sizes.indices) {
        sum += block(createPieceByListIndexUnsafe(i))
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
        val value = block(createPieceByListIndexUnsafe(i))
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
        val value = block(createPieceByListIndexUnsafe(i))
        if (value < min) {
            min = value
        }
    }
    return min
}

/**
 * @see kotlin.collections.indexOfFirst
 */
inline fun PieceList.indexOfFirst(predicate: PieceList.(Piece) -> Boolean): Int {
    for (i in sizes.indices) {
        if (predicate(createPieceByListIndexUnsafe(i))) {
            return i
        }
    }
    return -1
}

/**
 * @see kotlin.collections.dropWhile
 */
inline fun PieceList.dropWhile(predicate: PieceList.(Piece) -> Boolean): List<Piece> {
    val list = mutableListOf<Piece>()
    var found = false
    for (i in sizes.indices) {
        if (!found && predicate(createPieceByListIndexUnsafe(i))) {
            continue
        }
        found = true
        list.add(createPieceByListIndexUnsafe(i))
    }
    return list
}

/**
 * @see kotlin.collections.takeWhile
 */
inline fun PieceList.takeWhile(predicate: PieceList.(Piece) -> Boolean): List<Piece> {
    val list = mutableListOf<Piece>()
    for (i in sizes.indices) {
        if (predicate(createPieceByListIndexUnsafe(i))) {
            list.add(createPieceByListIndexUnsafe(i))
        } else {
            break
        }
    }
    return list
}

/**
 * @see kotlin.collections.binarySearch
 */
inline fun PieceList.binarySearch(
    fromIndex: Int = 0,
    toIndex: Int = count,
    comparator: PieceList.(Piece) -> Int,
): Int {
    // TODO: check, this is written by Copilot
    var low = fromIndex
    var high = toIndex - 1
    while (low <= high) {
        val mid = (low + high).ushr(1)
        val cmp = comparator(createPieceByListIndexUnsafe(mid))
        when {
            cmp < 0 -> low = mid + 1
            cmp > 0 -> high = mid - 1
            else -> return mid // key found
        }
    }
    return -1
}

