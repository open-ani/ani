/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.animationgarden.database.impl.xodus

import me.him188.animationgarden.server.database.UserToken
import me.him188.animationgarden.server.database.Users

class UsersImpl : Users {
    override suspend fun createUser(username: String, password: String) {
        TODO("Not yet implemented")
    }

    override suspend fun login(username: String, password: String): UserToken {
        TODO("Not yet implemented")
    }
}