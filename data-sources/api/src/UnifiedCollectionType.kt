package me.him188.ani.datasources.api

/**
 * Unified type for all collection types, also added representation for [NOT_COLLECTED].
 */
enum class UnifiedCollectionType {
    WISH,
    DOING,
    DONE,
    ON_HOLD,
    DROPPED,

    /**
     * The item is not collected.
     *
     * This is a dummy type that will not be returned from the server,
     * as when the item is not collected, the server simply won't return the item.
     *
     * [NOT_COLLECTED] can be useful to represent the "Delete" action to be received as an argument
     * e.g. to update the user's collection type.
     */
    NOT_COLLECTED,
}
