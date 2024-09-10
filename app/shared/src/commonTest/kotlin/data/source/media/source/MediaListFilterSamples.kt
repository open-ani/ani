@file:Suppress("unused", "UNUSED_VARIABLE")

package me.him188.ani.app.data.source.media.source

import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media

fun applyingFilters(mediaList: List<Media>) {
    // 执行过滤器

    val enableFilterBySubjectName = true
    val filters = buildList {
        // 把更容易过滤掉东西的过滤器放在前面, 增加匹配效率

        // 根据配置判断是否需要启用某项过滤
        if (enableFilterBySubjectName) {
            add(MediaListFilters.ContainsSubjectName)
        }
        add(MediaListFilters.ContainsEpisodeSort)
    }

    val newList: List<Media> = with(MediaListFilterContext(setOf("条目名称"), EpisodeSort(1))) {
        mediaList.filter { media ->
            filters.applyOn(media.asCandidate())
        }
    }
}
