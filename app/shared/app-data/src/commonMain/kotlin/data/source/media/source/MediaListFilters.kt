package me.him188.ani.app.data.source.media.source

import me.him188.ani.datasources.api.topic.contains

/**
 * 常用的过滤器
 *
 * @see MediaListFilter
 */
object MediaListFilters {
    val ContainsSubjectName = BasicMediaListFilter { media ->
        subjectNamesNoSpace.any { subjectName ->
            media.originalTitle.replace(" ", "")
                .contains(subjectName, ignoreCase = true)
        }
    }

    val ContainsEpisodeSort = BasicMediaListFilter { media ->
        val range = media.episodeRange ?: return@BasicMediaListFilter false
        range.contains(episodeSort)
    }
}
