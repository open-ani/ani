package me.him188.ani.app.data.source.media.source

import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.EpisodeRange

/**
 * 一个通用的过滤器, 可被多个数据源实现共享.
 *
 * [MediaListFilters] 中包含了一些常用的过滤器.
 *
 * @sample me.him188.ani.app.data.source.media.source.applyingFilters
 *
 * @param Ctx 所需上下文类型.
 * 最基础的为 [MediaListFilterContext]. 一个 [MediaListFilter] 使用的上下文类型越基础, 就越通用.
 * 例如 [MediaListFilters.ContainsSubjectName] 就使用最基础的上下文, 因此可以用于所有类型数据源.
 *
 * 使用 [BasicMediaListFilter] 可简化类型声明.
 *
 * @see DefaultRssMediaSourceEngine
 */
fun interface MediaListFilter<in Ctx : MediaListFilterContext> {
    /**
     * 一个待被过滤的物品. 通常由你需要过滤的物品转换而来.
     *
     * [asCandidate] 可将 [Media] 转换为 [Candidate].
     */
    interface Candidate {
        val originalTitle: String
        val episodeRange: EpisodeRange?
    }

    /**
     * 过滤一个 [Candidate].
     *
     * 返回 `true` 表示该 [Candidate] 符合过滤条件.
     * 返回 `false` 表示不符合, 将会导致该 [Candidate] 被完全从结果中剔除. Media Selector 中的计数也不会包含它.
     */
    fun Ctx.applyOn(media: Candidate): Boolean
}

typealias BasicMediaListFilter = MediaListFilter<MediaListFilterContext>

// TODO: require context MediaListFilterContext to limit scope
fun Media.asCandidate(): MediaListFilter.Candidate {
    val media = this
    return object : MediaListFilter.Candidate {
        override val originalTitle: String get() = media.originalTitle
        override val episodeRange: EpisodeRange? get() = media.episodeRange
        override fun toString(): String {
            return "Candidate(media=$media)"
        }
    }
}
