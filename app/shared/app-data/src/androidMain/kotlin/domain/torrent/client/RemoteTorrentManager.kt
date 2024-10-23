/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.torrent.client

import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.models.preference.AnitorrentConfig
import me.him188.ani.app.data.models.preference.ProxySettings
import me.him188.ani.app.data.models.preference.TorrentPeerConfig
import me.him188.ani.app.domain.torrent.TorrentEngine
import me.him188.ani.app.domain.torrent.TorrentEngineType
import me.him188.ani.app.domain.torrent.TorrentManager
import me.him188.ani.app.domain.torrent.service.TorrentServiceConnection
import me.him188.ani.utils.coroutines.childScope
import me.him188.ani.utils.io.SystemPath
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

/**
 * Android 远程 BT 管理器, 通过 AIDL IPC 与 AniTorrentService 通信
 */
class RemoteTorrentManager(
    parentCoroutineContext: CoroutineContext,
    private val saveDir: (type: TorrentEngineType) -> SystemPath,
    private val proxySettingsFlow: Flow<ProxySettings>,
    private val anitorrentConfigFlow: Flow<AnitorrentConfig>,
    private val peerFilterConfig: Flow<TorrentPeerConfig>,
    
    private val serviceConnectionRef: WeakReference<TorrentServiceConnection>
) : TorrentManager {
    private val scope = parentCoroutineContext.childScope()
    
    private var remoteEngine: RemoteTorrentEngine? = null
    
    override val engines: List<TorrentEngine>
        get() {
            val currentRemoteEngine = remoteEngine
            if (currentRemoteEngine != null) return listOf(currentRemoteEngine)
            
            val newRemoteEngine = RemoteTorrentEngine(serviceConnectionRef)
            remoteEngine = newRemoteEngine
            return listOf(newRemoteEngine)
        }
}