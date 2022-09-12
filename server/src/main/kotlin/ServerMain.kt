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
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.websocket.*
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ParsingException
import kotlinx.cli.default
import kotlinx.serialization.json.Json
import me.him188.animationgarden.api.model.CommitsModule
import java.io.File


object ServerMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val parser = ArgParser("AnimationGarden", useDefaultHelpShortName = false)
        val dataDir by parser.option(
            ArgType.String,
            fullName = "data-dir",
            shortName = "d",
            description = "Data directory"
        ).default(File(System.getProperty("user.dir"), "data").absolutePath)
        val port by parser.option(
            ArgType.Port,
            fullName = "port",
            shortName = "p",
            description = "Port number"
        ).default(6428)
        val host by parser.option(
            ArgType.String,
            fullName = "host",
            shortName = "h",
            description = "Listen host"
        ).default("0.0.0.0")
        parser.parse(args)


        val dataFolder = File(dataDir)
        dataFolder.mkdir()
        println("Data folder: ${dataFolder.absolutePath}")

        embeddedServer(Netty, port = port, host = host) {
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


val ArgType.Companion.Port get() = PortType

object PortType : ArgType<Int>(true) {
    override val description: kotlin.String
        get() = ArgType.Int.description

    override fun convert(value: kotlin.String, name: kotlin.String): kotlin.Int =
        value.toIntOrNull()?.takeIf { it in 0..65535 }
            ?: throw ParsingException("Option $name is expected to be integer number and within the range 0..65535. $value is provided.")
}

//
//private fun defaultFormat(call: ApplicationCall): String =
//    when (val status = call.response.status() ?: "Unhandled") {
//        HttpStatusCode.Found -> "${status}: " +
//                "${call.request.toLogStringWithColors()} -> ${call.response.headers[HttpHeaders.Location]}"
//        "Unhandled" -> "${status}: ${call.request.toLogStringWithColors()}"
//        else -> "${status}: ${call.request.toLogStringWithColors()}"
//    }
//
//internal fun ApplicationRequest.toLogStringWithColors(): String =
//    "${httpMethod.value} - ${path()}"
