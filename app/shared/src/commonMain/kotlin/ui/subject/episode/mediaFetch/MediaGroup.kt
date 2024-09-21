/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import me.him188.ani.datasources.api.Media

@Immutable // only after build
class MediaGroup @MediaGroupBuilderApi internal constructor(
    val groupId: MediaGroupId,
) {
    // 原地 build, 节约内存
    private val _list: ArrayList<Media> = ArrayList(4)
    val list: List<Media> get() = _list

    @Stable
    val first get() = list.first()

    @MediaGroupBuilderApi
    internal fun add(media: Media) {
        _list.add(media)
    }
}

internal typealias MediaGroupId = String

@RequiresOptIn
internal annotation class MediaGroupBuilderApi

@OptIn(MediaGroupBuilderApi::class)
object MediaGrouper {
    fun getGroupId(media: Media): String {
        var title = media.originalTitle
        if (title.startsWith('[')) {
            title = title.substringAfter(']')
        }
        return title
    }

    fun getItemIdWithinGroup(media: Media): String {
        val alliance = media.properties.alliance
        if (alliance.isNotEmpty()) return alliance

        val title = media.originalTitle
        if (title.startsWith('[')) {
            val index = title.indexOf(']')
            if (index != -1) {
                return title.substring(1, index)
            }
        }

        return title
    }

    fun buildGroups(list: List<Media>): List<MediaGroup> {
        val groups = HashMap<String, MediaGroup>()
        for (media in list) {
            val groupId = getGroupId(media)
            groups.getOrPut(groupId) { MediaGroup(groupId) }
                .add(media)
        }
        return groups.values.toList()
    }
}
