package me.him188.ani.app.update

import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.platform.Platform
import java.awt.Desktop
import java.io.File
import kotlin.system.exitProcess

interface DesktopUpdateInstaller : UpdateInstaller {
    companion object {
        fun currentOS(): DesktopUpdateInstaller {
            return when (Platform.currentPlatform) {
                is Platform.Linux -> throw UnsupportedOperationException("Linux is not supported")
                is Platform.MacOS -> MacOSUpdateInstaller
                is Platform.Windows -> WindowsUpdateInstaller
                Platform.Android -> throw IllegalStateException("Android is not a desktop OS")
            }
        }
    }
}

object MacOSUpdateInstaller : DesktopUpdateInstaller {
    override fun install(file: File, context: ContextMP) {
        Desktop.getDesktop().open(file)
        exitProcess(0)
    }
}

object WindowsUpdateInstaller : DesktopUpdateInstaller {
    override fun install(file: File, context: ContextMP) {
    }
}
