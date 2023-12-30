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
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import me.him188.animationgarden.datasources.api.NameIndexProvider
import me.him188.animationgarden.datasources.api.NameIndexSearchQuery
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NameIndexModule : KtorModule, KoinComponent {
    private val nameIndexProvider: NameIndexProvider by inject()

    override fun Application.install() {
        routing {
            get("search/{query}") {
                val query = call.parameters["query"] ?: return@get call.respondError("Missing query")
                val search = nameIndexProvider.startSearch(
                    NameIndexSearchQuery(
                        keyword = query,
                    )
                )
                call.respondSuccess(search.results.toList()) // List<SubjectIndex>
            }
        }
    }
}