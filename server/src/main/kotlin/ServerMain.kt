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

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import me.him188.animationgarden.api.model.CommitsModule
import org.slf4j.event.Level
import java.io.File


object ServerMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val appFolder = File(".")
        appFolder.mkdirs()

        val dataFolder = appFolder.resolve("data")
        dataFolder.mkdir()
        println("Data folder: ${dataFolder.absolutePath}")

        embeddedServer(Netty) {
            install(CallLogging) {
                level = Level.INFO
            }
            install(ContentNegotiation) {
                json(Json {
                    serializersModule = CommitsModule
                    ignoreUnknownKeys = true
                })
            }
            install(WebSockets)
            configureCommitsModule(dataFolder)
        }.start(wait = true)
    }
}