package me.him188.ani.app.tools.update

import me.him188.ani.app.platform.ContextMP
import me.him188.ani.utils.io.SystemPath

object IosUpdateInstaller : UpdateInstaller {
    override fun install(file: SystemPath, context: ContextMP): InstallationResult {
        return InstallationResult.Failed(InstallationFailureReason.UNSUPPORTED_FILE_STRUCTURE)
    }
}