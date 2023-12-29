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

package me.him188.animationgarden.server.modules

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import me.him188.animationgarden.server.database.Users
import me.him188.animationgarden.shared.dto.AuthRequest
import me.him188.animationgarden.shared.dto.LoginResponse
import me.him188.animationgarden.shared.dto.RegisterRequest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private typealias MyUserIdPrincipal = UserIdPrincipal

fun ApplicationCall.userId(): String = userIdOrNull()
fun ApplicationCall.userIdOrNull(): String = principal<MyUserIdPrincipal>()?.name ?: error("UserIdPrincipal not found")

class AuthModule : KoinComponent, KtorModule {
    private val users: Users by inject()

    override fun Application.install() {
        install(Authentication) {
            bearer {
                authenticate { tokenCredential ->
                    users.getUserIdByToken(tokenCredential.token)
                        ?.let { MyUserIdPrincipal(it) }
                }
            }
        }

        routing {
            route("auth") {
                authRoutes()
            }
        }
    }

    private fun Route.authRoutes() {
        post("register") {
            val request = call.receive<RegisterRequest>()
            when (val resp = users.createUser(request.username, request.password)) {
                Users.CreateUserResult.InvalidPassword -> call.respondError("Invalid password")
                Users.CreateUserResult.InvalidUsername -> call.respondError("Invalid username")
                Users.CreateUserResult.UsernameAlreadyExists -> call.respondError("Username already exists")
                is Users.CreateUserResult.Success -> {
                    call.respondSuccess(
                        LoginResponse(
                            resp.token.token,
                            resp.token.expireTimestamp
                        )
                    )
                }
            }
        }

        post("login") {
            val request = call.receive<AuthRequest>()
            when (val resp = users.login(request.username, request.password)) {
                is Users.LoginResult.Success -> {
                    call.respondSuccess(
                        LoginResponse(
                            resp.token.token,
                            resp.token.expireTimestamp
                        )
                    )
                }

                Users.LoginResult.UserNotFound -> call.respondError("User not found")
            }
        }

        authenticate {
            get("me") {
                val info = users.getUserInfo(call.userId())
                call.respondSuccess(info)
            }
        }
    }
}
