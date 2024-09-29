/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.source.media.source

import me.him188.ani.datasources.api.topic.contains

/**
 * 常用的过滤器
 *
 * @see MediaListFilter
 */
object MediaListFilters {
    val specialCharRegex = Regex("""[ 	~!@#$%^&*()_+{}\[\]\\|;':",.<>/?【】：～「」]""")

    val ContainsSubjectName = BasicMediaListFilter { media ->
        subjectNamesWithoutSpecial.any { subjectName ->
            media.originalTitle.replace(specialCharRegex, "")
                .contains(subjectName, ignoreCase = true)
        }
    }

    val ContainsEpisodeSort = BasicMediaListFilter { media ->
        val range = media.episodeRange ?: return@BasicMediaListFilter false
        range.contains(episodeSort)
    }
    val ContainsEpisodeEp = BasicMediaListFilter { media ->
        val range = media.episodeRange ?: return@BasicMediaListFilter false
        episodeEp != null && range.contains(episodeEp)
    }
    val ContainsEpisodeName = BasicMediaListFilter { media ->
        episodeName ?: return@BasicMediaListFilter false
        val name = episodeNameWithoutSpecial
        checkNotNull(name)
        if (name.isBlank()) return@BasicMediaListFilter false
        media.originalTitle.replace(specialCharRegex, "").contains(name)
    }

    val ContainsAnyEpisodeInfo = ContainsEpisodeSort or ContainsEpisodeName or ContainsEpisodeEp
}
