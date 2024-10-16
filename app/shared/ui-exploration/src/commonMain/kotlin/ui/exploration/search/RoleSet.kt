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
import me.him188.ani.app.data.models.subject.RelatedPersonInfo
import kotlin.jvm.JvmInline

@Immutable
@JvmInline
value class RoleSet(
    private val delegate: List<Role>,
) {
    operator fun plus(other: RoleSet): RoleSet = RoleSet(delegate + other.delegate)
    operator fun minus(other: RoleSet): RoleSet = RoleSet(delegate - other.delegate.toSet())
    operator fun contains(role: Role): Boolean = role in delegate

    @Stable
    companion object {
        @Stable
        val Empty = RoleSet(emptyList())

        @Stable
        val Default =
            RoleSet(listOf(Role.AnimeProduction, Role.Director, Role.Script, Role.Music))
    }
}

/**
 * 过滤 [RelatedPersonInfo] 中的角色, 返回符合条件的 [RelatedPersonInfo]
 */
fun List<RelatedPersonInfo>.filter(roleSet: RoleSet): Sequence<RelatedPersonInfo> {
    return asSequence().filter f@{ person ->
        val role = Role.matchOrNull(person.relation) ?: return@f false
        role in roleSet
    }
}

@Immutable
enum class Role(vararg val names: String) {
    /**
     * 动画制作
     */
    AnimeProduction("动画制作"),

    /**
     * 原作
     */
    OriginalAuthor("原作"),

    /**
     * 监督
     */
    Director("监督", "导演"),

    /**
     * 脚本, 编剧, 系列构成
     */
    Script("系列构成", "脚本", "编剧"),

    Music("音乐"),

    CharacterDesign("人设", "人物设定"),

    ArtDesign("美术设计"),

    AnimationDirector("动作作画监督"),
    ;

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
    companion object {
        fun matchOrNull(name: String): Role? = entries.firstOrNull { role ->
            role.names.any { it == name }
        }
    }
}
