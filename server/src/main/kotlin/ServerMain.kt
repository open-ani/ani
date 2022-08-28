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

package me.him188.animationgarden.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import java.io.File


object ServerMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val dataFolder = File(System.getProperty("user.dir"))
        val data = dataFolder.resolve("data")
//        val settings = dataFolder.resolve("settings")
        embeddedServer(Netty) {
            routing {
                route("/data/{token}") {
                    get {
                        val file = data.resolve(call.parameters.getOrFail("token"))
                        if (file.exists()) {
                            call.respond(HttpStatusCode.OK, file.readText())
                        } else {
                            call.respond(HttpStatusCode.NoContent)
                        }
                    }

                    put {
                        val file = data.resolve(call.parameters.getOrFail("token"))
                        file.writeText(call.receiveText())
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }.start(wait = true)
    }
}