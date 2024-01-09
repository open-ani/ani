/*
 * Ani
 * Copyright (C) 2022-2024 Him188
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

package me.him188.ani.datasources.dmhy

import io.ktor.client.HttpClientConfig
import me.him188.ani.datasources.api.DownloadProvider
import me.him188.ani.datasources.api.DownloadSearchQuery
import me.him188.ani.datasources.api.SearchSession
import me.him188.ani.datasources.api.topic.Topic
import me.him188.ani.datasources.dmhy.impl.DmhyClientImpl


interface DmhyClient {
    fun startSearchSession(filter: DownloadSearchQuery): SearchSession<Topic>

    companion object Factory {
        fun create(engineConfig: HttpClientConfig<*>.() -> Unit): DmhyClient =
            DmhyClientImpl(engineConfig)
    }
}


class DmhyDownloadProvider(
    private val client: DmhyClient = DmhyClient.create { },
) : DownloadProvider {
    override val id: String get() = "dmhy"

    override suspend fun startSearch(query: DownloadSearchQuery): SearchSession<Topic> {
        return client.startSearchSession(query)
    }
}