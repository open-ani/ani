package me.him188.ani.app.data.update

import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.core.component.KoinComponent
import java.io.File

class UpdateManager(
    val saveDir: File,
) : KoinComponent {
    companion object {
        private val logger = logger<UpdateManager>()
    }

    /**
     * 如果此版本与 [file] 版本相同, 则删除 [file]
     */
    fun deleteInstalled(file: File, currentVersion: String) {
        if (file.name.contains(currentVersion)) {
            file.delete()
        }
    }

    fun deleteInstaller(file: File) {
        file.delete()
        file.resolveSibling(file.name + ".sha256").delete()
    }

    fun deleteInstalledFiles() {
        if (!saveDir.exists()) return
        saveDir.listFiles()?.forEach {
            val version = currentAniBuildConfig.versionName
            if (it.name.contains(version)) {
                logger.info { "Deleting old installer file because it matches current version ${version}: $it" }
                deleteInstaller(it)
            }
        }
    }
}
