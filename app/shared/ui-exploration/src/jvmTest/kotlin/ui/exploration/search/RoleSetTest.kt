/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.search

import kotlin.reflect.full.memberProperties
import kotlin.test.Test
import kotlin.test.assertEquals

class RoleSetTest {
    @Test
    fun `can get roles by reflection`() {
        assert(getRoles().size > 5)
    }

    @Test
    fun `all Roles can be foreach-ed`() {
        val roles = getRoles().sortedBy { it.value }

        val actual = buildList {
            RoleSet(Int.MAX_VALUE).forEach {
                add(it)
            }
        }.sortedBy { it.value }
        assertEquals(roles, actual)
    }

    @Test
    fun `all Roles do not clash`() {
        val roles = getRoles().sortedBy { it.value }
        assert(roles.distinct().size == roles.size)
    }

    private fun getRoles(): List<Role> {
        val allRoles = Role.Companion::class.memberProperties.mapNotNull {
            val role = it.getter.call(Role)
            role as? Role
        }
        return allRoles
    }
}