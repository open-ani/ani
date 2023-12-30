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

import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.websocket.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.charsets.*
import io.ktor.websocket.*
import io.netty.util.internal.logging.InternalLogger
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ParsingException
import kotlinx.cli.default
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import me.him188.animationgarden.database.impl.xodus.DatabaseModule
import me.him188.animationgarden.datasources.api.DownloadProvider
import me.him188.animationgarden.datasources.api.NameIndexProvider
import me.him188.animationgarden.server.modules.*
import me.him188.animationgarden.utils.logging.info
import me.him188.animationgarden.utils.logging.logger
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.appender.ConsoleAppender
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.seconds

object ServerMain {
    init {
        Configurator.initialize(ConfigurationBuilderFactory.newConfigurationBuilder().apply {
            add(
                newAppender("stdout", "CONSOLE")
                    .addAttribute(
                        "target",
                        ConsoleAppender.Target.SYSTEM_OUT
                    )
                    .add(
                        newLayout("PatternLayout")
                            .addAttribute("pattern", "%d %highlight{%-5p}/%c: %msg%n%throwable")
                            .addAttribute("disableAnsi", "false")
                    )
//                    .add(
//                        newFilter("RegexFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
//                            .addAttribute("regex", regex(""".*io\.netty\..*"""))
//                    )
            )

            val layoutBuilder: LayoutComponentBuilder = newLayout("PatternLayout")
                .addAttribute("pattern", "%d [%t] %-5level: %msg%n")
            val triggeringPolicy = newComponent("Policies")
                .addComponent(newComponent("CronTriggeringPolicy").addAttribute("schedule", "0 0 0 * * ?"))
                .addComponent(newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "100M"))

            add(
                newAppender("rolling", "RollingFile")
                    .addAttribute("fileName", "logs/log.log")
                    .addAttribute("filePattern", "logs/archive/log-%d{MM-dd-yy}.log.gz")
                    .add(layoutBuilder)
                    .addComponent(triggeringPolicy)
            )

            add(
                newLogger("io.netty", Level.OFF)
            )
            add(
                newRootLogger(Level.DEBUG)
                    .add(newAppenderRef("rolling"))
                    .add(newAppenderRef("stdout"))
            )
        }.build())

        InternalLoggerFactory.setDefaultFactory(object : InternalLoggerFactory() {
            override fun newInstance(name: String?): InternalLogger {
                @Suppress("DEPRECATION")
                return Log4J2LoggerFactory().newInstance("io.netty")
            }
        })

    }

    private val logger by lazy { logger() }

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
        logger.info { "Data folder: ${dataFolder.absolutePath}" }

        val json = Json {
            ignoreUnknownKeys = true
        }
        embeddedServer(Netty, port = port, host = host) {
            install(ContentNegotiation) {
                json(json)
            }
            install(WebSockets) {
                pingPeriodMillis = 20.seconds.inWholeMilliseconds
                contentConverter = object : WebsocketContentConverter {
                    override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: Frame): Any? {
                        if (content !is Frame.Text) {
                            return null
                        }
                        return json.decodeFromString(serializer(typeInfo.reifiedType), content.readText())
                    }

                    override suspend fun serialize(charset: Charset, typeInfo: TypeInfo, value: Any?): Frame {
                        return Frame.Text(json.encodeToString(serializer(typeInfo.reifiedType), value as Any))
                    }

                    override fun isApplicable(frame: Frame): Boolean = frame is Frame.Text
                }
            }

            val application = this
            startKoin {
                modules(module {
                    single<Application> { application }
                    single<DownloadProvider> { ServiceLoader.load(DownloadProvider::class.java).first() }
                    single<NameIndexProvider> { ServiceLoader.load(NameIndexProvider::class.java).first() }
                })
                modules(DatabaseModule)
            }

            installModule(AuthModule())
            installModule(ExceptionModule())
            installModule(NameIndexModule())
            installModule(SubscriptionModule())
            installModule(SearchModule())
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
