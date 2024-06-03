package me.him188.ani.app.update

import androidx.compose.runtime.Stable
import me.him188.ani.app.platform.ContextMP
import me.him188.ani.app.platform.Platform
import java.io.File

/**
 * 安装包安装器
 *
 * - 安卓：弹出系统 APK 安装界面
 * - Windows：使用脚本自动覆盖安装 （一键）
 * - macOS：打开 dmg 让系统去安装，需要用户手动拖拽一下
 */
interface UpdateInstaller {
    /**
     * 如果 [install] 可能返回 [InstallationResult.Failed], 则需实现
     */
    fun openForManualInstallation(file: File, context: ContextMP) {}

    fun install(file: File, context: ContextMP): InstallationResult
}

sealed class InstallationResult {
    data object Succeed : InstallationResult() // 实际上可能不会返回, 因为安装成功会重启

    /**
     * 安装失败, 附带失败原因. UI 会展示这个失败原因
     */
    data class Failed(val reason: InstallationFailureReason) : InstallationResult()
}

enum class InstallationFailureReason {
    /**
     * 未支持的安装目录结构. 例如 Windows 上未找到 `Ani.exe`
     */
    UNSUPPORTED_FILE_STRUCTURE,
}

@Stable
val Platform.supportsInAppUpdate: Boolean
    get() = when (this) {
        is Platform.Desktop -> true
        Platform.Android -> false
    }
