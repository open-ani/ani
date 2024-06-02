package me.him188.ani.app.update

import me.him188.ani.app.platform.ContextMP
import java.io.File

interface UpdateInstaller {
    /**
     * 如果 [install] 可能返回 [InstallationResult.Failed], 则需实现
     */
    fun openForManualInstallation(file: File, context: ContextMP) {}
    fun install(file: File, context: ContextMP): InstallationResult
}

sealed class InstallationResult {
    data object Succeed : InstallationResult() // 实际上可能不会返回, 因为安装成功会重启
    data class Failed(val reason: InstallationFailureReason) : InstallationResult()
}

enum class InstallationFailureReason {
    UNSUPPORTED_FILE_STRUCTURE,
}
