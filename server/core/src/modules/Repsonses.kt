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

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import me.him188.animationgarden.shared.dto.ApiResponse

suspend fun ApplicationCall.respondCommon() {
    respond(ApiResponse(true, "OK"))
}

suspend inline fun <reified T> ApplicationCall.respondSuccess(
    data: T,
    message: String? = null,
    httpStatusCode: HttpStatusCode = HttpStatusCode.OK,
) {
    return respond(httpStatusCode, ApiResponse(true, message, data))
}

suspend inline fun ApplicationCall.respondError(
    message: String? = null,
    httpStatusCode: HttpStatusCode = HttpStatusCode.BadRequest,
) {
    return respond(httpStatusCode, ApiResponse(false, message))
}