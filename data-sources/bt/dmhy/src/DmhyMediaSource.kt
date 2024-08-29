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

import kotlinx.coroutines.CancellationException
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.DownloadSearchQuery
import me.him188.ani.datasources.api.source.FactoryId
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceInfo
import me.him188.ani.datasources.api.source.TopicMediaSource
import me.him188.ani.datasources.api.source.useHttpClient
import me.him188.ani.datasources.api.topic.Topic
import me.him188.ani.datasources.dmhy.impl.DmhyPagedSourceImpl
import me.him188.ani.datasources.dmhy.impl.protocol.Network
import me.him188.ani.utils.logging.error

class DmhyMediaSource(
    config: MediaSourceConfig,
) : TopicMediaSource() {
    class Factory : MediaSourceFactory {
        override val factoryId: FactoryId = FactoryId(ID)
        override val info: MediaSourceInfo get() = INFO
        override fun create(mediaSourceId: String, config: MediaSourceConfig): MediaSource = DmhyMediaSource(config)
    }

    companion object {
        const val ID = "dmhy"
        val INFO = MediaSourceInfo(
            displayName = "動漫花園",
            description = "动漫资源聚合网站",
            iconUrl = "https://dmhy.org/favicon.ico",
            iconResourceId = "dmhy.png",
        )
    }

    override val info: MediaSourceInfo get() = INFO
    private val network by lazy {
        Network(useHttpClient(config))
    }

    override val mediaSourceId: String get() = ID

    override suspend fun checkConnection(): ConnectionStatus {
        return try {
            network.list()
            ConnectionStatus.SUCCESS
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Failed to check connection" }
            ConnectionStatus.FAILED
        }
    }

    override suspend fun startSearch(query: DownloadSearchQuery): PagedSource<Topic> {
        return DmhyPagedSourceImpl(query, network)
    }
}
