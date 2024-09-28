/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

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
        add(MediaListFilters.ContainsAnyEpisodeInfo)
    }

    val context = MediaListFilterContext(
        setOf("条目名称"), EpisodeSort(1), EpisodeSort(1),
        "第1集",
    )
    val newList: List<Media> = with(context) {
        mediaList.filter { media ->
            filters.applyOn(media.asCandidate())
        }
    }
}
