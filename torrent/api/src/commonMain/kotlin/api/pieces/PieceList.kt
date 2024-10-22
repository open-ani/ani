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
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.jvm.JvmField

class PieceListImpl(
    sizes: LongArray, // immutable
    dataOffsets: LongArray, // immutable
    private val states: Array<PieceState>, // mutable
    initialPieceIndex: Int, // 第 0 个元素的 piece index
) : PieceList(sizes, dataOffsets, initialPieceIndex) {
    init {
        require(sizes.size == dataOffsets.size) { "sizes.size != dataOffsets.size" }
        require(sizes.size == states.size) { "sizes.size != states.size" }
    }

    override var Piece.state: PieceState
        get() = states[pieceIndex]
        set(value) {
            states[pieceIndex] = value
            notifyPieceStateChanges(this)
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

    // use object identity
    class Subscription(
        val pieceIndex: Int,
        val onStateChange: PieceList.(Subscription, Piece) -> Unit,
    )

    private val subscriptions: MutableStateFlow<PersistentList<Subscription>> = MutableStateFlow(persistentListOf())

    /**
     * Call related subscribers
     */
    private fun notifyPieceStateChanges(changedPiece: Piece) {
        val subscriptions = subscriptions.value
        for (subscription in subscriptions) {
            if (subscription.pieceIndex == changedPiece.pieceIndex) {
                subscription.onStateChange(this, subscription, changedPiece)
            }
        }
    }

    private fun subscribe(pieceIndex: Int, onStateChange: PieceList.(Subscription, Piece) -> Unit): Subscription {
        val subscriptions = subscriptions
        while (true) {
            val prevValue = subscriptions.value
            val sub = Subscription(pieceIndex, onStateChange)
            val nextValue = prevValue.plus(sub)
            if (subscriptions.compareAndSet(prevValue, nextValue)) {
                return sub
            }
        }
    }

    private fun unsubscribe(subscription: Subscription) {
        subscriptions.update { list ->
            list.minus(subscription)
        }
    }

    override suspend fun Piece.awaitFinished() {
        val piece = this
        suspendCancellableCoroutine { cont ->
            val sub = subscribe(piece.pieceIndex) { subscription, piece ->
                if (piece.state == PieceState.FINISHED) {
                    cont.resumeWith(Result.success(Unit))
                    unsubscribe(subscription)
                }
            }
            cont.invokeOnCancellation {
                unsubscribe(sub)
            }
        }
    }
}

class PieceListSlice(
    private val delegate: PieceList,
    startIndex: Int,
    endIndex: Int,
) : PieceList(
    sizes = delegate.sizes.copyOfRange(startIndex, endIndex),
    dataOffsets = delegate.dataOffsets.copyOfRange(startIndex, endIndex),
    initialPieceIndex = delegate.initialPieceIndex + startIndex,
) {
    init {
        require(startIndex >= 0) { "startIndex < 0" }
        require(endIndex <= delegate.sizes.size) { "endIndex > list.sizes.size" }
        require(startIndex <= endIndex) { "startIndex >= endIndex" }
    }

    override var Piece.state: PieceState
        get() = with(delegate) { state }
        set(value) {
            with(delegate) {
                state = value
            }
        }

    override fun Piece.compareAndSetState(expect: PieceState, update: PieceState): Boolean {
        with(delegate) {
            return compareAndSetState(expect, update)
        }
    }

    override suspend fun Piece.awaitFinished() {
        with(delegate) {
            awaitFinished()
        }
    }
}

fun PieceList.slice(startIndex: Int, endIndex: Int): PieceList {
    require(startIndex >= 0) { "startIndex < 0" }
    require(endIndex <= sizes.size) { "endIndex > sizes.size" }
    require(startIndex <= endIndex) { "startIndex >= endIndex" }
    return PieceListSlice(this, startIndex, endIndex)
}

sealed class PieceList(
    @PublishedApi
    @JvmField
    internal val sizes: LongArray, // immutable
    @PublishedApi
    @JvmField
    internal val dataOffsets: LongArray,// immutable
    /**
     * 第 0 个元素的 piece index. 如果列表为空则为 `0`.
     */
    @PublishedApi
    @JvmField
    internal val initialPieceIndex: Int
) {
    val totalSize: Long by lazy(LazyThreadSafetyMode.PUBLICATION) { sizes.sum() }

    abstract var Piece.state: PieceState
    abstract fun Piece.compareAndSetState(expect: PieceState, update: PieceState): Boolean

    @PublishedApi
    internal inline val Piece.indexInList get() = pieceIndex - initialPieceIndex

    /**
     * 该 piece 的数据长度 bytes
     */
    inline val Piece.size get() = sizes[indexInList]

    // extensions

    /**
     * 在种子所能下载的所有文件数据中的 offset bytes
     */
    inline val Piece.dataStartOffset: Long get() = dataOffsets[indexInList]

    /**
     * inclusive
     */
    inline val Piece.dataLastOffset: Long get() = dataOffsets[indexInList] + size - 1

    /**
     * exclusive
     */
    inline val Piece.dataEndOffset: Long get() = dataOffsets[indexInList] + size

    inline val Piece.dataOffsetRange: LongRange get() = dataStartOffset..dataLastOffset

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
    fun getByPieceIndex(pieceIndex: Int): Piece {
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

    abstract suspend fun Piece.awaitFinished()

    companion object {
        val Empty = create(0) { 0 }

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

inline val PieceList.listIndices: IntRange get() = sizes.indices

inline val PieceList.pieceIndices: IntRange get() = initialPieceIndex until initialPieceIndex + sizes.size

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

@OptIn(RawPieceConstructor::class)
inline fun PieceList.forEach(block: PieceList.(Piece) -> Unit) {
    for (i in pieceIndices) {
        block(Piece(i))
    }
}

inline fun PieceList.forEachIndexed(block: PieceList.(index: Int, Piece) -> Unit) {
    for (i in sizes.indices) {
        block(i, createPieceByListIndexUnsafe(i))
    }
}

inline fun <C : MutableCollection<E>, E> PieceList.mapTo(destination: C, transform: PieceList.(Piece) -> E): C {
    forEach { p ->
        destination.add(transform(p))
    }
    return destination
}

inline fun <R> PieceList.mapIndexed(transform: PieceList.(index: Int, Piece) -> R): List<R> {
    val list = ArrayList<R>(this.count)
    forEachIndexed { index, p ->
        list.add(transform(index, p))
    }
    return list
}

inline fun PieceList.sumOf(block: PieceList.(Piece) -> Long): Long {
    var sum = 0L
    forEach { p ->
        sum += block(p)
    }
    return sum
}

inline fun PieceList.maxOf(block: PieceList.(Piece) -> Long): Long {
    val sizes = sizes
    if (sizes.isEmpty()) {
        throw NoSuchElementException()
    }
    var max = Long.MIN_VALUE
    forEach { p ->
        val value = block(p)
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
    forEach { p ->
        val value = block(p)
        if (value < min) {
            min = value
        }
    }
    return min
}

inline fun PieceList.maxBy(block: PieceList.(Piece) -> Long): Piece {
    val sizes = sizes
    if (sizes.isEmpty()) {
        throw NoSuchElementException()
    }
    var max = Long.MIN_VALUE
    var maxPiece: Piece = Piece.Invalid
    forEach { p ->
        val value = block(p)
        if (value > max) {
            max = value
            maxPiece = p
        }
    }
    check(maxPiece != Piece.Invalid)
    return maxPiece
}

inline fun PieceList.minBy(block: PieceList.(Piece) -> Long): Piece {
    val sizes = sizes
    if (sizes.isEmpty()) {
        throw NoSuchElementException()
    }
    var min = Long.MAX_VALUE
    var minPiece: Piece = Piece.Invalid
    forEach { p ->
        val value = block(p)
        if (value < min) {
            min = value
            minPiece = p
        }
    }
    check(minPiece != Piece.Invalid)
    return minPiece
}

/**
 * @see kotlin.collections.indexOfFirst
 */
inline fun PieceList.pieceIndexOfFirst(predicate: PieceList.(Piece) -> Boolean): Int {
    forEach { p ->
        if (predicate(p)) {
            return p.pieceIndex
        }
    }
    return -1
}

/**
 * @see kotlin.collections.indexOfLast
 */
@OptIn(RawPieceConstructor::class)
inline fun PieceList.pieceIndexOfLast(predicate: PieceList.(Piece) -> Boolean): Int {
    for (i in pieceIndices.reversed()) {
        if (predicate(Piece(i))) {
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
    forEach { p ->
        if (!found && predicate(p)) {
            return@forEach
        }
        found = true
        list.add(p)
    }
    return list
}

/**
 * @see kotlin.collections.takeWhile
 */
inline fun PieceList.takeWhile(predicate: PieceList.(Piece) -> Boolean): List<Piece> {
    val list = mutableListOf<Piece>()
    forEach { p ->
        if (predicate(p)) {
            list.add(p)
        } else {
            return list
        }
    }
    return list
}

/**
 * @see kotlin.collections.binarySearch
 */
@OptIn(RawPieceConstructor::class)
inline fun PieceList.binarySearch(
    fromIndex: Int = 0,
    toIndex: Int = count,
    comparator: PieceList.(Piece) -> Int,
): Int {
    // TODO: check, this is written by Copilot
    var low = createPieceByListIndexUnsafe(fromIndex).pieceIndex
    var high = createPieceByListIndexUnsafe(toIndex - 1).pieceIndex
    while (low <= high) {
        val mid = (low + high).ushr(1)
        val cmp = comparator(Piece(mid))
        when {
            cmp < 0 -> low = mid + 1
            cmp > 0 -> high = mid - 1
            else -> return mid // key found
        }
    }
    return -1
}

