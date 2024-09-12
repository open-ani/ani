package me.him188.ani.datasources.bangumi.models


import kotlinx.serialization.Serializable

/**
 * - `0`: 未收藏 - `1`: 想看 - `2`: 看过 - `3`: 抛弃
 *
 * Values: _1,_2,_3
 */
@Serializable(with = BangumiEpisodeCollectionTypeAsInt::class)
enum class BangumiEpisodeCollectionType(val value: Int) {
    NOT_COLLECTED(0),
    WATCHLIST(1),
    WATCHED(2),
    DISCARDED(3);

    /**
     * Override [toString()] to avoid using the enum variable name as the value, and instead use
     * the actual value defined in the API spec file.
     *
     * This solves a problem when the variable name and its value are different, and ensures that
     * the client sends the correct enum values to the server always.
     */
    override fun toString(): String = value.toString()
}

private object BangumiEpisodeCollectionTypeAsInt : EnumValueSerializer<BangumiEpisodeCollectionType>(
    "BangumiEpisodeCollectionType", BangumiEpisodeCollectionType.entries, { it.value },
)
