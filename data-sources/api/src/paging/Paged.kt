package me.him188.ani.datasources.api.paging


data class Paged<T>(
    val total: Int?,
    val hasMore: Boolean,
    val page: List<T>,
) {
    companion object {
        fun <T> empty(): Paged<T> = Paged(null, false, emptyList())
    }
}

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
            data
        )
    }
