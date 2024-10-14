/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.search

import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.source.ConnectionStatus

/**
 * 提供番剧名称索引的数据源. 支持使用关键字搜索正式名称.
 */
interface SubjectProvider {
    /**
     * Unique ID. Can be name of the provider.
     */
    val id: String

    suspend fun testConnection(): ConnectionStatus

    fun startSearch(query: SubjectSearchQuery): PagedSource<SubjectInfo>
}

class SubjectSearchQuery(
    val keyword: String,
    val type: SubjectType = SubjectType.ANIME,
    val useOldSearchApi: Boolean = true,
    val tags: List<String> = listOf(),
    val airDate: Pair<String?, String?> = Pair(null, null),
    val rating: Pair<String?, String?> = Pair(null, null),
    val rank: Pair<String?, String?> = Pair(null, null),
    val nsfw: Boolean? = null,
)

enum class SubjectType {
    ANIME,

    /*
    bangumi supports
            条目类型
            - `1` 为 书籍
            - `2` 为 动画
            - `3` 为 音乐
            - `4` 为 游戏
            - `6` 为 三次元
            
            没有 `5`
     */
}
