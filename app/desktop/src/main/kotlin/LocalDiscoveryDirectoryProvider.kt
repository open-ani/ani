package me.him188.ani.app.desktop

import me.him188.ani.app.platform.Arch
import me.him188.ani.app.platform.Platform
import uk.co.caprica.vlcj.factory.discovery.provider.DiscoveryDirectoryProvider
import uk.co.caprica.vlcj.factory.discovery.provider.DiscoveryProviderPriority
import java.io.File

// 在 test-sandbox 运行时使用
class TestDiscoveryDirectoryProvider : DiscoveryDirectoryProvider {
    override fun priority(): Int = DiscoveryProviderPriority.USER_DIR

    override fun directories(): Array<String> {
        val os = when (Platform.currentPlatform) {
            is Platform.Linux -> return emptyArray()
            is Platform.MacOS -> "macos"
            is Platform.Windows -> "windows"
            Platform.Android -> error("Invalid platform: ${Platform.currentPlatform}")
        }

        val arch = when (Platform.currentPlatform.arch) {
            Arch.X86_64 -> "x64"
            Arch.AARCH64 -> "arm64"
        }

        val libs = File(System.getProperty("user.dir")).resolve("../appResources/${os}-${arch}/lib")
        if (!libs.exists()) return emptyArray()
        return arrayOf(libs.absolutePath)
    }

    override fun supported(): Boolean = true
}

// 在打包后的 app 使用
class ComposeResourcesDiscoveryDirectoryProvider : DiscoveryDirectoryProvider {
    override fun priority(): Int = DiscoveryProviderPriority.USER_DIR

    override fun directories(): Array<String> {
        val path = System.getProperty("compose.application.resources.dir") ?: return emptyArray()
        val libs = File(path).resolve("lib")
        if (!libs.exists()) return emptyArray()
        return arrayOf(libs.absolutePath)
    }

    override fun supported(): Boolean = true
}