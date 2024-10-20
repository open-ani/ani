/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.torrent

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.models.preference.AnitorrentConfig
import me.him188.ani.app.data.models.preference.ProxySettings
import me.him188.ani.app.data.models.preference.TorrentPeerConfig
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.domain.torrent.engines.AnitorrentEngine
import me.him188.ani.utils.coroutines.childScope
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.resolve
import kotlin.coroutines.CoroutineContext

class DesktopTorrentManager(
    parentCoroutineContext: CoroutineContext,
    private val saveDir: (type: TorrentEngineType) -> SystemPath,
    private val proxySettingsFlow: Flow<ProxySettings>,
    private val anitorrentConfigFlow: Flow<AnitorrentConfig>,
    private val peerFilterConfig: Flow<TorrentPeerConfig>,
) : TorrentManager {
    private val scope = parentCoroutineContext.childScope()

    private val anitorrent: AnitorrentEngine by lazy {
        AnitorrentEngine(
            anitorrentConfigFlow,
            proxySettingsFlow,
            peerFilterConfig,
            saveDir(TorrentEngineType.Anitorrent),
            scope.coroutineContext + CoroutineName("AnitorrentEngine"),
        )
    }

    override val engines: List<TorrentEngine> by lazy {
        // 注意, 是故意只启用一个下载器的, 因为每个下载器都会创建一个 DirectoryMediaCacheStorage
        // 并且使用相同的 mediaSourceId: MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID.
        // 搜索数据源时会使用 mediaSourceId 作为 map key, 导致总是只会用一个 storage.
        // 
        // 如果要支持多个, 需要考虑将所有 storage 合并成一个 MediaSource.

        listOf(anitorrent)
    }

    companion object {
        fun create(
            parentCoroutineContext: CoroutineContext,
            settingsRepository: SettingsRepository,
            baseSaveDir: () -> SystemPath,
        ): DesktopTorrentManager {
            val saveDirLazy by lazy(baseSaveDir)
            return DesktopTorrentManager(
                parentCoroutineContext,
                { type ->
                    saveDirLazy.resolve(type.id)
                },
                settingsRepository.proxySettings.flow,
                settingsRepository.anitorrentConfig.flow,
                settingsRepository.torrentPeerConfig.flow
            )
        }
    }
}