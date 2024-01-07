package me.him188.ani.datasources.api


data class Paged<T>(
    val total: Int,
    val hasMore: Boolean,
    val page: List<T>,
)

inline fun <T, R> Paged<T>.map(block: (T) -> R): Paged<R> {
    return Paged(
        total = total,
        hasMore = hasMore,
        page = page.map(block),
    )
}