package me.him188.ani.app.data.source.media.source

import me.him188.ani.app.data.source.media.source.MediaListFilter.Candidate
import me.him188.ani.datasources.api.EpisodeSort

/**
 * 包含条目和剧集等查询信息, 用于将 filter 执行.
 *
 * @see MediaListFilter
 */
open class MediaListFilterContext(
    val subjectNames: Set<String>, // faster
    val episodeSort: EpisodeSort,
) {
    val subjectNamesNoSpace: Set<String> by lazy {
        subjectNames.mapTo(HashSet(subjectNames.size)) { it.replace(" ", "") }
    }

    /**
     * 返回 `true` 表示该 [Candidate] 符合过滤条件.
     */
    fun BasicMediaListFilter.applyOn(candidate: Candidate): Boolean =
        this@MediaListFilterContext.applyOn(candidate) // calls MediaListFilter's member function

    /**
     * 返回 `true` 表示该 [Candidate] 符合所有过滤条件.
     */
    fun Iterable<BasicMediaListFilter>.applyOn(candidate: Candidate): Boolean =
        this.all { it.applyOn(candidate) }

    /**
     * 返回 `true` 表示该 [Candidate] 符合所有过滤条件.
     */
    fun Sequence<BasicMediaListFilter>.applyOn(candidate: Candidate): Boolean =
        this.all { it.applyOn(candidate) }
}
