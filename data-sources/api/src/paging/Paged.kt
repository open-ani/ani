package me.him188.ani.datasources.api.paging


/**
 * 分页数据的一页.
 */
data class Paged<T>(
    /**
     * 总共的数据数量 (不是本页数据数量), 可能为 `null` 表示未知.
     */
    val total: Int?,
    /**
     * 是否还有更多页数
     */
    val hasMore: Boolean,
    /**
     * 本页数据, 可能为空, 为空时 [hasMore] 一定为 `false`.
     */
    val page: List<T>,
) {
    companion object {
        /**
         * 表示已经没有更多页数了
         */
        fun <T> empty(): Paged<T> = Paged(null, false, emptyList())
    }
}

/**
 * 根据一页数据 [list] 创建一个 [Paged] 对象, 当 [list] 为空时, 表示没有更多页数.
 */
fun <T> Paged(list: List<T>): Paged<T> {
    return Paged(null, hasMore = list.isNotEmpty(), list)
}

inline fun <T, R> Paged<T>.map(block: (T) -> R): Paged<R> {
    return Paged(
        total = total,
        hasMore = hasMore,
        page = page.map(block),
    )
}

fun <T> Paged.Companion.processPagedResponse(total: Int?, pageSize: Int, data: List<T>?) =
    if (data == null) {
        Paged(total, false, emptyList())
    } else {
        Paged(
            total,
            data.isNotEmpty() && data.size >= pageSize,
            data,
        )
    }
