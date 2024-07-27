package me.him188.ani.datasources.api.source

import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.contains

/**
 * A media matched from the source.
 */
data class MediaMatch(
    val media: Media,
    val kind: MatchKind,
)

/**
 * 判断该 [MediaMatch] 是否满足条件 [request].
 *
 * 返回 `null` 表示条件不足以判断. 届时可以根据数据源大致的准确性或者其他信息考虑是否需要在 [MediaSource.fetch] 的返回中包含此资源.
 *
 * 该函数会在如下情况下返回 `null`:
 * - 当 [Media.episodeRange] 为 `null` 时. 这意味着无法知道该资源的剧集范围.
 */
fun MediaMatch.matches(request: MediaFetchRequest): Boolean? {
    val actualEpRange = this.media.episodeRange ?: return null
    val expectedEp = request.episodeEp
    return !(request.episodeSort !in actualEpRange && (expectedEp == null || expectedEp !in actualEpRange))
}

/**
 * 当且仅当该资源一定满足请求时返回 `true`. 若条件不足, 返回 `false`.
 */
fun MediaMatch.definitelyMatches(request: MediaFetchRequest): Boolean = matches(request) == true

enum class MatchKind {
    /**
     * The request has an exact match with the cache.
     * Usually because episode id is the same.
     */
    EXACT,

    /**
     * The request does not have a [EXACT] match but a [FUZZY] one.
     *
     * This is done on a best-effort basis where they can be false positives.
     */
    FUZZY,
}
