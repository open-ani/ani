/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

package me.him188.ani.app.ui.exploration.search

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import me.him188.ani.app.ui.exploration.search.Role.Companion.AnimeProduction
import me.him188.ani.app.ui.exploration.search.Role.Companion.Director
import me.him188.ani.app.ui.exploration.search.Role.Companion.Music
import me.him188.ani.app.ui.exploration.search.Role.Companion.Script
import kotlin.jvm.JvmInline

@Immutable
@JvmInline
value class RoleSet(
    @PublishedApi internal val list: Int,
) {
    inline operator fun plus(role: Role): RoleSet = RoleSet(list or role.value)
    inline operator fun plus(role: RoleSet): RoleSet = RoleSet(list or role.list)
    inline operator fun minus(role: Role): RoleSet = RoleSet(list and role.value.inv())
    inline operator fun contains(role: Role): Boolean = list and role.value != 0

    inline fun forEach(crossinline action: (Role) -> Unit) {
        Role.forEachRole {
            if (it in this) action(it)
        }
    }

    @Stable
    companion object {
        @Stable
        val Empty = RoleSet(0)

        @Stable
        val Default = Empty + AnimeProduction + Director + Script + Music
    }
}

@JvmInline
@Immutable
value class Role(
    @PublishedApi internal val value: Int,
) {
    @Stable
    companion object {
        /**
         * 动画制作
         */
        val AnimeProduction: Role = Role(1 shl 1)

        /**
         * 原作
         */
        val OriginalAuthor: Role = Role(1 shl 2)

        /**
         * 监督
         */
        val Director: Role = Role(1 shl 3)

        /**
         * 脚本, 编剧, 系列构成
         */
        val Script: Role = Role(1 shl 4)
        val Music: Role = Role(1 shl 5)
        val CharacterDesign: Role = Role(1 shl 6)
        val ArtDesign: Role = Role(1 shl 7)
        val AnimationDirector: Role = Role(1 shl 8)

        // NOTE! If you add new roles, make sure to update `RoleSet.forEach`.

        /*
                    "动画制作",
                    "原作",
                    "监督", "导演",
                    "脚本", "编剧",
                    "音乐",
                    "人设", "人物设定",
                    "系列构成",
                    "美术设计",
                    "动作作画监督",
         */

        fun forEachRole(action: (Role) -> Unit) {
            action(AnimeProduction)
            action(OriginalAuthor)
            action(Director)
            action(Script)
            action(Music)
            action(CharacterDesign)
            action(ArtDesign)
            action(AnimationDirector)
        }
    }
}