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

package me.him188.animationgarden.shared.dto

import kotlinx.serialization.Serializable

@Serializable
class RegisterRequest(
    val username: String,
    val password: String,
)

@Serializable
class LoginResponse(
    val token: String,
    val expireTimestamp: Long,
)

@Serializable
class AuthRequest(
    val username: String,
    val password: String,
)

@Serializable
class UserInfo(
    val username: String,
    val registeredTimestamp: Long, // timestamp
)