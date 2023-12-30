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
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import me.him188.animationgarden.datasources.api.DownloadProvider
import me.him188.animationgarden.datasources.api.DownloadSearchQuery
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SearchModule : KtorModule, KoinComponent {
    private val downloadProvider: DownloadProvider by inject()

    override fun Application.install() {
        routing {
            authentication {
                route()
            }
        }
    }

    private fun RootRoute.route() {
        webSocket("search/{name}") {
            val name = call.parameters.getOrFail("name")
            val session = downloadProvider.startSearch(
                DownloadSearchQuery(
                    keywords = name,
                )
            )

            while (true) {
                val page = session.nextPageOrNull() ?: close()
                sendSerialized(page)
            }
        }
    }
}