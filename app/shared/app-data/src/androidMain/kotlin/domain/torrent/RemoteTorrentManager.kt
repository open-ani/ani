/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.torrent

import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.utils.io.SystemPath

/**
 * Android 远程 BT 管理器, 通过 AIDL IPC 与 AniTorrentService 通信
 */
class RemoteTorrentManager(
    settingsRepository: SettingsRepository,
    baseSaveDir: () -> SystemPath
) : TorrentManager {
    override val engines: List<TorrentEngine> = listOf()
}