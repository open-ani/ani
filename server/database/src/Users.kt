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

package me.him188.animationgarden.server.database

import me.him188.animationgarden.shared.dto.UserInfo

data class UserToken(
    val token: String,
    val expireTimestamp: Long,
)

typealias UserId = String

interface Users {
    sealed interface CreateUserResult {
        data class Success(val token: UserToken) : CreateUserResult // also logs you in!
        data object UsernameAlreadyExists : CreateUserResult
        data object InvalidUsername : CreateUserResult
        data object InvalidPassword : CreateUserResult
    }

    suspend fun createUser(username: String, password: String): CreateUserResult


    sealed interface LoginResult {
        data class Success(val token: UserToken) : LoginResult
        data object UserNotFound : LoginResult
    }

    suspend fun login(username: String, password: String): LoginResult


    suspend fun getUserIdByToken(token: String): UserId?

    suspend fun getUserInfo(userId: UserId): UserInfo?
}
