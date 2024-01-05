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

import me.him188.animationgarden.datasources.bangumi.BangumiClient
import me.him188.animationgarden.datasources.bangumi.BangumiSubjectType
import kotlin.system.exitProcess

suspend fun main() {
    val client = BangumiClient.create()
    client.subjects.getSubjectById(400602)?.infobox?.forEach {
        println("${it.key}=${it.value}")
    }
    exitProcess(0)

    val res = client.subjects.searchSubjectByKeywords(
        "药屋",
        types = listOf(BangumiSubjectType.ANIME),
//        sort = BangumiSort.MATCH,
    )
    println(res?.map { it.name })

}