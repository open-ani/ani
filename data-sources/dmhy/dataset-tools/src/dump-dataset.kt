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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.model.SearchQuery
import kotlin.system.exitProcess

suspend fun main() {
    val client = AnimationGardenClient.Factory.create {
    }

    val session = client.startSearchSession(
        SearchQuery(
            keywords = "药师少女的独语",
        )
    )

    session.results.toList()
    session.results.first().let {
        println(it.rawTitle)
        println(it.details)
    }
    exitProcess(0)
}