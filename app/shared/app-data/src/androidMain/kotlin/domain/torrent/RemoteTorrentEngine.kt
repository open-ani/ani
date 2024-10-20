/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.torrent

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.domain.torrent.service.TorrentServiceConnection
import me.him188.ani.app.torrent.api.TorrentDownloader
import me.him188.ani.datasources.api.source.MediaSourceLocation
import java.lang.ref.WeakReference

class RemoteTorrentEngine(
    private val connection: WeakReference<TorrentServiceConnection>
) : TorrentEngine {
    override val type: TorrentEngineType = TorrentEngineType.RemoteAnitorrent

    override val isSupported: Flow<Boolean> 
        get() = connection.get()?.connected ?: flowOf(false)

    override val location: MediaSourceLocation = MediaSourceLocation.Local

    override suspend fun testConnection(): Boolean {
        return connection.get()?.connected?.value ?: false
    }
    
    override suspend fun getDownloader(): TorrentDownloader {
        TODO("Not yet implemented")
    }


    override fun close() {
        TODO("Not yet implemented")
    }
}