/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.tools.ldc

/**
 * @see addFirst
 * @see addLast
 * @see removeAll
 * @see removeFirstOrNull
 */
abstract class LazyDataCacheMutator<T> {
    abstract suspend fun update(
        map: (List<T>) -> List<T>,
    )
//
//    inline fun List<T>.mutate(
//        where: (T) -> Boolean,
//        map: T.() -> T,
//    ): List<T> = buildList(this.size) {
//        for (item in this@mutate) {
//            add(if (where(item)) map(item) else item)
//        }
//    }

    // helper functions
}

object MutationContext {
    inline fun <T> List<T>.replaceAll(
        where: (T) -> Boolean,
        map: T.() -> T,
    ): List<T> = map { if (where(it)) map(it) else it }
}

/**
 * Changes one element of the [LazyDataCache.cachedDataFlow] matching [where].
 */
suspend inline fun <T> LazyDataCacheMutator<T>.setEach(
    crossinline where: (T) -> Boolean,
    crossinline map: T.() -> T,
) {
    update { data ->
        data.map { if (where(it)) map(it) else it }
    }
}

suspend inline fun <T> LazyDataCacheMutator<T>.addFirst(
    item: T,
) {
    update { data ->
        buildList(data.size + 1) { // more performant 
            add(item)
            addAll(data)
        }
    }
}

suspend inline fun <T> LazyDataCacheMutator<T>.addLast(
    item: T,
) {
    update { data ->
        data + item
    }
}

suspend inline fun <T> LazyDataCacheMutator<T>.removeAll(
    crossinline where: (T) -> Boolean,
) {
    update { data ->
        data.filterNot(where)
    }
}

suspend inline fun <T> LazyDataCacheMutator<T>.removeFirstOrNull(
    crossinline where: (T) -> Boolean,
): T? {
    var removed: T? = null
    update { data ->
        buildList(data.size) {
            for (datum in data) {
                if (removed == null && where(datum)) {
                    removed = datum
                } else {
                    add(datum)
                }
            }
        }
    }
    return removed
}
