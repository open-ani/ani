/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.update

import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.utils.io.SystemPath
import me.him188.ani.utils.io.delete
import me.him188.ani.utils.io.exists
import me.him188.ani.utils.io.inSystem
import me.him188.ani.utils.io.list
import me.him188.ani.utils.io.name
import me.him188.ani.utils.io.resolveSibling
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent

class UpdateManager(
    val saveDir: SystemPath,
) : KoinComponent {
    companion object {
        private val logger = logger<UpdateManager>()
    }

    /**
     * 如果此版本与 [file] 版本相同, 则删除 [file]
     */
    fun deleteInstalled(file: SystemPath, currentVersion: String) {
        if (file.name.contains(currentVersion)) {
            file.delete()
        }
    }

    fun deleteInstaller(file: SystemPath) {
        file.delete()
        file.resolveSibling(file.name + ".sha256").delete()
    }

    fun deleteInstalledFiles() {
        if (!saveDir.exists()) return
        saveDir.list().forEach {
            val version = currentAniBuildConfig.versionName
            if (it.name.contains(version)) {
                logger.info { "Deleting old installer file because it matches current version ${version}: $it" }
                deleteInstaller(it.inSystem)
            }
        }
    }
}
